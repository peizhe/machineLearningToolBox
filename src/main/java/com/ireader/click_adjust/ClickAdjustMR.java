package com.ireader.click_adjust;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-31.
 */
public class ClickAdjustMR {

    public static class ClickAdjustMapper extends Mapper<LongWritable,Text,Text,Text>{

        /**
         * input data : key : null  value:  adid  view click
         * */
        @Override
        protected void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException {

            String line = value.toString();

            String fileds[] = line.split("\\s++",2);

            context.write(new Text(fileds[0]),new Text(fileds[1]));
        }
    }



    public static class ClickAdjustReducer extends Reducer<Text,Text,Text,Text> {

        private ClickAdjust ca = new ClickAdjust();


        @Override
        protected void reduce(Text key ,Iterable<Text> values ,Context context) throws IOException,InterruptedException{

            List<Double> views = new ArrayList<Double>();
            List<Double> clicks = new ArrayList<Double>();

            for(Text val : values ) {
                String[] fields = val.toString().split("\\s+");

                views.add(Double.parseDouble(fields[0]));
                clicks.add(Double.parseDouble(fields[1]));

            }

            double alpha = 10.0;
            double belta = 1000.0;

            try {
                //
                List<Double> ret = ca.estimate(clicks,views,alpha,belta);

                context.write(key, new Text(ret.get(0).toString() + "\t" + ret.get(1).toString()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }





}
