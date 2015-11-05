package com.ireader.ml.classifier.tree;

import com.ireader.ml.classifier.tree.singletree.CART;
import com.ireader.ml.classifier.tree.singletree.dto.Ensemble;
import com.ireader.ml.classifier.tree.singletree.dto.Tree;
import com.ireader.ml.classifier.tree.singletree.mr.Cost;
import com.ireader.ml.classifier.tree.singletree.mr.DTPredict;
import com.ireader.ml.classifier.tree.singletree.mr.FeatureMaxMin;
import com.ireader.ml.classifier.tree.singletree.mr.UpdateLabel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by zxsted on 15-11-3.
 *
 * GBDT 的主函数
 */
public class GBDT {

    Configuration conf = null;
    Ensemble ensemble = null;
    CART cart = new CART();

    private double learnRate;
    private int regionNum;                     // 特征值的划分区间个数
    private int maxleafnodeNum ;               // 树的叶子节点个数总数
    private int maxTreeDepth;                  // 树的深度
    private double cha_threshold = 0.00001;    // 误差差值阈值
    private double cost_threshold;

    String attributeMetaInfoPath =null;        // maxmin
    String dataSetPath = null;
    String runDir =null;

    public GBDT(){
        conf = new Configuration();
    }

    /**
     * DT_FEAT
     * DT_RULE
     * ENSEMBLE_PATH
     * DT_FEATNUM
     *
     * */

    /**
     * GBDT 的训练函数
     *
     * @param modelFile
     * @param dataSetPath
     * @param maxIter
     * @param msethreshold
     * @return
     */
    public void train(String modelFile,String dataSetPath,int maxIter,double msethreshold)
                throws InterruptedException, IOException, ClassNotFoundException, ParserConfigurationException {

        FileSystem fs = FileSystem.get(conf);

        // 如果之前有已经训练的模型，则加载后继续训练
        if (fs.exists(new Path(modelFile))) {
            List<String> list = loadHDFStolist(modelFile,conf);
            if (list.size() != 0){
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < list.size() - 1; i++) {
                    sb.append(list.get(i) + "\n");
                }
                sb.append(list.get(list.size()));
                ensemble.load(sb.toString());
            }
        }

        // 计算各个feature的最大最小值
        runMaxMinJob(dataSetPath, attributeMetaInfoPath);

        cart.setRegionNum(this.regionNum)
            .setMaxTreeDepth(this.maxTreeDepth)
            .setMaxleafnodeNum(this.maxleafnodeNum)
            .setCha_threshold(this.cha_threshold);

        String curdataSetPath = dataSetPath;

        int i = 0;
        for( ; i < maxIter; i++) {

            this.learnRate*=0.95;

            // 训练 回归树
            Tree tree =  cart.DTtrain(this.attributeMetaInfoPath,curdataSetPath,this.runDir+"_i",
                                     this.regionNum,this.maxleafnodeNum,this.maxTreeDepth,this.cha_threshold,this.conf);
            // 将本次训练好的模型加入 ensemble 中
            ensemble.add(tree, this.learnRate);

            // 将新的model 保存到hdfs中
            if (fs.exists(new Path(modelFile))) fs.delete(new Path(modelFile));

            String[] temparr = ensemble.docToString(ensemble.persist()).split("\n");
            List<String> list = new ArrayList<String>();
            for (String line : list) {
                list.add(line);
            }

            saveListToHDFS(list,modelFile,conf);

            conf.set("ENSEMBLE_PATH",modelFile);

            // 根据当前模型更新 数据集的label
            curdataSetPath = this.runDir+"/uplabel_"+i;
            runUpdateLabelJob(modelFile,curdataSetPath,this.runDir+"/uplabel_"+i);

            // 每隔 10 次循环计算一次cost
            if (i % 10 == 0){
                String costoutpath = this.runDir+"/cost_"+i;
                runCostJob(modelFile, dataSetPath, costoutpath);

                // 提取出结果
                List<String> costout = loadHDFStolist(costoutpath,conf);
                double curcost = Double.parseDouble(costout.get(0).trim());

                System.out.println("第" + i + "次循环，模型的损失值为：" + curcost);
                if (curcost < cost_threshold)
                    System.out.println("当前模型的cost 小于指定阈值，提前结束！");
            }

            // 清除本次计算使用的数据集
            if(fs.exists(new Path(curdataSetPath))) fs.delete(new Path(curdataSetPath));
        }

        System.out.println("模型训练完毕！共进行了" + i + "次循环！");

    }

    /**
     * GBDT 的预测函数
     *
     * @param modelFile
     * @param dataSetPath
     * @param outputPath
     * @return
     */
    public boolean predict(String modelFile,String dataSetPath,String outputPath)
                            throws InterruptedException, IOException, ClassNotFoundException {

        return runPredictJob(modelFile,dataSetPath,outputPath);
    }


    /**
     * 更新数据集的
     *
     * @param modelPath
     * @param dataSetPath
     * @param outpath
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean runUpdateLabelJob(String modelPath, String dataSetPath,String outpath)
                                     throws IOException, ClassNotFoundException, InterruptedException {

        conf.set("ensemble_path", modelPath);

        Job job = new Job(conf,"GBDT_UPDATE_LABEL");

        job.setJarByClass(GBDT.class);

        job.setMapperClass(UpdateLabel.UpdateLabelMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(dataSetPath));
        FileOutputFormat.setOutputPath(job, new Path(outpath));

        return job.waitForCompletion(true);
    }

    /**
     * 模型预测的cost函数
     * @param modelPath
     * @param dataSetPath
     * @param outpath
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean runCostJob(String modelPath, String dataSetPath,String outpath)
                          throws IOException, ClassNotFoundException, InterruptedException {

        conf.set("ensemble_path", modelPath);

        Job job = new Job(conf,"GBDT_COST");

        job.setJarByClass(GBDT.class);

        job.setMapperClass(Cost.CostMapper.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setCombinerClass(Cost.CostCombimer.class);

        job.setNumReduceTasks(1);
        job.setReducerClass(Cost.CostReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(dataSetPath));
        FileOutputFormat.setOutputPath(job, new Path(outpath));

        return job.waitForCompletion(true);
    }

    /**
     * 计算各个 特征的最大和最小值
     *
     * @param dataSetPath
     * @param outPath
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean runMaxMinJob(String dataSetPath, String outPath)
                          throws IOException, ClassNotFoundException, InterruptedException {

        Job job = new Job(conf,"FEATURE_MAXMIN");

        job.setJarByClass(GBDT.class);

        job.setMapperClass(FeatureMaxMin.FeatureMaxMinMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setCombinerClass(FeatureMaxMin.FeatureMaxMinConsumer.class);

        job.setNumReduceTasks(1);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(FeatureMaxMin.FeatureMaxMinReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(dataSetPath));
        FileOutputFormat.setOutputPath(job, new Path(outPath));

        return job.waitForCompletion(true);

    }


    /**
     *  对指定数据集的数据进行预测
     *
     * @param modelPath
     * @param dataSetPath
     * @param predictPath
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean runPredictJob(String modelPath,String dataSetPath,String predictPath)
                                  throws IOException, ClassNotFoundException, InterruptedException {

        conf.set("ensemble_path",modelPath);

        Job job = new Job(conf, "GBDT_PREDICT");

        job.setJarByClass(GBDT.class);

        // 设置 map 阶段
        job.setJarByClass(DTPredict.DTPredictMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(dataSetPath));
        FileOutputFormat.setOutputPath(job, new Path(predictPath));

        return job.waitForCompletion(true);

    }


    /**
     *  将指定的 HDFS 文件读出为list
     *
     * @param fileName
     * @param conf
     * @return
     * @throws IOException
     */
    public static List<String> loadHDFStolist(String fileName,Configuration conf) throws IOException {

        List<String> retList = new ArrayList<String>();

        FileSystem fs = FileSystem.get(conf);

        FSDataInputStream iStream = fs.open(new Path(fileName));

        Scanner scanner = new Scanner(iStream);

        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            retList.add(line);
        }

        scanner.close();
        iStream.close();

        return  retList;
    }

    /**
     * 将指定list中的内容保存到 HDFS 中
     *
     * @param list
     * @param fileName
     * @param conf
     */
    public static void saveListToHDFS(List<String> list, String fileName,Configuration conf) throws IOException {

        FileSystem fs = FileSystem.get(conf);

        if (fs.exists(new Path(fileName))) {
            fs.delete(new Path(fileName));
        }

        FSDataOutputStream ostream = fs.create(new Path(fileName));

        PrintWriter pw = new PrintWriter(ostream);

        for (String line : list) {
            pw.println(line);
        }
        pw.close();
        ostream.close();
    }

    /** ============== getter and setter =============================================================== */

    public double getLearnRate() {
        return learnRate;
    }

    public GBDT setLearnRate(double learnRate) {
        this.learnRate = learnRate;
        return this;
    }

    public int getRegionNum() {
        return regionNum;
    }

    public GBDT setRegionNum(int regionNum) {
        this.regionNum = regionNum;
        return this;
    }

    public int getMaxleafnodeNum() {
        return maxleafnodeNum;
    }

    public GBDT setMaxleafnodeNum(int maxleafnodeNum) {
        this.maxleafnodeNum = maxleafnodeNum;
        return this;
    }

    public int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    public GBDT setMaxTreeDepth(int maxTreeDepth) {
        this.maxTreeDepth = maxTreeDepth;
        return this;
    }

    public double getCha_threshold() {
        return cha_threshold;
    }

    public GBDT setCha_threshold(double cha_threshold) {
        this.cha_threshold = cha_threshold;
        return this;
    }

    public double getCost_threshold() {
        return cost_threshold;
    }

    public GBDT setCost_threshold(double cost_threshold) {
        this.cost_threshold = cost_threshold;
        return this;
    }

    public String getAttributeMetaInfoPath() {
        return attributeMetaInfoPath;
    }

    public GBDT setAttributeMetaInfoPath(String attributeMetaInfoPath) {
        this.attributeMetaInfoPath = attributeMetaInfoPath;
        conf.set("DT_FEATNUM",this.attributeMetaInfoPath);
        return this;
    }

    public String getDataSetPath() {
        return dataSetPath;
    }

    public GBDT setDataSetPath(String dataSetPath) {
        this.dataSetPath = dataSetPath;
        return this;
    }

    public String getRunDir() {
        return runDir;
    }

    public GBDT setRunDir(String runDir) {
        this.runDir = runDir;
        return this;
    }
}
