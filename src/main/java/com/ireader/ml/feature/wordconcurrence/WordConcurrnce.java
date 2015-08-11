package com.ireader.ml.feature.wordconcurrence;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zxsted on 15-8-6.
 */
public class WordConcurrnce {

    private static int MAX_WINDOW = 20;   // 滑动窗口

    private static String wordRegex = "([a-zA-Z]{1,})";
    private static Pattern wordPattern = Pattern.compile(wordRegex);   // 提取英文字母
    private static IntWritable one = new IntWritable(1);

    public static class WordConCurrenceMapper extends Mapper<Text,BytesWritable,WordPair,IntWritable>{

        private int windowSize;
        private Queue<String> windowQueue = new LinkedList<String>();

        @Override
        protected void setup(Context context) throws IOException,InterruptedException{
            windowSize = Math.min(context.getConfiguration().getInt("window",2),MAX_WINDOW);

        }

        @Override
        public void map(Text docName,BytesWritable docContent,Context context) throws IOException, InterruptedException {

            Matcher matcher = wordPattern.matcher(new String(docContent.getBytes(),"UTF-8"));

            while(matcher.find()) {
                windowQueue.add(matcher.group());
                if(windowQueue.size() >= windowSize) {
                    Iterator<String> it = windowQueue.iterator();
                    String w1 = it.next();
                    while(it.hasNext()) {
                        String next = it.next();
                        context.write(new WordPair(w1,next),one);
                    }

                    windowQueue.remove();
                }
            }

            if(!(windowQueue.size() <= 1 )) {
                Iterator<String> it = windowQueue.iterator();
                String w1 = it.next();
                while(it.hasNext()){
                    context.write(new WordPair(w1,it.next()),one);
                }
            }
        }
    }


    public static class WordConcurrenceReducer extends Reducer<WordPair,IntWritable,WordPair,IntWritable> {

        @Override
        public void reduce(WordPair wordPair,Iterable<IntWritable> frequence,Context context) throws IOException, InterruptedException {
            int sum = 0;
            for(IntWritable val:frequence){
                sum += val.get();
            }

            context.write(wordPair,new IntWritable(sum));
        }
    }

}


























