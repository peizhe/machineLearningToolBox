package com.ireader.ml.classifier.knnclassifier;

import com.ireader.util.distance.EuclideanDistance;
import com.ireader.ml.common.struct.ListWritable;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zxsted on 15-7-27.
 */
public class KNearestNeighbour {

    public static class KNNMap extends Mapper<LongWritable,
                Text,LongWritable,ListWritable<DoubleWritable>> {
        private int k;
        private ArrayList<Instance> trainSet;
        private EuclideanDistance Eucldistance =new  EuclideanDistance<Double>();

        @Override
        protected void setup(Context context) throws IOException,InterruptedException{

            k = context.getConfiguration().getInt("k",1);
            trainSet = new ArrayList<Instance>();

            // 从分布式缓存中 加载样本点
            Path[] trainFile = DistributedCache.getLocalCacheFiles(context.getConfiguration());

            BufferedReader br = null;
            String line ;
            for (int i = 0; i < trainFile.length ; i++) {
//                br = new BufferedReader(new FileReader(trainFile[0].toString()));
                br = new BufferedReader(new FileReader("input"));
                while((line = br.readLine()) != null) {
                    Instance trainInstance = new Instance(line);
                    trainSet.add(trainInstance);
                }
            }
        }


        // map
        @Override
        public void map(LongWritable textIndex,Text textLine,Context context)
            throws IOException,InterruptedException {

            //

            ArrayList<Double> distance = new ArrayList<Double>(k);
            ArrayList<DoubleWritable> trainLable = new ArrayList<DoubleWritable>();

            for (int i = 0; i < k ; i++) {
                distance.add(Double.MAX_VALUE);
                trainLable.add(new DoubleWritable(-1.0));
            }

            ListWritable<DoubleWritable> labels = new ListWritable(DoubleWritable.class);

            Instance testInstance = new Instance(textLine.toString());
            for (int i = 0 ; i < trainSet.size(); i++) {

                // new ArrayList<Double>().trainSet.get(i).getAttributeValue()), testInstance.getAttributeValue()
                ArrayList<Double> trainInst = new ArrayList<Double>();
                ArrayList<Double> testInst  = new ArrayList<Double>();
                for  (double val : trainSet.get(i).getAttributeValue()) {
                    trainInst.add(val);
                }

                for(double val : testInstance.getAttributeValue() ) {
                    testInst.add(val);
                }
                try{
                    double dis = Eucldistance.getDistance(trainInst,testInst);
                    int index = indexOfMax(distance);
                    if (dis < distance.get(index)) {
                        distance.remove(index);
                        distance.add(dis);
                        trainLable.add(new DoubleWritable(trainSet.get(i).getLabel()));
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            labels.setList(trainLable);
            context.write(textIndex,labels);

        }


        public int indexOfMax(ArrayList<Double> array) {
            int index = -1;
            Double min = Double.MIN_VALUE;
            for (int i = 0; i < array.size(); i++) {

                if (array.get(i) > min) {
                    min = array.get(i);
                    index = 1;
                }
            }

            return index;
        }
    }


    public static class KNNReduce extends Reducer<LongWritable,ListWritable<DoubleWritable>,NullWritable ,DoubleWritable>{

        @Override
        protected void reduce(LongWritable index, Iterable<ListWritable<DoubleWritable>> values,
                           Context context) throws IOException, InterruptedException {
            DoubleWritable predictedLabel = new DoubleWritable();
            for(ListWritable<DoubleWritable> val: values) {
                try {
                    predictedLabel = valueOfMostFrequent(val);
                    break;
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            context.write(NullWritable.get(),predictedLabel);
        }

        public DoubleWritable valueOfMostFrequent(ListWritable<DoubleWritable> list) throws Exception {

            if (list.getList().isEmpty()) {
                throw new Exception("list is empty!");
            } else {

                HashMap<DoubleWritable,Integer> tmp = new HashMap<DoubleWritable,Integer>();

                for(int i = 0 ; i < list.getList().size(); i++) {
                    if(tmp.containsKey(list.getList().get(i))) {
                        Integer frequence = tmp.get(list.getList().get(i)) + 1;
                        tmp.remove(list.getList().get(i));
                        tmp.put(list.getList().get(i),frequence);
                    } else {
                        tmp.put(list.getList().get(i),new Integer(1));
                    }

                }

                // 找到最大频率
                DoubleWritable value = new DoubleWritable();
                Integer frequence = new Integer(Integer.MIN_VALUE);

                Iterator<Map.Entry<DoubleWritable,Integer>> iter = tmp.entrySet().iterator();

                while(iter.hasNext()) {
                    Map.Entry<DoubleWritable,Integer> entry = (Map.Entry<DoubleWritable,Integer>) iter.next();

                    if (entry.getValue() > frequence) {
                        frequence = entry.getValue();
                        value = entry.getKey();
                    }
                }
                return value;
            }

        }
        }


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Job KNNJob = new Job();

        KNNJob.setJobName("KNNJob");
        KNNJob.setJarByClass(KNearestNeighbour.class);

        // 将训练数据添加到分布式缓存中
        DistributedCache.addCacheFile(URI.create(args[2]+"/#input" ), KNNJob.getConfiguration());
        KNNJob.getConfiguration().setInt("k", Integer.parseInt(args[3]));

        KNNJob.setMapperClass(KNNMap.class);
        KNNJob.setMapOutputKeyClass(LongWritable.class);
        KNNJob.setMapOutputValueClass(ListWritable.class);

        KNNJob.setReducerClass(KNNReduce.class);
        KNNJob.setOutputKeyClass(NullWritable.class);
        KNNJob.setOutputValueClass(DoubleWritable.class);

        KNNJob.setInputFormatClass(TextInputFormat.class);
        KNNJob.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(KNNJob, new Path(args[0]));
        FileOutputFormat.setOutputPath(KNNJob, new Path(args[1]));

        KNNJob.waitForCompletion(true);
        System.out.println("job finished!");

    }
    }




