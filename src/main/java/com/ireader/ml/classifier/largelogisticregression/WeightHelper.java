package com.ireader.ml.classifier.largelogisticregression;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by zxsted on 15-7-24.
 */
public class WeightHelper {

    private int k;

    private FileStatus[] fileList;
    private FileSystem fs;
    private ArrayList<Double> weights = null;
    private Configuration conf = null;
    private String weightFilePath = null;

    private WeightHelper(){
        weights = new ArrayList<Double>();
    }

    public String getWeightFilePath(){
        return this.weightFilePath;
    }

    public WeightHelper(Configuration conf, String dirPath, int k) {
        this.k = k;
        this.weightFilePath = dirPath ;

        try{
            fs = FileSystem.get(URI.create(dirPath),conf);
            fileList = fs.listStatus(new Path(dirPath));
            weights = new ArrayList<Double>();
            this.conf = conf;
        }catch(IOException e) {
            e.printStackTrace();;
        }
    }




    // 生成一个随机权重向量
    private void randomInitial(int k) {
        this.k = k;
        this.weights.clear();
        Random rand = new Random();
        rand.setSeed(2233L);
        for(int i = 0 ;i < k; i++) {
            double temp = rand.nextDouble();
            while (weights.contains(temp))
                temp = rand.nextDouble();
            weights.add(temp);
        }
    }


    // 将随机生成的权重写入HDFS
    public void randomInitialWeight(int k) throws IOException {

        randomInitial(k);

        Path path = new Path( this.weightFilePath);

        FSDataOutputStream fso = null;

        if(fs.exists(path))
               fs.delete(path,true);

        fso = fs.create(path);

        fso.write((stringFormat(weights) + "\n").getBytes());

    }

    // 将已有的权重向量写入HDFS 权重文件中
    public void saveWeights( ArrayList<Double> weights) throws IOException {

        Path destFile = new Path(this.getWeightFilePath());

        // 将新的权重写入hdfs中
        if(fs.exists(destFile))
            fs.delete(destFile,true);

        FSDataOutputStream fso = null;

        fso = fs.create(destFile);

        fso.write((stringFormat(weights).toString()+"\n").getBytes());


        fso.close();

    }

    // 从结果文件夹中读取 权重文件
    public ArrayList<Double> readNewWeights(String srcdir) throws IOException {

        Path srcDir = new Path(srcdir);

        ArrayList<Double> newWeights = new ArrayList<Double>();
        HashMap<Integer, Double> newWeightdict = new HashMap<Integer,Double>();


//        FileSystem fs = FileSystem.get(conf);
        FileStatus[] fileList = fs.listStatus(srcDir);

        BufferedReader br = null;
        FSDataInputStream fsi = null;
        String line = null;


        // 读取权重跟新结果
        for(int i = 0 ; i < fileList.length; i++) {
            if(!fileList[i].isDirectory()) {
                fsi = fs.open(fileList[i].getPath());
                br = new BufferedReader(new InputStreamReader(fsi,"UTF-8"));

                while((line  = br.readLine().trim()) != null) {

                    String[] fields = line.split("\t");

                    int colnum = Integer.parseInt(fields[0]);
                    double weight = Double.parseDouble(fields[1]);

                    newWeightdict.put(colnum,weight);
                }
            }
            br.close();
            fsi.close();
        }

        if(newWeightdict.size() != this.k) {
            throw new IOException("权重向量长度不为" + this.k);
        }

        for(int i = 0 ; i < this.k;i++) {

            double wval = newWeightdict.get(i);

            newWeights.add(wval);
        }

        return newWeights;

    }




    // 从HDFS中读取权重跟新结果， 并写入权重文件中
    public void updateWeights(String srcdir) throws IOException {

        ArrayList<Double> newWeights = null;

        // 读取新权重
        newWeights =  readNewWeights(srcdir);
        //
        saveWeights(newWeights);

    }


     private String stringFormat(ArrayList<Double> weights) {

         StringBuffer wieghtsline = new StringBuffer();

         for (int i = 0 ; i < this.k; i++) {

             wieghtsline.append(((i == 0) ? "" : "\t") + String.valueOf(weights.get(i)));

         }

         return wieghtsline.toString().trim();
     }


    public static void main(String[] args) {
        WeightHelper  whelper = new WeightHelper();
        whelper.randomInitial(40);

        ArrayList<Double> ws  = whelper.weights;

        String str = whelper.stringFormat(ws);
        System.out.println(str);
    }
}
