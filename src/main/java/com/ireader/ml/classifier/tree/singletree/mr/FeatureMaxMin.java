package com.ireader.ml.classifier.tree.singletree.mr;

import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


import java.io.IOException;

/**
 * Created by zxsted on 15-10-25.
 *
 *  统计样本的各个特征的最大最小值
 */
public class FeatureMaxMin {

    public static class FeatureMaxMinMapper extends Mapper<LongWritable,Text,Text,Text> {

        private  int featnum = 0;

        public void setup(Context context) {

            Configuration conf = context.getConfiguration();

            featnum = conf.getInt("DT_FEATNUM",0);
        }


        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException {

            String valstr = value.toString().trim();

            if (valstr.length() == 0) return;

            DataPoint dp = new DataPoint(valstr);

            double[] vals = dp.getFeatures().getValArr();

            if(featnum != vals.length) throw new  RuntimeException("当前record的特征长度与设置长度不同！");

            for (int i = 0 ; i < vals.length; i++) {
                context.write(new Text(String.valueOf(i)),new Text(String.valueOf(vals[i])));
            }
        }

    } // end mapper

    public static class FeatureMaxMinConsumer extends Reducer<Text,Text,Text,Text> {

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        public void reducer(Text key,Iterable<Text> values,Context context) throws IOException, InterruptedException {

            for (Text val : values) {
                double curval = Double.parseDouble(val.toString());

                if (curval > max) max = curval;
                if (curval < min) min = curval;
            }

            context.write(key, new Text(max+","+min));
        }
    } // end consumer

    public static class FeatureMaxMinReducer extends  Reducer<Text,Text,Text,Text> {

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        public void reducer(Text key,Iterable<Text> values,Context context) throws IOException, InterruptedException {

            for (Text val : values) {

                String[] fields = val.toString().trim().split(",");

                double curmax = Double.parseDouble(fields[0].trim());
                double curmin = Double.parseDouble(fields[1].trim());

                if (curmax > max) max = curmax;
                if (curmin < min) min = curmin;
            }

            context.write(key, new Text(max+","+min));
        }

    }
}
