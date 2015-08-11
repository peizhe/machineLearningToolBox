package com.ireader.ml.cluster.kmeans;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

/**
 * Created by zxsted on 15-7-23.
 */
public class KMeansDriver {

    private int k;
    private int iterationNum;
    private String sourcePath;
    private String outputPath;

    private Configuration conf ;

    public KMeansDriver(int k , int iterationNum,String sourcePath,String outputPath,Configuration conf)
    {
        this.k = k;
        this.iterationNum = iterationNum;
        this.sourcePath = sourcePath;
        this.outputPath = outputPath;
        this.conf = conf;
    }


    // 聚类循环主任务
    public boolean clusterCenterJob() throws Exception {

        boolean ret = false;

        for(int i = 0; i < iterationNum; i++) {

            ret = true;

            Job clusterCenterJob = new Job(conf,"clustercenterJob at iteration "+ i+"iteration.");
//            clusterCenterJob.setJobName("clustercenterJob");
            clusterCenterJob.setJarByClass(KMeans.class);

            clusterCenterJob.getConfiguration().set("clusterPath", outputPath + "/cluster-" + i + "/");

            clusterCenterJob.setMapperClass(KMeans.KMeansMapper.class);
            clusterCenterJob.setMapOutputKeyClass(IntWritable.class);
            clusterCenterJob.setMapOutputValueClass(Cluster.class);

            clusterCenterJob.setCombinerClass(KMeans.KMeansCombiner.class);
            clusterCenterJob.setReducerClass(KMeans.KMeansReader.class);
            clusterCenterJob.setOutputKeyClass(NullWritable.class);
            clusterCenterJob.setOutputValueClass(Cluster.class);

            FileInputFormat.setInputPaths(clusterCenterJob, new Path(this.sourcePath));
            FileOutputFormat.setOutputPath(clusterCenterJob, new Path(this.outputPath));

            ret |= clusterCenterJob.waitForCompletion(true);

            if(ret) {
                System.out.println("iteration :" + i + " success!");
            }else{
                System.out.println("iteration :" + i + " failed!");
                throw new Exception("KMeans iteration "+i+"has bean failed!");
            }


        }

        return ret;

    }

    public boolean KMeansClusterJob() throws IOException ,InterruptedException,ClassNotFoundException{

        Job KMeansClusterJob = new Job();
        KMeansClusterJob.setJobName("KMeansClustedJob");
        KMeansClusterJob.setJarByClass(KMeansCluster.class);

        KMeansClusterJob.setMapperClass(KMeansCluster.KMeansClusterMapper.class);

        KMeansClusterJob.setMapOutputKeyClass(Text.class);
        KMeansClusterJob.setMapOutputValueClass(IntWritable.class);

        KMeansClusterJob.setNumReduceTasks(0);

        FileInputFormat.addInputPath(KMeansClusterJob, new Path(sourcePath));
        FileOutputFormat.setOutputPath(KMeansClusterJob, new Path(outputPath + "/clusteredInstances" + "/"));

        boolean ret = KMeansClusterJob.waitForCompletion(true);

        System.out.println("finished!");

        return ret;
    }


    public void generateInitialCluster() {
        RandomClusterGenerator generator =  new RandomClusterGenerator(conf, sourcePath, k);
        generator.generateInitialCluster(outputPath+"/");
    }


    public boolean run() throws Exception {

        this.generateInitialCluster();
        boolean ret = this.clusterCenterJob();
        ret |= this.KMeansClusterJob();

        return ret;
    }


    public static void main(String[] args) throws Exception {

        System.out.println("Start KMeans job ......");
        Configuration conf = new Configuration();
        int k = Integer.parseInt(args[0]);
        int iterationNum = Integer.parseInt(args[1]);
        String sourcePath = args[2];
        String outputPath = args[3];
        KMeansDriver dirver = new KMeansDriver(k,iterationNum,sourcePath,outputPath,conf);
        boolean success = dirver.run();
    }



}
