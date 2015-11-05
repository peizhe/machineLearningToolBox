package com.ireader.matrix.multiply;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import com.ireader.matrix.multiply.writable.Key;
import com.ireader.matrix.multiply.writable.Value;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zxsted on 15-9-1.
 */
public class MatrixMultiply {



    public static class FirstStepMapper extends Mapper<LongWritable,Text,Key,Value>
    {
        private int m;
        private int p;
        private int s;
        private int t;
        private int v;
        private String  leftflag;

        private int mPerS;
        private int pPerV;

//        private MultipleOutputs<Text,IntWritable> mos;
        @Override
        protected void setup(Context context) {
            Configuration conf = context.getConfiguration();

//            mos = new MultipleOutputs(context);

            m = conf.getInt("m",0);
            p = conf.getInt("p",0);
            s = conf.getInt("s",0);
            t = conf.getInt("t",0);
            v = conf.getInt("v",0);
            leftflag = conf.get("leftflag", "left");
            mPerS = m/s;
            pPerV = p/v;
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException,
                InterruptedException {
            String line = value.toString();
            String[] fields = line.split("\\s+");

            Key outputkey = new Key();
            Value outputValue = new Value();


            if(fields[0].equalsIgnoreCase(leftflag)) {   // 如果是 A 矩阵

                int i = Integer.parseInt(fields[1]);
                int j = Integer.parseInt(fields[2]);

                for(int kPerV = 0 ;kPerV < pPerV; kPerV++) {
                    outputkey.index1 = i/s;        // block 索引
                    outputkey.index2 = j/t;
                    outputkey.index3 = kPerV;

                    outputValue.index1 = i%s;      // block 内索引
                    outputValue.index2 = j%t;
                    outputValue.v = Double.parseDouble(fields[3]);
                    outputValue.flag = "A";

                    context.write(outputkey,outputValue);
                }
            } else {

                int j = Integer.parseInt(fields[1]);
                int k = Integer.parseInt(fields[2]);

                for(int iPerS = 0 ; iPerS < mPerS; iPerS++){
                    outputkey.index1 = iPerS;
                    outputkey.index2 = j / t;
                    outputkey.index3 = k / v;

                    outputValue.flag = "B";
                    outputValue.index1 = j %t;
                    outputValue.index2 = k % v;
                    outputValue.v  = Double.parseDouble(fields[3]);

                    context.write(outputkey,outputValue);
                }

            }
        }

    }


    public static class FirstStepReducer extends Reducer<Key,Value,Text,Text> {

        private int m;
        private int p;
        private int s;
        private int t;
        private int v;
        private String  leftflag;

        private int mPerS;
        private int pPerV;

        @Override
        protected void setup(Context context){

            Configuration conf = context.getConfiguration();

            m = conf.getInt("m",0);
            p = conf.getInt("p",0);
            s = conf.getInt("s",0);
            t = conf.getInt("t",0);
            v = conf.getInt("v",0);
            leftflag = conf.get("leftflag", "left");
            mPerS = m/s;
            pPerV = p/v;

        }

        @Override
        protected void reduce(Key key,Iterable<Value> values , Context context) throws IOException,
                InterruptedException {
            ArrayList<Map.Entry<String,Double>> listA = new ArrayList<Map.Entry<String,Double>>();
            ArrayList<Map.Entry<String,Double>> listB = new ArrayList<Map.Entry<String,Double>>();

            for(Value val : values) {
                if(val.flag.equals("A")) {
                    listA.add(new AbstractMap.SimpleEntry<String,Double>(val.index1+","+val.index2,val.v));
                } else {
                    listB.add(new AbstractMap.SimpleEntry<String,Double>(val.index1+","+val.index2,val.v));
                }
            }

            String[] iModSAndJModT;
            String[] jModTAndKModV;

            double a_ij;
            double b_jk;

            String hashKey;
            HashMap<String,Double> hash = new HashMap<String,Double>();
            for(Map.Entry<String, Double> a : listA) {
                iModSAndJModT = a.getKey().split(",");
                a_ij = a.getValue();

                for(Map.Entry<String,Double> b: listB) {
                    jModTAndKModV = b.getKey().split(",");
                    b_jk = b.getValue();

                    if(iModSAndJModT[1].equalsIgnoreCase(jModTAndKModV[0])) {
                        hashKey = iModSAndJModT[0] + "," + jModTAndKModV[1];

                        if(hash.containsKey(hashKey)) {
                            hash.put(hashKey,hash.get(hashKey) + a_ij*b_jk);
                        } else {
                            hash.put(hashKey,a_ij * b_jk);
                        }
                    }
                }
            }


            String[] blockIndices = key.toString().split(",");
            String[] indices;
            String i;
            String k;

            Text outputkey = new Text();
            Text outputValue = new Text();

            for(Map.Entry<String,Double> entry:hash.entrySet()) {
                indices = entry.getKey().split(",");
                i = Integer.toString(Integer.parseInt(blockIndices[0]) * s + Integer.parseInt(indices[0]));
                k = Integer.toString(Integer.parseInt(blockIndices[2]) * v + Integer.parseInt(indices[1]));

                outputkey.set(i + "," + k);
                outputValue.set(Double.toString(entry.getValue()));
                context.write(outputkey, outputValue);
            }
        }
    }



    public static class DoubleSumReducer extends Reducer<Text,Text,Text,DoubleWritable> {

        protected void reduce(Text key,Iterable<Text> values ,Context context) throws IOException,
                InterruptedException {
            double ret = 0.0;

            for (Text val : values ) {
                ret += Double.parseDouble(val.toString().trim());
            }

            context.write(key,new DoubleWritable(ret));
        }
    }



}























