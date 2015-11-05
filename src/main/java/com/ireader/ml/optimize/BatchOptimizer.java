package com.ireader.ml.optimize;

import com.ireader.ml.common.struct.DataPoint;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
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
import java.util.*;

/**
 * Created by zxsted on 15-10-10.
 *
 *
 * 批量优化框架类
 *
 * 按照 mini批次 计算梯度，以及损失函数， 提升计算效率
 *
 * 注意：这不是通常的minibatch 方法，常规的minibatch方法是随机梯度下降的优化
 *      这里仅仅是为了减少map循环调用次数的 全量批次计算
 *
 * 用到的conf 变量名列表：
 *  optimaize_mini_batch_size // 一个批次的大小
 *  optimaize_grad_classname  // 梯度计算类名称 , 返回梯度计算实现类的类名
 *  optimizer_weight_file     // 使用默认参数配置文件
 *
 * 批量优化类的设计思路：
 *
 *  1. 梯度和损失函数计算的类通过工厂类实例化时，要传入conf，以获取 除 weights 以外所有参数（包括超参数）
 *     这样，需要在 相应的drive类中将参数对应存储文件名配置进conf中，gradFunc和costFunc 每次计算循环时，
 *     都根据最新的各个参数初始化一次。
 *  2. 为了泛化，梯度的接口接受的参数是一维数组， 所以具体的梯度计算和损失函数计算要根据具体情况，进行参数
 *      解析， 如神经网络 的梯度和损失函数内部要实现， 多个权重矩阵的展平和重构操作
 *
 */
public class BatchOptimizer {


    /**
     *  梯度计算类
     * */
    public static class BatchGradMapper extends Mapper<LongWritable,Text,IntWritable,Text> {

//        private GradientFactory gradientFactory = null; //

        private Gradient gradient = null;

        private double[] weightsArray = null;

        private int batchSize = 0;                      // 一个批次的大小

        @Override
        protected void setup(Context context) throws IOException {

            Configuration conf = context.getConfiguration();

            batchSize = conf.getInt("optimaize_mini_batch_size", 100);

            String gradClassName = conf.get("optimaize_grad_classname");   // 梯度计算类名称 , 返回梯度计算实现类的类名

            /**
             * 构建梯度计算对象时，使用 conf 传递除 weights 以外的所有参数：
             * 如 dropout learningRate moment etc
             * */
            gradient = new GradientFactory().getInstance(gradClassName,conf);

            weightsArray = getWeightsArray(conf);

        }

        // 暂存一个 miniBatch 的list
        protected List<DataPoint> miniBatchList = new ArrayList<DataPoint>();

        @Override
        protected void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException {

            DataPoint cur_dp = new DataPoint(value.toString());
            miniBatchList.add(cur_dp);

            double[] grads = null;

            if(miniBatchList.size() == batchSize) {
                grads = gradient.gradient(miniBatchList,weightsArray);
                miniBatchList.clear(); // 处理完一个miniBatch 则清空这个list
            }


            for(int i = 0; i < grads.length; i++) {
                context.write(new IntWritable(i),new Text(String.valueOf(grads[i])));
            }
        }


        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

            double[] grads = null;
            if(miniBatchList.size() != 0) {
                grads = gradient.gradient(miniBatchList,weightsArray);
                miniBatchList.clear(); // 处理完一个miniBatch 则清空这个list
            }

            for(int i = 0; i < grads.length; i++) {
                context.write(new IntWritable(i),new Text(String.valueOf(grads[i])));
            }
        }
    } // BatchGradMapper


    public static class GradComsummer extends Reducer<IntWritable,Text,IntWritable,Text> {

        @Override
        protected void reduce(IntWritable key,Iterable<Text> values,Context context) throws IOException,InterruptedException{

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
    } // GradComsummer


    public static class GradReducer extends Reducer<IntWritable,Text,IntWritable,Text> {

        @Override
        protected void reduce(IntWritable key,Iterable<Text> values,Context context)
                                                 throws  IOException,InterruptedException{

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

 /**=================== Cost compute ===========================================================*/

    /**
     * 损失函数计算类
     * */
    public static class BatchCostMapper extends Mapper<LongWritable,Text,NullWritable,Text>{

        private GradientFactory gradientFactory = null;

        private Gradient gradient = null;

        private double[] weightsArray = null;

        private int batchSize = 0;

        @Override
        protected void setup(Context context) throws IOException {

            Configuration conf = context.getConfiguration();

            batchSize = conf.getInt("optimaize_mini_batch_size",100);

            String gradClassName = conf.get("optimize_grad_classname");

            /**
             * 构建梯度计算对象时，使用 conf 传递除 weights 以外的所有参数：
             * 如 dropout learningRate moment etc
             * */
            gradient = new GradientFactory().getInstance(gradClassName,conf);

            weightsArray = getWeightsArray(conf);

        }

        // 暂存一个 miniBatch 的list
        protected List<DataPoint> miniBatchList = new ArrayList<DataPoint>();

        @Override
        protected void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{

            DataPoint cur_dp = new DataPoint(value.toString());
            miniBatchList.add(cur_dp);

            double cost = 0.0;

            if(miniBatchList.size() == batchSize) {
                cost = gradient.loss(miniBatchList,weightsArray);
                miniBatchList.clear(); // 处理完一个miniBatch 则清空这个list
            }

            context.write(NullWritable.get(), new Text(String.valueOf(cost)));

        }


        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

            double cost = 0.0;

            if(miniBatchList.size() != 0) {
                cost = gradient.loss(miniBatchList,weightsArray);
                miniBatchList.clear();
            }

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

 /**============================================================================================*/


    /**
     * 从HDFS文件中加载权重， 参数以数组的形式返回
     * gradient 和 Cost 自己负责解析
     * 文件存储格式：
     * idx val
     *
     * example:
     * 1 0.76
     * 2 0.83
     * ...
     *
     */
    static double[] getWeightsArray(Configuration conf) throws IOException {

        double[] retArray = null;

        // 使用默认参数配置文件
        String WeightfileName = conf.get("optimizer_weight_file");

        FileSystem fs = FileSystem.get(conf);

        FSDataInputStream fis = null;

        BufferedReader bin = null;

        Map<Integer,Double> tempdict= new HashMap<Integer,Double>();

        try {
            fis = fs.open(new Path(WeightfileName));

            bin = new BufferedReader(new InputStreamReader(fis, "utf-8"));

            String line = null;

            while ((line = bin.readLine()) != null) {

                String fields[] = line.split("\\s+");

                int idx = Integer.parseInt(fields[0]);
                double val = Double.parseDouble(fields[1]);
                tempdict.put(idx,val);
            }
        }finally {
            bin.close();
            fis.close();
            fs.close();
        }

        retArray = new double[tempdict.size()];

        for(int i = 0 ; i < tempdict.size(); i++) {
            retArray[i] = tempdict.get(i);
        }

        return retArray;
    }


}
