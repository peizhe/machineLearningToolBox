package com.ireader.click_adjust;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created by zxsted on 15-11-1.
 */
public class ClickAdjustTrain {

    public static boolean  runAdjustJob(String dataSetPath,String outputPath,Configuration conf) throws IOException, ClassNotFoundException, InterruptedException {

        Job job = new Job(conf,"click_adjust");

        job.setJarByClass(ClickAdjustTrain.class);

        job.setMapperClass(ClickAdjustMR.ClickAdjustMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(ClickAdjustMR.ClickAdjustReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(dataSetPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job.waitForCompletion(true);
    }


    public static void main(String[] args) {

        Configuration conf = new Configuration();
        String inputPath = args[0];
        String outputPath = args[1];
        try {

            boolean success = runAdjustJob(inputPath,outputPath,conf);
            System.out.println("训练结束！");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
