package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.math.FuncUtil;
import com.ireader.ml.common.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-24.
 *
 *  map ： 计算单个样本的梯度
 *
 *  reduce ： 累加各个样本计算的 梯度，并同时更新 权重
 */
public class UpdateWeight {

    public static class UpdateWeightMapper extends Mapper<NullWritable,Text,IntWritable,Text> {

        @Override
        public void  map(NullWritable key, Text val,Context context) throws IOException,InterruptedException{

            WDataPoint value = new WDataPoint(val.toString());
            double product = value.getWeight();
            double label = value.getSub_dp().getLabel();

            double factor = label * (FuncUtil.sigmoid(product* label) -1 );


            for (int i = 0 ; i < value.getFeat().size(); i++){
                double gradval= factor * value.getFeat().get(i);
                context.write(new IntWritable(i), new Text(String.valueOf(gradval)));
            }

        }
    }



    public static class UpdateWeightCombiner extends Reducer<IntWritable,Text,IntWritable,Text>{

        @Override
        public void reduce(IntWritable key , Iterable<Text> values , Context context)
                                                throws  IOException,InterruptedException{

            double num = 0;
            double sum = 0.0;
            for (Text val: values ) {
                sum += Double.parseDouble(val.toString());
                num++;
            }

            context.write(key, new Text((sum/num) + "," + num));

        }

    }


    public static class UpdateWeightReducer extends Reducer<IntWritable,Text,IntWritable,DoubleWritable>{

        private ArrayList<Double> weights = new ArrayList<Double>();

        private double step = 0.0;

        private String regType = "L2";

        private double lambda = 0.0;


        protected void setup(Context context)  throws IOException ,InterruptedException{

            Configuration conf = context.getConfiguration();

            String weightfile = conf.get("LLR_WeightSavePath");
            int featnum = conf.getInt("LLR_FeatNum",-1);
            step = conf.getDouble("LLR_LearnRate", 0.01);          // 更新步长
            regType = conf.get("LLR_RegType","L2");
            lambda = conf.getDouble("LLR_Lambda", 0.0);


            Path wfile = new Path(weightfile);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            FSDataInputStream fsi =null;
            BufferedReader in = null;

            if(fs.isFile(wfile)) {
                fsi = fs.open(wfile);
                String line = null;
                in  = new BufferedReader(new InputStreamReader(fsi,"utf-8"));

                while((line = in.readLine()) != null) {
                    if(!(featnum == StringUtil.StringToList(weights, line.trim(),"\t"))){
                        throw new IOException("load weights number and featnum is not same!");
                    }
                }
            }

            in.close();
            fsi.close();
        }

        @Override
        public void  reduce(IntWritable key,Iterable<Text> values,Context context)
             throws IOException,InterruptedException {

            double all_num = 0.0;
            double sum = 0.0;
            // 先统计样本总数
            for(Text val : values) {
                all_num += Double.parseDouble(val.toString().split(",")[1].trim());
            }

            for (Text val: values ) {
                String fields[] = val.toString().trim().split(",");
                double cur_num =  Double.parseDouble(fields[1].trim());
                double mean = Double.parseDouble(fields[0].trim());

                sum += mean *(cur_num / all_num);
            }

            int colno = key.get();

            double weight = weights.get(colno);

            // 正则化项
            double regItem = 0.0;
            if(regType.equalsIgnoreCase("L2"))
                regItem = 0.5*lambda * weight;
            else if(regType.equalsIgnoreCase("L1")) {
                if (0 == weight)
                    regItem = 0.0;
                else
                    regItem = lambda * ((weight > 0) ? 1 : -1);
            }


            double updateW = weight - step * sum + regItem;     // 更新权重

            context.write(key, new DoubleWritable(updateW));

        }
    }
}
