package com.ireader.ml.classifier.tree.singletree.mr;

import com.ireader.ml.classifier.tree.singletree.dto.Ensemble;
import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by zxsted on 15-11-3.
 */
public class Cost {
    public static class CostMapper extends Mapper<Object,Text,NullWritable,Text> {


        Ensemble ensemble = null;

        Configuration conf = null;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);

            conf = context.getConfiguration();

            // 取出模型的地址
            String modelPath = conf.get("ENSEMBLE_PATH");

            String modelString = loadModel(modelPath);
            ensemble = new Ensemble();
            ensemble.load(modelString);

        }

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            DataPoint dp = new DataPoint(value.toString());

            double predict = ensemble.predict(dp);

            double label = dp.getLabel();

            double adjustlabel = label - predict;


            // 输出预测结果 ： key ： 调整label值后输入的一行数据  value : 该数据的预测值
            context.write(NullWritable.get(),new Text(String.valueOf(adjustlabel * adjustlabel)));
        }


        private String loadModel(String modelpath) throws IOException {

            FileSystem fs = FileSystem.get(this.conf);
            Path modelPath =  new Path(modelpath);

            FSDataInputStream iStream = fs.open(modelPath);

            StringBuffer sb = new StringBuffer();
            Scanner scanner = new Scanner(iStream);

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line+"\n");
            }

            scanner.close();
            iStream.close();

            return sb.toString();
        }


    } // Mapper

    public static class CostCombimer extends Reducer<NullWritable,Text,NullWritable,Text> {

        @Override
        public void reduce(NullWritable key,Iterable<Text> values , Context context) throws IOException, InterruptedException {
            int count = 0;

            double mse = 0.0;
            for (Text val : values) {
                count++;
                mse += Double.parseDouble(val.toString().trim());
            }

            String valstr = String.valueOf(mse / count) + "," + String.valueOf(count);
            context.write(NullWritable.get(), new Text(valstr));
        }
    }

    public static class CostReducer extends Reducer<NullWritable,Text,NullWritable,Text> {

        @Override
        public void reduce(NullWritable key,Iterable<Text> values,Context context) throws IOException, InterruptedException {

            long count = 0;
            double mse = 0.0;
            for (Text value : values) {
                count += Integer.parseInt(value.toString().split(",")[1]);
            }

            for (Text value : values) {
                String[] valstr = value.toString().split(",");
                int curcount = Integer.parseInt(valstr[1]);
                double curmse = Double.parseDouble(valstr[0]);

                mse += curmse * (curcount / count);
            }

            context.write(NullWritable.get(), new Text(String.valueOf(mse)));
        }
    }
}
