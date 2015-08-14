package com.ireader.ml;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-5.
 */
public  abstract class Driver {


    /**
     *  模型 训练
     * */
    public  abstract boolean fit() throws IOException, InterruptedException, ClassNotFoundException;


    /**
     * 模型预测
     * */
    public  abstract boolean transform()throws IOException, InterruptedException, ClassNotFoundException;





    public boolean  runJob(Configuration conf,
                           String jobname,
                           Class jarclass,
                           String inputPath,
                           String outputPath,
                           Class mapclass,
                           Class combinerClass,
                           Class reduceClass,
                           Class partitionerClass,
                           Class mapkeyClass,
                           Class mapValClass,
                           Class outputKeyClass,
                           Class OutputValClass,
                           Integer NumberReduceTasks,
                           Class InputFormatClass,
                           boolean isDeleteInputPath) throws InterruptedException, IOException, ClassNotFoundException {

        boolean success = runJob(conf,
                jobname,
                jarclass,
                inputPath,
                outputPath,
                mapclass,
                combinerClass,
                reduceClass,
                partitionerClass,
                mapkeyClass,
                mapValClass,
                outputKeyClass,
                OutputValClass,
                NumberReduceTasks,
                InputFormatClass,
                null,
                isDeleteInputPath);
        return success;


        }




    public boolean  runJob(Configuration conf,
                           String jobname,
                           Class jarclass,
                           String inputPath,
                           String outputPath,
                           Class mapclass,
                           Class combinerClass,
                           Class reduceClass,
                           Class partitionerClass,
                           Class mapkeyClass,
                           Class mapValClass,
                           Class outputKeyClass,
                           Class OutputValClass,
                           Integer NumberReduceTasks,
                           Class InputFormatClass,
                           Class OutputFormatClass,
                           boolean isDeleteInputPath) throws InterruptedException, IOException, ClassNotFoundException {

        FileSystem fs = FileSystem.get(conf);
        Path out = new Path(outputPath);
        if(fs.exists(out)){
            fs.delete(out,true);
        }

        Job job =  initJob(conf,
                jobname,
                jarclass,
                inputPath,
                outputPath,
                mapclass,
                combinerClass,
                reduceClass,
                partitionerClass,
                mapkeyClass,
                mapValClass,
                outputKeyClass,
                OutputValClass,
                NumberReduceTasks);

        if (null != InputFormatClass) job.setInputFormatClass(InputFormatClass);
        if (null != OutputFormatClass) job.setOutputFormatClass(OutputFormatClass);



        boolean success = job.waitForCompletion(true);

        if(isDeleteInputPath)
            fs.delete(new Path(inputPath));

        return success;

    }


        public boolean  runJob(Configuration conf,
                           String jobname,
                           Class jarclass,
                           String inputPath,
                           String outputPath,
                           Class mapclass,
                           Class combinerClass,
                           Class reduceClass,
                           Class partitionerClass,
                           Class mapkeyClass,
                           Class mapValClass,
                           Class outputKeyClass,
                           Class OutputValClass,
                           Integer NumberReduceTasks,
                           boolean isDeleteInputPath) throws InterruptedException, IOException, ClassNotFoundException {

        boolean success = runJob(conf,
                jobname,
                jarclass,
                inputPath,
                outputPath,
                mapclass,
                combinerClass,
                reduceClass,
                partitionerClass,
                mapkeyClass,
                mapValClass,
                outputKeyClass,
                OutputValClass,
                NumberReduceTasks,
                null,
                null,
                isDeleteInputPath);
        return success;
    }


    // job 运行辅助
    private Job  initJob(Configuration conf,
                            String jobname,
                            Class jarclass,
                            String inputPath,
                            String outputPath,
                            Class mapclass,
                            Class combinerClass,
                            Class reduceClass,
                            Class partitionerClass,
                            Class mapkeyClass,
                            Class mapValClass,
                            Class outputKeyClass,
                            Class OutputValClass,
                            Integer NumberReduceTasks
    ) throws IOException, ClassNotFoundException, InterruptedException {

//        Job job = new Job(conf,jobname);
        Job job = Job.getInstance(conf,jobname);
        job.setJarByClass(jarclass);
        job.setMapperClass(mapclass);

        Path input = new Path(inputPath);
        Path out = new Path(outputPath);


        if(reduceClass != null){
            job.setReducerClass(reduceClass);
        }
        if(combinerClass != null) {
            job.setCombinerClass(combinerClass);
        }
        if(partitionerClass != null){
            job.setPartitionerClass(partitionerClass);
        }
        if(NumberReduceTasks != null){
            job.setNumReduceTasks(NumberReduceTasks);
        }
//        else if (reduceClass != null) {
//            job.setNumReduceTasks(10);   // 默认10个
//        }
//        else {
//            job.setNumReduceTasks(0);   // 没有reduce
//        }

        job.setMapOutputKeyClass(mapkeyClass);
        job.setMapOutputValueClass(mapValClass);


        if(outputKeyClass != null) job.setOutputKeyClass(outputKeyClass);
        if(outputKeyClass != null) job.setOutputValueClass(OutputValClass);

        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, out);


        return job;
    }

}
