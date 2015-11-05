package com.ireader.ml.feature.wordconcurrence;

import com.ireader.conf.Config;
import com.ireader.ml.feature.Transformer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by zxsted on 15-8-6.
 */
public class WordConcurrentTransfer  extends Transformer{


    private Configuration conf = null;
    private String inputPath = null;
    private String outputPath = null;
    private int windowsize = 0;


    public WordConcurrentTransfer(){

        conf = new Configuration();
    }

    public WordConcurrentTransfer(Configuration conf){
        this.conf = conf;
    }

    public WordConcurrentTransfer setInputPath(String inpath) {
        this.inputPath = inpath;
        return this;
    }

    public WordConcurrentTransfer setOutputPath(String outpath) {
        this.outputPath = outpath;
        return this;
    }

    public WordConcurrentTransfer setWindowSize(int windowsize) {
        this.windowsize = windowsize;
        return this;
    }


    @Override
    public boolean transform() throws InterruptedException, IOException, ClassNotFoundException {

        boolean success = runJob(conf,
                "wordConcurrent Job",
                WordConcurrentTransfer.class,
                inputPath,
                outputPath,
                WordConcurrnce.WordConCurrenceMapper.class,
                null,
                WordConcurrnce.WordConcurrenceReducer.class,
                null,
                Text.class,
                BytesWritable.class,
                WordPair.class,
                IntWritable.class,
                100,
                WholeFileInputFormat.class,
                false
        );

        return success;
    }



    public static void main(String[] args) {

        String conffile = args[0];

        try {
            Config config = new Config(conffile);

            Configuration conf = new Configuration();

            WordConcurrentTransfer transfer = new WordConcurrentTransfer(conf);

            transfer.setInputPath(config.getString("inputpath",""))
                    .setOutputPath(config.getString("outputPath",""))
                    .setWindowSize(config.getInt("windowsize",6));

            transfer.transform();


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
