package com.ireader.ml.classifier.tree.singletree.mr;

import com.ireader.ml.classifier.tree.singletree.dto.Ensemble;
import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by zxsted on 15-10-25.
 */
public class UpdateLabel {

    public static class UpdateLabelMapper extends Mapper<Object,Text,Text,Text> {


        Ensemble ensemble = null;

        Configuration conf = null;

//        double lrate = 0.0;

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

            double adjustlabel = label -  predict;

            dp.setLabel(adjustlabel);

            // 输出预测结果 ： key ： 调整label值后输入的一行数据  value : 该数据的预测值
            context.write(new Text(dp.toString()),new Text(String.valueOf(predict)));
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
}
