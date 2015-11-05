package com.ireader.ml.cluster.kmeans;

import com.ireader.util.distance.Distance;
import com.ireader.util.distance.EuclideanDistance;
import com.ireader.ml.common.struct.DoubleVector;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-23.
 */
public class KMeans {

    public static class KMeansMapper extends Mapper<LongWritable,Text,IntWritable,Cluster> {

        private ArrayList<Cluster> kCluster = new ArrayList<Cluster>();   // 类中心点

        /**
         * 加载类别中心点
         */
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);

            FileSystem fs = FileSystem.get(context.getConfiguration());
            FileStatus[] fileList = fs.listStatus(new Path(context.getConfiguration().get("ClusterPath")));
            BufferedReader in = null;
            FSDataInputStream fsi = null;
            String line = null;
            for (int i = 0; i < fileList.length; i++) {
                if (!fileList[i].isDirectory()) {
                    fsi = fs.open(fileList[i].getPath());
                    in = new BufferedReader(new InputStreamReader(fsi, "UTF-8"));
                    while ((line = in.readLine()) != null) {
                        System.out.println("read a line :" + line);
                        Cluster cluster = new Cluster(line);
                        cluster.setNumOfPoints(0);
                        kCluster.add(cluster);
                    }
                }
            }
            in.close();
            fsi.close();
        }


        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            DoubleVector doubleVector = new DoubleVector(value.toString());    // 解析成一个实例

            int id;
            try {
                id = getNearest(doubleVector);
                if (id == -1)
                    throw new InterruptedException("id = -1");
                else {
                    Cluster cluster = new Cluster(id, doubleVector);
                    cluster.setNumOfPoints(1);
                    System.out.println("cluster that i emit is :" + cluster.toString());
                    context.write(new IntWritable(id), cluster);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public int getNearest(DoubleVector doubleVector) throws Exception {
            int id = -1;
            double distance = Double.MAX_VALUE;
            Distance<Double> distanceMeasure = new EuclideanDistance<Double>();
            double newDis = 0.0;
            for (Cluster cluster : kCluster) {
                newDis = distanceMeasure.getDistance(cluster.getCenter().getValue(),
                        doubleVector.getValue());
                if (newDis < distance) {
                    id = cluster.getClusterID();
                    distance = newDis;
                }
            }
            return id;

        }

    }
        public static class KMeansCombiner extends Reducer<IntWritable,Cluster,IntWritable,Cluster> {
            public void reduce(IntWritable key,Iterable<Cluster> values,Context context)
                throws IOException,InterruptedException {
                DoubleVector doubleVector = new DoubleVector();
                int numOfPoints = 0;
                for(Cluster cluster: values) {
                    numOfPoints += cluster.getNumOfPoints();
                    System.out.println("cluster is :" + cluster.getClusterID());
                    doubleVector = doubleVector.add(cluster.getCenter().multiply(cluster.getNumOfPoints()));
                }

                Cluster cluster = new Cluster(key.get(), doubleVector.divide(numOfPoints));
                cluster.setNumOfPoints(numOfPoints);
                context.write(key,cluster);

            }
        }



        public static class KMeansReader extends Reducer<IntWritable,Cluster,NullWritable,Cluster>{
            public void reduce(IntWritable key,Iterable<Cluster> value,Context context) throws IOException,
                    InterruptedException{
                DoubleVector doubleVector = new DoubleVector();
                int numOfPoints = 0;
                for(Cluster cluster:value) {
                    numOfPoints += cluster.getNumOfPoints();
                    doubleVector = doubleVector.add(cluster.getCenter().multiply(numOfPoints));
                }

                Cluster cluster = new Cluster(key.get(), doubleVector.divide(numOfPoints));
                cluster.setNumOfPoints(numOfPoints);
                context.write(NullWritable.get(),cluster);
            }

        }
    }

