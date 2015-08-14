package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.math.FuncUtil;
import com.ireader.ml.common.struct.DataPoint;
import com.ireader.ml.common.struct.DoubleVector;
import com.ireader.ml.common.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-24.
 *
 * 使用训练完成的参数， 进行预测
 */
public class LRpredictor {

    public static class LRpredictorMapper extends Mapper<LongWritable,Text,Text,DoubleWritable> {

        private ArrayList<Double> weights = new ArrayList<Double>();
        private DoubleVector weightvec = null;
        @Override
        protected void setup(Context context) throws IOException,InterruptedException{

            Configuration conf = context.getConfiguration();

            String weightfile = conf.get("LLR_WeightSavePath");
            int featnum = conf.getInt("LLR_FeatNum", -1);

            Path wfile = new Path(weightfile);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            FSDataInputStream fsi =null;
            BufferedReader in = null;

            if(fs.isFile(wfile)) {
                fsi = fs.open(wfile);
                String line = null;
                in  = new BufferedReader(new InputStreamReader(fsi,"utf-8"));

                while((line = in.readLine()) != null) {
                    if(!(featnum == StringUtil.StringToList(weights, line.trim(), "\t"))){
                        throw new IOException("load weights number and featnum is not same!");
                    }
                }

                weightvec = new DoubleVector(weights);
            }

            in.close();
            fsi.close();

        }

        @Override
        public void map(LongWritable key ,Text value,Context context) throws IOException,InterruptedException{

            String curline = value.toString();

            if(curline.split("\\s+").length < 4) return;

            DataPoint dp = new DataPoint(curline);

            double pred = dp.getFeatures().innerMul(weightvec);

            pred = FuncUtil.sigmoid(pred);

            String pointcontent = dp.toString();


             context.write(new Text(pointcontent),new DoubleWritable(pred));
        }


    }
}
