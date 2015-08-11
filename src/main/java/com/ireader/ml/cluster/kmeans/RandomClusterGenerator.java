package com.ireader.ml.cluster.kmeans;

import com.ireader.ml.common.struct.DoubleVector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zxsted on 15-7-23.
 * 随机生成 类别中心点
 */
public class RandomClusterGenerator {
    private int k;

    private FileStatus[] fileList;
    private FileSystem fs;
    private ArrayList<Cluster> kClusters;
    private Configuration conf;


    public RandomClusterGenerator(Configuration conf,String filePath,int k) {
        this.k = k;

        try{
            fs = FileSystem.get(URI.create(filePath),conf);
            fileList = fs.listStatus(new Path(filePath));
            kClusters = new ArrayList<Cluster>(k);
            this.conf = conf;
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * destinationPath the destination Path we will store
     * our cluster file in.the initial file will be named clusters-0
     * */
    public void generateInitialCluster(String destinationPath) {
        Text line = new Text();
        FSDataInputStream fsi = null;
        try{
            for(int i = 0; i < fileList.length; i++) {
                fsi = fs.open(fileList[i].getPath());

                LineReader lineReader = new LineReader(fsi);
                while(lineReader.readLine(line) > 0) {
                    System.out.println("read a line :" + line);
                    DoubleVector doubleVector = new DoubleVector(line.toString());
                    makeDescision(doubleVector);
                }
            }
        }catch(IOException e) {
            e.printStackTrace();
        } finally{
            try{
                fsi.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        writeBackToFile(destinationPath);
    }

    //
    public void makeDescision(DoubleVector doubleVector) {
        if(kClusters.size() < k) {
            Cluster cluster = new Cluster(kClusters.size()+1, doubleVector);
            kClusters.add(cluster);
        }
        else {
            int choice = randomChoose(k);
            if(!(choice == -1)) {
                int id = kClusters.get(choice).getClusterID();
                kClusters.remove(id);
                Cluster cluster = new Cluster(id, doubleVector);
                kClusters.add(cluster);
            }
        }
    }

    public int randomChoose(int k) {
        Random random = new Random();
        if(random.nextInt(k+1) == 0) {
            return new Random().nextInt(k);
        }else
            return -1;
    }

    public void writeBackToFile(String destinationPath) {
        Path path = new Path(destinationPath + "cluster-0/clusters");
        FSDataOutputStream fsi = null;

        try{
            fsi = fs.create(path);
            for(Cluster cluster:kClusters) {
                fsi.write((cluster.toString() + "\n").getBytes());
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                fsi.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

}
