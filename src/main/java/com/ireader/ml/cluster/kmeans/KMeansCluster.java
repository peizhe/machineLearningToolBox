package com.ireader.ml.cluster.kmeans;

import com.ireader.ml.common.measure.distance.Distance;
import com.ireader.ml.common.measure.distance.EuclideanDistance;
import com.ireader.ml.common.struct.DoubleVector;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-23.
 *  根据中心点文件将各个点划分到不同的类别
 */
public class KMeansCluster {

    public static class KMeansClusterMapper extends Mapper<LongWritable,Text,Text,IntWritable> {

        private ArrayList<Cluster> kClusters  = new ArrayList<Cluster>();

        @Override
        protected void setup(Context context) throws IOException ,InterruptedException{
            super.setup(context);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            FileStatus[] fileList = fs.listStatus(new Path(context.getConfiguration().get("clusterPath")));
            BufferedReader in = null;

            FSDataInputStream fsi = null;
            String line = null;
            for (int i = 0; i < fileList.length;i++) {
                if(!(fileList[i].isDirectory())) {
                    fsi = fs.open(fileList[i].getPath());
                    in = new BufferedReader(new InputStreamReader(fsi,"utf-8"));
                    while((line = in.readLine()) != null) {
                        Cluster cluster = new Cluster(line);
                        kClusters.add(cluster);
                    }
                }
            }
            in.close();
            fsi.close();

        }


        @Override
        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{
            DoubleVector doubleVector = new DoubleVector(value.toString());
            int id;
            try{
                id = getNearest(doubleVector);

                if (id == -1)
                    throw new InterruptedException("id==-1");
                else {
                    context.write(value,new IntWritable(id));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public int getNearest(DoubleVector doubleVector) throws Exception {
            int id = -1;
            double distance = Double.MAX_VALUE;
            Distance<Double> distanceMeasure = new EuclideanDistance<Double>();
            double newDis = 0.0;
            for(Cluster cluster : kClusters) {
                newDis = distanceMeasure.getDistance(cluster.getCenter().getValue(),
                        doubleVector.getValue());
                if(newDis < distance){
                    id = cluster.getClusterID();
                    distance = newDis;
                }
            }
            return id;

        }
    }
}
