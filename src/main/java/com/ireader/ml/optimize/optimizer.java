package com.ireader.ml.optimize;

import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zxsted on 15-8-10.
 */
public class optimizer {




    public static class GradMapper extends Mapper<LongWritable,Text,IntWritable,Text>{


        private List<Double> weightVec = new ArrayList<Double>();

        private GradientFactory gradFactory = null;

        private Gradient gradient = null;
        /**
         * 读取 模型的权重向量
         */
        protected void setup(Context context) throws IOException {

            Configuration conf = context.getConfiguration();

            String weightfile = conf.get("opti_weight_file");          //  参数文件

            String gradClassName = conf.get("opti_grad_classname");    // 梯度计算类的名称

//            GradientFactory gradFactory = new GradientFactory();

            gradient = new GradientFactory().getInstance(gradClassName,conf);


            FileSystem fs = FileSystem.get(conf);

            FSDataInputStream fis = null;

            BufferedReader bin = null;

            HashMap<Integer,Double> tempdict= new HashMap<Integer,Double>();
            String line = null;

            fis = fs.open(new Path(weightfile));

            bin = new BufferedReader(new InputStreamReader(fis,"utf-8"));

            while ((line = bin.readLine()) != null) {
               String fields[] = line.split("\\s+");

               int idx = Integer.parseInt(fields[0]);
               double val = Double.parseDouble(fields[1]);
               tempdict.put(idx,val);
            }

            for(int i = 0 ; i < tempdict.size(); i++) {
                weightVec.add(tempdict.get(i));
            }
        }

        @Override
        protected void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{
            DataPoint cur_dp = new DataPoint(value.toString());
            double[] grads = gradient.gradient(cur_dp, weightVec);

            for(int i = 0; i < grads.length; i++) {
                context.write(new IntWritable(i),new Text(String.valueOf(grads[i])));
            }

        }

    }


    public static class GradComsummer extends Reducer<IntWritable,Text,IntWritable,Text> {

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            Long num = 1l;
            for(Text val:values) {
                num+=1;
            }

            double sum = 0.0d;
            for(Text val : values) {
                sum += Double.parseDouble(val.toString().trim()) / num;
            }

            context.write(key, new Text(String.valueOf(sum) + ","+String.valueOf(num)));
       }

    }


    public static class GradReducer extends Reducer<IntWritable,Text,IntWritable,Text>{

        @Override
        protected void reduce(IntWritable key,Iterable<Text> values, Context context) throws IOException ,InterruptedException{

            double grad = 0.0d;
            long num = 0l;

            List<Double> grads = new ArrayList<Double>();
            List<Long> nums = new ArrayList<Long>();

            for(Text val : values) {
                String fields[] = val.toString().trim().split(",");
                grads.add(Double.parseDouble(fields[0].trim()));
                nums.add(Long.parseLong(fields[1].trim()));
            }

            long allnum = 0L;
            for (long val : nums) {
                allnum += val;
            }

            double meangrad = 0.0;
            for(int i = 0 ; i < grads.size() ; i++) {
                meangrad += grads.get(i) * (nums.get(i) / (double)allnum);
            }

            context.write(key,new Text(String.valueOf(meangrad)));
        }
    }





    /**==========================================================================================*/
    /**================  Cost function cosume  ==================================================*/
    /**==========================================================================================*/


    public static class CostMapper extends Mapper<LongWritable,Text,NullWritable,Text> {

        private List<Double> weights = new ArrayList<Double>();

        private Gradient gradient = null;

//        private void setWeights(double[] _weights) {
//            for(int i = 0 ; i < _weights.length ; i++) {
//                weights.add(_weights[i]);
//            }
//        }

        @Override
        protected void setup(Context context) {
            String gradClassName = context.getConfiguration().get("opti_grad_classname");

            gradient = new GradientFactory().getInstance(gradClassName,context.getConfiguration());


        }

        @Override
        protected void map(LongWritable key , Text val ,Context context ) throws IOException,InterruptedException {

//            Configuration conf = context.getConfiguration();


            DataPoint dp = new DataPoint(val.toString());
            double cost = gradient.loss(dp,weights);

            context.write(NullWritable.get(), new Text(String.valueOf(cost)));

        }
    }

    public static class CostComsumer extends  Reducer<NullWritable,Text,NullWritable,Text> {

        @Override
        public void reduce(NullWritable key, Iterable<Text> values,Context context) throws IOException,InterruptedException {

            double sum = 0.0;
            double num = 0;
            for (Text val : values) {
                num +=1;
            }

            for(Text val : values) {
                sum += Double.parseDouble(val.toString().trim()) / num;
            }

            context.write(key, new Text(String.valueOf(sum) + "," + String.valueOf(num)));

        }
    }


    /**
     *  reducer 个数要设置为1
     * */
    public static class CostReducer extends Reducer<NullWritable,Text,NullWritable,Text> {

        @Override
        protected void reduce(NullWritable key, Iterable<Text> values,Context context) throws IOException,InterruptedException {

            double cost = 0.0d;

            double num = 0.0d;

            List<Double> nums = new ArrayList<Double>();
            List<Double> costs = new ArrayList<Double>();

            for (Text val : values) {
                String[] items = val.toString().trim().split(",");
                num += Double.parseDouble(items[1].toString());
                costs.add(Double.parseDouble(items[0].toString()));
                nums.add(Double.parseDouble(items[1].toString()));
            }

            for (int i = 0 ; i < nums.size(); i++) {
                cost += costs.get(i) * (nums.get(i) / num);
            }

            context.write(key,new Text(String.valueOf(cost)));
        }
    }


    /**=====================================================================================================*/
    /**====================== 分布式 weight 更新 ============================================================*/
    /**=====================================================================================================*/
    /**
     * weight :
     *    idx   weight
     * gradient:
     *    idx   gradient
     * */

    public static class UpdateMapper extends Mapper<LongWritable ,Text,Text,Text> {

    }






}
