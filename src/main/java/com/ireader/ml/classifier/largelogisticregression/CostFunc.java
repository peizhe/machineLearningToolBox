package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.struct.DataPoint;
import com.ireader.ml.common.struct.DoubleVector;
import com.ireader.util.StringUtil;
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

/**
 * Created by zxsted on 15-7-31.
 *
 * // 用于计算数据集的损失函数
 */
public class CostFunc {

    public static class CostFuncMapper extends Mapper<LongWritable,Text,IntWritable,Text> {

        //存储模型参数
        private ArrayList<Double> weights = new ArrayList<Double>();
        private DoubleVector weightvec = null;

        @Override
        protected void setup(Context context) throws IOException {

           Configuration conf = context.getConfiguration();

            String wfilename = conf.get("LLR_WeightSavePath");

            int featNum = conf.getInt("LLR_FeatNum", -1);


            // 从hdfs 上读取权重文件
            Path wFilePath = new Path(wfilename);

            FileSystem fs = FileSystem.get(context.getConfiguration());

            FSDataInputStream fsi = null;
            BufferedReader in = null;

            if(fs.isFile(wFilePath)) {
                fsi = fs.open(wFilePath);
                String line = null;

                in = new BufferedReader(new InputStreamReader(fsi,"utf-8"));

                while((line = in.readLine()) != null) {
                    if(!(featNum == StringUtil.StringToList(weights,line.trim(),"\t"))){
                        throw  new IOException("oad weights number and featnum is not same!");
                    }
                }

                weightvec = new DoubleVector(weights);
            }

            in.close();
            fsi.close();


        }


        @Override
        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{

            String curline = value.toString();

            if(curline.split("\\s+").length < 4) return;

            DataPoint dp = new DataPoint(curline);

            double pred = dp.getFeatures().innerMul(weightvec);

            double diffval = dp.getLabel() - pred;

            IntWritable one = new IntWritable(1);
            context.write(one, new Text(String.valueOf(diffval * diffval)));

        }


    }


    public static class CostFuncCombiner extends Reducer<IntWritable,Text,IntWritable,Text> {

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            double sum = 0.0;
            double num = 0;
            for(Text val :  values) {
                sum +=  Double.parseDouble(val.toString().trim());
                num++;
            }

            context.write(new IntWritable(1), new Text(sum/num + "-" + num));

        }
    }

    public static class CostFuncReducer extends Reducer<IntWritable,Text,NullWritable,Text> {

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException,
                InterruptedException {

            double sum = 0.0;
            double all_num = 0;

            for(Text val: values) {
                all_num += Double.parseDouble(val.toString().split("-")[1].trim());
            }

            for (Text val : values) {

                String pair[] = val.toString().split(",");
                double cur_num = Double.parseDouble(pair[1].trim());
                double mean  = Double.parseDouble(pair[0].trim());
                sum += mean *(cur_num / all_num) ;
//                num += Integer.parseInt(val.toString().trim().split("-")[1]);
            }

            context.write(NullWritable.get(), new Text(String.valueOf(sum)));


        }
    }


}
