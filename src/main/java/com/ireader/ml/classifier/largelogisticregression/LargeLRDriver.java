package com.ireader.ml.classifier.largelogisticregression;


import com.ireader.ml.Driver;
import com.ireader.ml.classifier.Trainer;
import com.ireader.ml.conf.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;

import java.io.*;
import java.util.ArrayList;


/**
 * Created by zxsted on 15-7-24.
 */
public class LargeLRDriver extends Trainer {

    private Configuration conf = null;
    private WeightHelper weightHelper = null;        // 参数管理器
    private int iter = -1;                           // 训练循环次数
    private int featnum = -1;                        // model 样本特征个数
    private int slicelength = -1;                    // 划分每个work计算的feat 个数
    private double learnRate = 0.0;                  // 学习速率
    private String regType = "l2";                   // regulation type
    private double lambda = 0.0;                     // regulation
    private int validateStep = -1;                   // 验证间隔
    private double threshold = 0.0;                  // 结束阈值
    private String weightSavePath = null;            // 参数文件的hdfs地址
    private String trainInputPath;                   // 训练数据集hdfs地址
    private String trainOutputPath;                  // 训练结果输出地址
    private String predictInputPath;                 // 预测数据集hdfs地址
    private String preductOutputPath;                // 预测结果hdfs地址

    @Deprecated
    public LargeLRDriver(int featnum, int featSliceLen, double learnRate, String weightFile){

        this.featnum = featnum;
        conf = new Configuration();
        conf.setInt("LLR_FeatNum", featnum);
        conf.setInt("LLR_SliceLeangth", featSliceLen);
        conf.setDouble("LLR_Lambda", learnRate);
        conf.set("LLR_WeightSavePath", weightFile);


    }





    public LargeLRDriver(){
        conf = new Configuration();
    }


    public LargeLRDriver setFeatNum(int featNum) {
        this.featnum = featNum;
        conf.setInt("LLR_FeatNum", featnum);
        return this;
    }

    public LargeLRDriver setSlicelength(int slicelength) {
        this.slicelength =slicelength;
        conf.setInt("LLR_SliceLeangth", slicelength);
        return this;
    }

    public LargeLRDriver setLearnRate(double rate){
        this.learnRate = rate;
        conf.setDouble("LLR_Lambda", learnRate);
        return this;
    }

    public LargeLRDriver setRegType(String type){
        this.regType = type;
        return this;
    }

    public LargeLRDriver setLambda(double lambda){
        this.lambda = lambda;
        return this;
    }

    public LargeLRDriver setWeightSavePath(String path) {
        this.weightSavePath = path;
        conf.set("LLR_WeightSavePath", path);
        return this;
    }


    public LargeLRDriver setThreshold(double threshold)
    {
        this.threshold = threshold;
        return this;
    }

    public LargeLRDriver setIter( int iter) {
        this.iter = iter;
        return this;
    }

    public LargeLRDriver setValidateStep(int step) {
        this.validateStep = step;
        return this;
    }

    public LargeLRDriver setTrainInputPath(String input) {
        this.trainInputPath = input;
        return this;
    }

    public LargeLRDriver setTrainOutputPath(String output) {
        this.trainOutputPath = output;
        return this;
    }

    public LargeLRDriver setPredictInputPath(String input) {
        this.predictInputPath  = input;
        return this;
    }
    public LargeLRDriver setPredictOutputPath(String output) {
        this.preductOutputPath = output;
        return this;
    }



    // 训练
    @Override
    public boolean fit() throws IOException, InterruptedException, ClassNotFoundException {

        int iteration = this.iter;
        boolean success = false;
        double rmse = Double.MAX_VALUE;
        weightHelper = new WeightHelper(conf,this.weightSavePath,this.featnum);

        for (int i = 0; i <  iteration; i++) {

            // 1. 生成随机初始权重
            weightHelper.randomInitialWeight(this.featnum);

            // 2. 安指定的区间长度切割 特征向量 map job
            success = runJob(this.conf,
                    "split feat  columns job",
                    DistributeColum.class,
                    this.trainInputPath,
                    "tmp/ELR/splitcolomn",
                    DistributeColum.DistributeColumMapper.class,     // map
                    null,                                            // combine
                    null,                                            // reduce
                    null,                                            // part
                    Text.class,
                    WDataPoint.class,
                    null,
                    null,
                    0,                                            // reduce num
                    false);                                         // is delete input
            if(!success) error("split feat  columns job",i);

            // 3. 为每个特征 附加上 w*x 结果 mapred job
            success |= runJob(conf,
                    "append feat and weight`s inner product job",
                    RowGradientCompute.class,
                    "tmp/ELR/splitcolomn",
                    "tmp/ELR/appendvectorinnerproduct",
                    RowGradientCompute.RowGradientComputeMapper.class,
                    null,
                    RowGradientCompute.RowGradientComputeReducer.class,
                    null,
                    Text.class,
                    WDataPoint.class,
                    NullWritable.class,
                    WDataPoint.class,
                    100,
                    KeyValueTextInputFormat.class,                        // inputformat.class
                    true);
            if(!success) error("append feat and weight`s inner product job",i);

            // 4. 更新 权重  mapred job
            success |= runJob(conf,
                    "update model weights job",
                    UpdateWeight.class,
                    "tmp/ELR/appendvectorinnerproduct",
                    "tmp/ELR/newWeights",
                    UpdateWeight.UpdateWeightMapper.class,
                    UpdateWeight.UpdateWeightCombiner.class,
                    UpdateWeight.UpdateWeightReducer.class,
                    null,
                    NullWritable.class,
                    WDataPoint.class,
                    IntWritable.class,
                    DoubleWritable.class,
                    1,
                    KeyValueTextInputFormat.class,                          // inputformat.class
                    true);

            if(!success) error("update model weights job",i);
            // 5. 暂存旧的权重
            ArrayList<Double> oldWeightList = weightHelper.readNewWeights("tmp/ELR/newWeights");

            // 6. 将新权重存储到 hdfs
            weightHelper.updateWeights("tmp/ELR/newWeights");

            // 7. 如果满足指定的 step 间隔 计算 rsme 并与上次进行比较 如果小于一定的值则停机 并输出就权重到权重文件，否则继续循环
            if(((i / this.validateStep) != 0) && i!=0) {

                success |= runJob(conf,
                        "rmse compute job",
                        CostFunc.class,
                        this.trainInputPath,
                        "tmp/ELR/rsme",
                        CostFunc.CostFuncMapper.class,
                        CostFunc.CostFuncCombiner.class,
                        CostFunc.CostFuncReducer.class,
                        null,
                        LongWritable.class,
                        Text.class,
                        NullWritable.class,
                        DoubleWritable.class,
                        1,
                        false);
                if(!success) error("rmse compute job",i);

                // 读取 rsme 计算结果
                ArrayList<String> resList = readFromHdfs("tmp/ELR/rsme");
                if(resList.size() != 0) {
                    String line = resList.get(0).trim();

                    double curRmse = Double.parseDouble(line);
                    if ( rmse < curRmse){
                        rmse = curRmse;
                    }
                    if(Math.abs(rmse - curRmse) < this.threshold)
                        break;

                }
                if(success)
                    System.out.println("LR model validate at iteration " + i + " , rmse is" + rmse);
            }

        }    // for end

        if (success)
            System.out.println("LR modol training finished! with rmse : " + rmse);

        return success;
    }


    @Override
    public boolean  transform() throws InterruptedException, IOException, ClassNotFoundException {

        // 预测： mapjob

        boolean success = runJob(conf,
                "use LR model predict job",
                LRpredictor.class,
                this.predictInputPath,
                this.preductOutputPath,
                LRpredictor.LRpredictorMapper.class,
                null,
                null,
                null,
                LongWritable.class,
                Text.class,
                Text.class,
                DoubleWritable.class,
                0,
                false);

        if(!success) {
            System.err.println("LR model encount error when pridect !");
            System.exit(1);
        }

        return success;
    }

    // 错误报告，程序退出
    private void error(String jobname,int iter) {
        System.err.println(jobname + " encount error and failed at iteration " + iter );
        System.exit(1);
    }

    // 读取hdfs指定文件夹内容
    private ArrayList<String> readFromHdfs( String src) throws IOException {

        ArrayList<String> resList = new ArrayList<String>();
        Path srcDir = new Path(src);
        FileSystem fs = FileSystem.get(this.conf);
        FSDataInputStream fsi = null;
        BufferedReader br = null;

        if(fs.isDirectory(srcDir)){
            FileStatus[] fileStatuses = fs.listStatus(srcDir);
            for(FileStatus file : fileStatuses) {
                if(fs.isFile(file.getPath())){
                    fsi = fs.open(file.getPath());
                    br = new BufferedReader(new InputStreamReader(fsi,"utf-8"));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        resList.add(line.trim());
                    }
                }
                br.close();
                fsi.close();
            }
        }

        return resList;
    }



    public static void main(String[] args) {

        boolean isTrain = false;




        if(args.length < 3) {
            System.out.println("Input params number is wrong,you should execute like this:");
            System.out.println("Usage: \n hadoop jar LargeLRDriver <train|predict> <InputPath> <OutputPath> [configfile] ");
        }

        String type = args[0];

        if(type.equalsIgnoreCase("train")){
            isTrain = true;
        }

        String inputPath = args[1];
        String outputPath = args[2];

        String configfile = null;

        if(args.length == 4){
            configfile = args[3];
        }

        Config config = null;

        try {
            if (configfile != null && !configfile.equals("")) {
                config = new Config(configfile);
            } else{
                config = new Config();    // load default model preperties content
            }
        } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
        }

        if (isTrain) {
            Trainer driver = new LargeLRDriver().setTrainInputPath(config.getString("LLR_TrainInputPath","/tmp/zhangxiaoshan/output/discrete/part-00000"))
                    .setTrainOutputPath(config.getString("LLR_TrainOutputPath",""))
                    .setWeightSavePath(config.getString("LLR_WeightSavePath", ""))
                    .setIter(config.getInt("LLR_Iter", 20))
                    .setFeatNum(config.getInt("LLR_FeatNum",20))
                    .setSlicelength(config.getInt("LLR_SliceLeangth",10))
                    .setLearnRate(config.getDouble("LLR_LearnRate",0.0001))
                    .setRegType(config.getString("LLR_RegType","L2"))
                    .setLambda(config.getDouble("LLR_Lambda",0.001))
                    .setValidateStep(config.getInt("LLR_ValidateStep",3))
                    .setThreshold(config.getDouble("LLR_Threshold",0.001));

            try {

                driver.train();       // 执行模型训练

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            Trainer driver = new LargeLRDriver().setPredictInputPath(config.getString("LLR_PredictInputPath",""))
                            .setPredictOutputPath(config.getString("LLR_PredictOutputPath",""))
                            .setWeightSavePath(config.getString("LLR_WeightSavePath",""));
            try {

                driver.predict();    // 执行模型预测

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

}
