package com.ireader.ml.optimize;

import com.ireader.local.ann.dto.DataPoint;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-10.
 */
public class optimizer {

//    public static class optimizerMapper extends Mapper<LongWritable,Text,IntWritable,Text>{
//
//        private List<Double> weightVec = new ArrayList<Double>();
//        /**
//         * 读取 模型的权重向量
//         */
//        protected void setup(Context context) {
//
//        }
//
//        protected void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException{
//
//            Grident gradient = new ;
//
//            DataPoint cur_dp = new DataPoint(value.toString());
//            List<Double> weight =gradient.gradient(cur_dp , weightVec);
//            double loss = gradient.loss(cur_dp , weightVec);
//
//            for(int i = 0; i < weight.size(); i++) {
//                context.write(new IntWritable(i),new Text(weight.get(i)+","+loss));
//            }
//
//        }




//    }
}
