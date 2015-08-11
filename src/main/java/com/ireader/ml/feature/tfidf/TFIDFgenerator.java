package com.ireader.ml.feature.tfidf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by zxsted on 15-7-31.
 *
 *  对份好词的数据集， 计算TF-IDF，后期可以考虑将分词器集成进来， 对原始文本直接使用
 */
public class TFIDFgenerator {


    // 计算TF

    public static class TFMapper extends Mapper<LongWritable,Text,Text,Text> {

        String File_name = "";   // 保存文件名， 根据文件名进行分区
        int all = 0;    // 统计单词总数
        static Text one = new Text("1");
        String word ;

        public void map(LongWritable key,Text value,Context context) throws IOException,InterruptedException
        {
            FileSplit split = (FileSplit) context.getInputSplit();
            String str = split.getPath().toString();
            File_name = str.substring(str.lastIndexOf("/") +1); // 获取文件名
            StringTokenizer itr = new StringTokenizer(value.toString());
            while(itr.hasMoreTokens()) {
                word = File_name;
                word += " ";
                word += itr.nextToken();    // 将文件名加单词作为key
                all++;
                context.write(new Text(word),one);
            }

        }

        public void cleanup(Context context) throws IOException,InterruptedException{
            // map 的最后， 我们将单词的总数写入，

            String str = "";
            str += all;
            context.write(new Text(File_name+ " " + "!") , new Text(str));
            // 注意： 这里值使用的 "!"是特别构造的。 因为!的ascii比所有的字母都小。
        }

    }

    public static class TFCombiner extends Reducer<Text,Text,Text,Text>{
        float all = 0;

        public void reduce(Text key, Iterable<Text> values,Context context ) throws IOException,
                InterruptedException {
            int index = key.toString().indexOf(" ");
            // 因为!的ascii最小，所以在map阶段的排序后，!会出现在第一个
            if (key.toString().substring(index+1,index+2).equals("!")){
                for (Text val : values) {
                    // 获取单词总数
                    all = Integer.parseInt(val.toString());
                }
                // 这个key value 被抛弃
                return ;
            }

            float sum = 0;    // 统计某个单词的个数
            for (Text val : values) {
                sum += Integer.parseInt(val.toString());
            }

            // 跳出循环后，某个单词出现的次数就统计完了， 所有的TF = sum / all
            float tmp = sum / all;
            String value = "";
            value += tmp; // 记录词频

            // 将key 中单词和文件名进行呼唤， ： test1 hello -> hello test1
            String p[] = key.toString().split(" ");
            String key_to =" ";

            key_to += p[1];
            key_to += " ";
            key_to += p[0];

            context.write(new Text(key_to),new Text(value));
         }
    }

    public static class TFReducer extends Reducer<Text,Text,Text,Text> {

        @Override
        public void reduce(Text key,Iterable<Text> values,Context context) throws IOException,
                InterruptedException {
            for (Text val :  values ) {
                context.write(key,val);
            }
        }
    }

    public static class TFIDFPartitoner extends Partitioner<Text,Text> {

        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            // 将一个文件中的计算结果作为一个文件保存
            String ip1 = key.toString();
            ip1 = ip1.substring(0,ip1.indexOf(" "));
            Text p1 = new Text(ip1);
            return Math.abs((p1.hashCode()*127) % numPartitions);
        }
    }



    // IDF

    public static class IDFMapper extends Mapper<LongWritable,Text,Text,Text> {

        @Override
        public void map(LongWritable key,Text value,Context context) throws IOException,
                    InterruptedException {

            String val = value.toString().replaceAll("  ", " ") ; //  将vlaue中的TAB分割符换成空格 es: Bank test1    0.11764706 -> Bank test1 0.11764706

            int index = val.indexOf(" ");
            String s1 = val.substring(0, index); // 获取单词作为key
            String s2 = val.substring(index + 1); // 其余部分作为value

            s2 += " ";
            s2 += "1"; // 统计单词在所有文章中出现的次数， "1" 表示出现1，
            context.write(new Text(s1) , new Text(s2));

        }
    }

    public static class IDFReducer extends Reducer<Text,Text,Text,Text> {

        int file_count = 0;
        @Override
        public void reduce(Text key,Iterable<Text> values ,Context context)
             throws IOException,InterruptedException{

            // 同一个单词会被分成同一个group
            file_count = context.getNumReduceTasks();   // 获总的文件个数
            float sum = 0;

            List<String> vals = new ArrayList<String>();

            for (Text str : values) {
                int index = str.toString().lastIndexOf(" ");

                sum += Integer.parseInt(str.toString().substring(index+1));
                vals.add(str.toString().substring(0,index)); // 文件名
            }

            float tmp = sum / file_count;  // 单词在所有文件中出现的次数

            for(int j = 0 ; j < vals.size(); j++) {
                String val = vals.get(j);
                String end = val.substring(val.lastIndexOf(" "));
                float tf  = Float.parseFloat(end);

                val += " ";
                val += tmp;   // cf
                val += tf * Math.log( 1.0 / tmp) ; // tf - idf

                context.write(key, new Text(val));
            }
        }
    }


    public static void main(String[] args) throws Exception{

        Path tmp= new Path("tmp/TFIDF");    // 设置中文间临时存储目录

        // part1------------------------------------------------
        Configuration conf1 = new Configuration();
        // 设置文件个数， 在计算DF（文件频率）使用
        FileSystem fs = FileSystem.get(conf1);
        FileStatus p[] = fs.listStatus(new Path[1]);

        // 获取输入文件夹中文件的个数， 然后来设置NumReduceTasks
//        Job TFJob = new Job(conf1,"TFjob");
        Job TFJob = Job.getInstance(conf1,"TFjob");

        TFJob.setJarByClass(TFIDFgenerator.class);
        TFJob.setMapperClass(TFMapper.class);
        TFJob.setCombinerClass(TFCombiner.class);
        TFJob.setReducerClass(TFReducer.class);

        TFJob.setMapOutputKeyClass(Text.class);
        TFJob.setMapOutputValueClass(Text.class);
        TFJob.setOutputKeyClass(Text.class);
        TFJob.setOutputValueClass(Text.class);

        TFJob.setNumReduceTasks(p.length);

        TFJob.setPartitionerClass(TFIDFPartitoner.class);

        FileInputFormat.addInputPath(TFJob, new Path(args[1]));
        FileOutputFormat.setOutputPath(TFJob, tmp);

        boolean success = TFJob.waitForCompletion(true);
        if(success)
            System.out.println("TF compute completed !");
        else{
            System.out.println("TF compute faild as TF step!");
            System.exit(1);
        }


        // IDF -------------------------------------------------------

        Configuration conf2 = new Configuration();

        Job IDFJob = new Job(conf2,"IDFJob");

        IDFJob.setJarByClass(TFIDFgenerator.class);

        IDFJob.setMapperClass(IDFMapper.class);
        IDFJob.setReducerClass(IDFReducer.class);

        IDFJob.setMapOutputKeyClass(Text.class);
        IDFJob.setMapOutputValueClass(Text.class);

        IDFJob.setOutputKeyClass(Text.class);
        IDFJob.setOutputValueClass(Text.class);

        IDFJob.setNumReduceTasks(p.length);

        FileInputFormat.setInputPaths(IDFJob,tmp);
        FileOutputFormat.setOutputPath(IDFJob,new Path(args[2]));

        success |= IDFJob.waitForCompletion(true);


        if(success) {
            System.out.println("TFIDF compute completed!");
            fs.delete(tmp,true);
        } else {
            System.out.println("TFIDF compute faild at IDF step!");
        }


    }


}
