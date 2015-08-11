package com.ireader.ml.classifier.NaiveBayes;

import com.ireader.ml.Driver;
import com.ireader.ml.classifier.Trainer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-5.
 */
public class NaiveBayesDriver extends Trainer {

    private Configuration  conf =null;

    private String srcPath = null;

    private String destPath = null;

    public NaiveBayesDriver(){
        this.conf = new Configuration();
    }

    public NaiveBayesDriver(Configuration conf){
        this.conf = conf;
    }

    public NaiveBayesDriver setSrcPath(String path){
        this.srcPath = path;
        return this;
    }

    public NaiveBayesDriver setDestPath(String destPath){
        this.destPath = destPath;
        return this;
    }



    @Override
    public boolean fit() throws IOException, InterruptedException, ClassNotFoundException {

        boolean success = runJob(conf,
                "Naive bayse training job",
                NaiveBayesDriver.class,
                this.srcPath,
                this.destPath,
                NaiveBayesTrain.TrainMapper.class,
                null,
                NaiveBayesTrain.TrainReducer.class,
                null,
                null,
                null,
                Text.class,
                Integer.class,
                100,
                false);

        return success;
    }

    @Override
    public boolean transform() throws IOException, InterruptedException, ClassNotFoundException {

        boolean success = runJob(conf,
                "Naive bayse predicting job",
                NaiveBayesDriver.class,
                this.srcPath,
                this.destPath,
                NaiveBayesPredict.NBPredictMapper.class,
                null,
                null,
                null,
                null,
                null,
                Text.class,
                Text.class,
                100,
                false);

        return success;
    }


    /**
     *  本地上传到hdfs中
     * */
    public static void put2HDFS(String src,String dst,Configuration conf) throws Exception{

        Path dstPath = new Path(dst);
        FileSystem hdfs = dstPath.getFileSystem(conf);

        hdfs.copyFromLocalFile(false,true,new Path(src),new Path(dst));

    }

    /**
     *  从hdfs上下载 文件
     * */
    public static void getFromHDFS(String src,String dst,Configuration conf) throws Exception{

        Path dstPath = new Path(dst);
        FileSystem lfs = dstPath.getFileSystem(conf);
        String temp[] = src.split("/");
        Path ptemp = new Path(temp[temp.length - 1]);
        if(lfs.exists(ptemp))
            lfs.delete(ptemp,true);
        lfs.copyToLocalFile(true, new Path(src), dstPath);
    }


    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf,args).getRemainingArgs();
        FileSystem fs = FileSystem.get(conf);
        Path path_train,path_temp,path_test,path_out;

        if(otherArgs.length != 5){
            System.err.println("Usage:NaiveBayesDriver <dfs_path> <conf> <train> <test> <out>");
            System.exit(2);
        }

        conf.set("conf", otherArgs[0] + "/" + otherArgs[1]);     // 配置文件 class 和feature 的字典
        conf.set("train", otherArgs[0] + "/" + otherArgs[2]);    // 训练样本
        conf.set("test", otherArgs[0] + "/" + otherArgs[3]);     // 测试样本
        conf.set("output", otherArgs[0] + "/" + otherArgs[4]);   // 输出目录

        // 将文件上传到hdfs
        put2HDFS(otherArgs[1],otherArgs[0] + "/" + otherArgs[1],conf);
        put2HDFS(otherArgs[2],otherArgs[0] + "/" + otherArgs[2],conf);
        put2HDFS(otherArgs[3],otherArgs[0] + "/" + otherArgs[3],conf);

        path_train = new Path(otherArgs[0] +"/"+ otherArgs[2]);
        path_temp  = new Path(otherArgs[0] +"/"+ otherArgs[2]+".train");
        path_test  = new Path(otherArgs[0] + "/"+ otherArgs[3]);
        path_out   = new Path(otherArgs[0] + "/" + otherArgs[4]);


        Trainer driver = new NaiveBayesDriver();

        //
        System.out.println("开始训练 NaiveBayes Classifier");
        boolean tsuccess = driver.train();

        if(tsuccess)
            System.out.println("训练NaiveBayes Classifier 成功！");
        //

        conf.set("train_result", otherArgs[0] + "/" +otherArgs[2] + ".train");   //
        System.out.println("开始使用 NaiveBayes Classifier 进行预测！");

        boolean psuccess = driver.predict();

        if(psuccess)
            System.out.println("NaiveBayes classifier 预测结束！");






    }

}
