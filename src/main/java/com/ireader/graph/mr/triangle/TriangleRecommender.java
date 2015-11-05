package com.ireader.graph.mr.triangle;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.*;

/**
 * Created by zxsted on 15-11-2.
 *
 *  三角闭合 好友推荐（无向图版）：
 *
 * 如果B和C有一个共同好友A，那么未来B和C成为好友的可能性会很大，这种叫“三角闭合”原理，
 * 我们会发现这种由朋友连接成的图为无向图，因为朋友是相互的。但是像Facebook，微博这种粉丝关系
 * 就是有向图，因为你可能follow某明星，但是明星又不会粉你，那么你和明星之间就是单向连接，就是
 * 有向图。
 *
 * 输入格式为一行，格式如下
 * <user><TAB><comma-separated list of user's friends>
 *
 * 我们想向用户U推荐还不是其好友，但是和用户U共享好友的用户，最多推送N个，已共享好友数降序排列。
 * 输出格式：
 * <user><TAB><comma-separated list of people the user may know>
 */
public class TriangleRecommender {

    public static class Map extends Mapper<LongWritable,Text,IntWritable,Text> {

        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException {

            String line = value.toString();
            String[] userAndFriends = line.split("\t");

            if (userAndFriends.length == 2) {
                String user = userAndFriends[0];
                IntWritable userKey = new IntWritable(Integer.parseInt(user));

                String[] friends = userAndFriends[1].split(",");
                String friend1;

                IntWritable friend1Key = new IntWritable();
                Text friend1Value = new Text();
                String friend2;
                IntWritable friend2Key = new IntWritable();
                Text friend2Value = new Text();

                // 遍历 用户的朋友列表， 输出 直接朋友 （用于在reducer 中 过滤 朋友列表中是直接朋友的） ， 和间接朋友 对
                for (int i = 0 ; i < friends.length; i++) {
                    friend1 = friends[i];
                    friend1Value.set("1," + friend1);
                    context.write(userKey, friend1Value);          // paths of length 1 ， 即直接朋友

                    friend1Key.set(Integer.parseInt(friend1));
                    friend1Value.set("2," + friend1);
                    for (int j = i+1; j< friends.length;j++) {
                        friend2 = friends[j];
                        friend2Key.set(Integer.parseInt(friend2));
                        friend1Value.set("2,"+friend2);
                        context.write(friend1Key, friend2Value);   // Paths of length 2 ， 即间接朋友
                        context.write(friend2Key,friend1Value);    // Paths of length 2 ， 即间接朋友

                    }
                }
            }
        }
    }


    /**
     *  找出一个用户的 即间接朋友列表中有共同朋友个数最多的 MAX_RECOMMENDATION_COUNT 个
     * */
    public static class Reduce extends Reducer<IntWritable,Text,IntWritable,Text> {

        public void reduce(IntWritable key, Iterable<Text> values,Context context)
                                                throws IOException,InterruptedException {

            String[] value;
            HashMap<String,Integer> hash = new HashMap<String,Integer>();

            for (Text val: values) {
                value = (val.toString()).split(",");
                if (value[0].equals("1")) {    // Paths of length 1  即直接朋友
                    hash.put(value[1], -1);
                } else if (value[0].equals("2")) {  // Paths of length 2 即间接朋友
                    if (hash.containsKey(value[1])) {
                        if (hash.get(value[1])!=-1) {    // 如果已经是直接朋友那么跳过
                            hash.put(value[1],hash.get(value[1]) + 1);   // 如果是  间接朋友 那么 相应的计数增加
                        }
                    } else {
                        hash.put(value[1],1);
                    }
                }
            }


            // Convert hash to list and remove paths of length 1
            ArrayList<java.util.Map.Entry<String,Integer>> list = new ArrayList<java.util.Map.Entry<String,Integer>>();

            for (java.util.Map.Entry<String,Integer> entry : hash.entrySet()) {
                if (entry.getValue() != -1) {   // 如果 path 为 1 ，那么排除
                    list.add(entry);
                }
            }

            // 根据 value（共同朋友的个数） 来随 key - value 对进行排序，
            Collections.sort(list, new Comparator<java.util.Map.Entry<String, Integer>>() {
                @Override
                public int compare(java.util.Map.Entry<String, Integer> o1, java.util.Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            int MAX_RECOMMENDATION_COUNT = 10;

            if (MAX_RECOMMENDATION_COUNT < 1 ) {
                context.write(key,new Text(StringUtils.join(list, ",")));
            } else {
                ArrayList<String> top = new ArrayList<String>();

                for (int i = 0 ; i < Math.min(MAX_RECOMMENDATION_COUNT,list.size());i++) {
                    top.add(list.get(i).getKey());
                }

                context.write(key,new Text(StringUtils.join(top,",")));
            }
        }
    }


    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        Job job = new Job(conf,"TriangleRecommender");



        job.setJarByClass(TriangleRecommender.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        org.apache.hadoop.mapreduce.lib.input.FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}

























