package com.ireader.ml.feature.tfidf;

import com.ireader.conf.Config;
import com.ireader.ml.feature.Transformer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by zxsted on 15-8-10.
 */
public class TFIDFTransformer  extends Transformer {

    private Configuration conf = null;

    // 是否进行 IDF 计算
    private boolean isIDF = true;

    // 丢弃的最小频数阈值
    private int dropThreshold = 0;

    // 输入路径
    private String inputPath = null;

    // 输出路径
    private String outputPath = null;


    public TFIDFTransformer(){
        this.conf = new Configuration();
    }


    public TFIDFTransformer setInputPath(String  inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public TFIDFTransformer setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }


    // 是否使用idf
    public TFIDFTransformer setIsIDF(boolean isIDF){
        this.isIDF = isIDF;
        return this;
    }

    // 设置丢弃阈值
    public TFIDFTransformer setDropThreshold(int threshold){
        this.dropThreshold = threshold;
        return this;
    }

    public TFIDFTransformer setConf(Configuration conf) {
        this.conf = conf;
        return this;
    }


    @Override
    public boolean fit(){
        System.out.println("TFIDF need`t fit!");
        return false;
    }

    @Override
    public boolean transform() throws IOException, ClassNotFoundException, InterruptedException {

        boolean success = false;

        String tfoutputpath =null;

        FileSystem fs = FileSystem.get(conf);
        FileStatus[] p = fs.listStatus(new Path(this.inputPath));

        if(isIDF){
            tfoutputpath = "/tmp/TF" ; //

        }else {

            tfoutputpath = this.outputPath;
        }

        success = runJob(conf,
                "TF job",
                TFIDFTransformer.class,
                this.inputPath,
                tfoutputpath,
                TFIDFgenerator.IDFMapper.class,
                TFIDFgenerator.TFCombiner.class,
                TFIDFgenerator.TFReducer.class,
                TFIDFgenerator.TFIDFPartitoner.class,
                Text.class,
                Text.class,
                Text.class,
                Text.class,
                p.length,
                false);

        if(success)
            System.out.println("TF compute completed !");
        else{
            System.out.println("TF compute faild as TF step!");
            System.exit(1);
        }

        if(isIDF) {

            success |= runJob(conf,
                    "IDF Job",
                    TFIDFTransformer.class,
                    tfoutputpath,
                    this.outputPath,
                    TFIDFgenerator.IDFMapper.class,
                    null,
                    TFIDFgenerator.IDFReducer.class,
                    null,
                    Text.class,
                    Text.class,
                    Text.class,
                    Text.class,
                    p.length,
                    false);
        }


        if(success) {
            System.out.println("TFIDF compute completed!");
            fs.delete(new Path(tfoutputpath),true);
        } else {
            System.out.println("TFIDF compute faild at IDF step!");
        }


        return success;
    }


    public static void main(String[] args) {

        String configfile = args[0];      // model 配置文件

        try {
            Config config = new Config(configfile);   // 加载模型配置文件

            Transformer transformer =new  TFIDFTransformer()
                    .setInputPath(config.getString("TFIDF_inputpath",""))
                    .setOutputPath(config.getString("TFIDF_outputPath",""))
                    .setDropThreshold(config.getInt("TFIDF_dropthresh",2))
                    .setIsIDF(config.getBoolean("TFIDF_isIDF",true));

            transformer.transform();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
