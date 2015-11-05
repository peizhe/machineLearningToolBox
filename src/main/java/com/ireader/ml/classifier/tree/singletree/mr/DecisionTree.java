package com.ireader.ml.classifier.tree.singletree.mr;

import com.ireader.ml.classifier.tree.singletree.dto.Rule;
import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zxsted on 15-10-25.
 *
 * 决策树的统计类
 *
 */
public class DecisionTree {

    public static class DecisionTreeMapper extends Mapper<Object,Text,Text,Text> {

        private final static IntWritable one = new IntWritable(1);

        /**
         * 各个特征的特征列表（分割点）
         */
        private List<double[]> featvals = new ArrayList<double[]>();

        /**
         * 当前决策规则
         */
        private List<Rule> ruleQueue = new LinkedList<Rule>();


        public void setup(Context context) throws IOException {

            Configuration conf = context.getConfiguration();

            String featFilePath = conf.get("DT_FEAT");
            Path featPath = new Path(featFilePath);


            String ruleFilePath = conf.get("DT_RULE");
            Path rulePath = new Path(ruleFilePath);

            loadFeatFile(featPath,conf);
            loadQueueFile(rulePath, conf);
        }

        private void loadFeatFile(Path filePath, Configuration conf) throws IOException {

            FileSystem fs = FileSystem.get(conf);

            FSDataInputStream fin = null;
            BufferedReader bin = null;

            fin = fs.open(filePath);
            bin = new BufferedReader(new InputStreamReader(fin));

            String line = null;

            while ((line = bin.readLine()) != null) {

                if (line.length() == 0) continue;

                double[] curfeatvals = null;

                String[] fields = line.split(",");
                curfeatvals = new double[fields.length];

                for (int i = 0; i < fields.length; i++) {
                    curfeatvals[i] = Double.parseDouble(fields[i].trim());
                }

                featvals.add(curfeatvals);
            }

            if (bin != null) bin.close();
            if (fin != null) fin.close();
        }

        /**
         *
         * */
        private void loadQueueFile(Path filePath, Configuration conf) throws IOException {

            FileSystem fs = FileSystem.get(conf);

            FSDataInputStream fin = null;
            BufferedReader bin = null;

            fin = fs.open(filePath);
            bin = new BufferedReader(new InputStreamReader(fin));

            String line = null;

            while ((line = bin.readLine()) != null) {

                if (line.length() == 0) continue;

                Rule rule = Rule.parse(line.trim());
                ruleQueue.add(rule);
            }

            if (bin != null) bin.close();
            if (fin != null) fin.close();

            if (ruleQueue.size() == 0) {
                // 说明ruleQueue文件是空的，那么此时应该正在处理根节点
                // 我们为根节点生成一个空白的规则，加入队列
                // 确保队列非空
                ruleQueue.add(new Rule());
            }
        }


        /**
         * 判断一个样本记录是否符合规则要求
         */
        private boolean isFitRule(Rule rule, double[] aValues) {

            boolean isFit = true;

            for (String condition : rule.conditions) {

                String fields[] = condition.split(",");
                String fidstr = fields[0];
                String fvalstr = fields[1];
                String pos = fields[2].trim();

                int id = Integer.parseInt(fidstr.trim());
                double fval = Double.parseDouble(fvalstr.trim());
                double curfval = aValues[id];

                if (pos.equalsIgnoreCase("left") && curfval >= fval) {
                    isFit = false;
                    break;
                } else if (pos.equalsIgnoreCase("right") && curfval < fval) {
                    isFit = false;
                    break;
                }
            }

            return isFit;
        }


        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String line = value.toString();
            DataPoint dp = new DataPoint(line);

            double[] valArr = dp.getFeatures().getValArr();
            double label = dp.getLabel();

            /**  遍历每个规则 */
            int nid = 0;
            for (;nid < ruleQueue.size(); nid++) {
                Rule rule = ruleQueue.get(nid);

                if (isFitRule(rule,valArr)) {

                    /** 遍历每个属性看看是否在rule中出现过 */
                    for (int aid = 0; aid < valArr.length;aid++) {
                        if (!rule.contains(aid)) {      // 如果当前属性不在 RULE 中, 是一个候选的属性
                            double[] curfeatvals = featvals.get(aid);
                            for (double featval : curfeatvals) {

                                double curSamplefeatval = valArr[aid];

                                String newKey = null;

                                // 这里nid + 1 是因为 后续的 nid 计算是从 1 开始的
                                // 这里aid + 1 是因为 后续的 aid 计算是从 1 开始的
                                if (curSamplefeatval < featval) {
                                    newKey = (nid+1)+"#"+(aid+1)+","+featval +",left";
                                } else if (curSamplefeatval >= featval) {
                                    newKey = (nid+1)+"#"+(aid+1)+","+featval +",right";
                                }

                                context.write(new Text(newKey),new Text(String.valueOf(label)));
                            }
                        }
                    } // for rule
                }

            } // end for ruleQueue

        }


    } // mapper

    public static class DecisionTreeConsumer extends Reducer<Text,Text,Text,Text> {


        protected void reduce(Text key, Iterable<Text> values,Context context)
                throws IOException,  InterruptedException {
            double square = 0.0;
            double mean = 0.0;

            int count = 0;

            for (Text val : values) {
                double curval = Double.valueOf(val.toString().trim());
                square += curval * curval;
                mean += curval;
                count++;
            }

            square /= count;
            mean /= count;

            context.write(key, new Text(square + "," + mean+"," + count));

        }
    }


    public static class DecisionTreeReducer extends Reducer<Text,Text,Text,Text> {


        protected void reduce(Text key, Iterable<Text> values,Context context)
                                                       throws IOException,InterruptedException {
            double square = 0.0;
            double mean = 0.0;
            int count = 0;

            for (Text val : values) {
                count += Integer.parseInt(val.toString().trim().split(",")[2]);
            }

            for (Text val : values) {
                String[] fields = val.toString().trim().split(",");

                double curcount  = Double.parseDouble(fields[2].trim());

                square += Double.parseDouble(fields[0].trim()) * (curcount / count);
                mean += Double.parseDouble(fields[1].trim()) * (curcount / count);
            }

            context.write(key,new Text(square + "," + mean+","+count));
        }
    }
}
