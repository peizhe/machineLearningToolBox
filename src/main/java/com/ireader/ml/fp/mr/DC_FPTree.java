package com.ireader.ml.fp.mr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.ireader.ml.fp.local.FPTree;
import com.ireader.ml.fp.local.Pair;



public class DC_FPTree extends Configured implements Tool{
	public static final Log LOG = LogFactory.getLog(DC_FPTree.class);
	private static  int GroupNum = 500;
	private static  int minsuport = 6;
	
	
	/*===================频繁1项集统计部分==================================================*/
	public static class Freq_Mapper extends Mapper<LongWritable,Text,Text,IntWritable> {
		@Override
		public void setup(Context context) {
			DC_FPTree.minsuport = context.getConfiguration().getInt("FP_Growth_min_sup",6);
		}
		
		@Override
		public void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException {
			String raw_line = value.toString();
			String[] items = raw_line.split("\\s+");
			
			for (int i = 0; i < items.length; i ++) {
				context.write(new Text(items[i]),new IntWritable(1));
			}
		}
	}
	
	public static class Freq_Reducer extends Reducer<Text,IntWritable,Text,IntWritable> {
		private int min_sup;
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			this.min_sup = conf.getInt("FP_Growth_min_sup", 3);
		}
		@Override
		public void reduce(Text key,Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
			Iterator<IntWritable> it = values.iterator();
			int sum = 0;
			while(it.hasNext()) {
				sum += it.next().get(); 
			}
			if(sum > this.min_sup)   {     //如果大于最小支持度则输出
				context.write(key, new IntWritable(sum));
			}
		}
	}
	
	/*===================分布式FP_Growth算法的主job==========================================*/
	
	/**
	 * 数据分发mapper类， 用于根据频繁项分组向各个reduce分发冗余数据
	 * */
	public static class GroupMapper extends 
										Mapper<LongWritable,Text,IntWritable,Record> {
		
		List<String> freq = new LinkedList<String> ();    //频繁1项集
		List<List<String>> freq_group = new LinkedList<List<String>> ();   //  分组后的频繁1项集
		
		@Override
		public void setup(Context context) throws IOException {
			DC_FPTree.GroupNum = context.getConfiguration().getInt("dataset_group_num",300);
			DC_FPTree.minsuport = context.getConfiguration().getInt("FP_Growth_min_sup",6);
			//从hdfs文件中读入频繁1项集
			FileSystem fs = FileSystem.get(context.getConfiguration());
			String sotedFilePath = context.getConfiguration().get("sorted_item_file");
			Path freqFile = new Path(sotedFilePath);
			FSDataInputStream in = fs.open(freqFile);
			InputStreamReader isr  = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			try{
				String line;
				while((line = br.readLine()) != null) {
					String[] strs = line.split("\\s+");
					String word = strs[0];
					freq.add(word);
				}
			}finally{
				br.close();
			}
			LOG.info("read line of file is :" + freq.size());
			//对1频繁项集进行分组
			Collections.shuffle(freq);                     //乱序
			int cap = freq.size() / GroupNum;   //每段分为一组 
			for (int i = 0; i < GroupNum ; i ++) {
				List<String> list = new LinkedList<String> ();
				for(int j = 0; j < cap; j++) {
					list.add(freq.get(i * cap + j));
				}
				freq_group.add(list);
			}
			
			int remainder = freq.size() % GroupNum ;
			int base = GroupNum * cap;
			for(int i =0 ; i < remainder; i++) {              //每组的末尾再加上一个剩余元素
				freq_group.get(i).add(freq.get(base + i));
			}
			
			LOG.info("group length is :" +freq_group.size()+ " \t one record is :" + freq_group.get(2).get(2));
		}// setup end
		
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] arr  = value.toString().split("\\s+");
			Record record = new Record(arr);
			LinkedList<String> list = record.list;
			BitSet bs = new BitSet(freq_group.size());                                      //位集
			bs.clear();
			while(record.list.size() > 0) {
				String item = list.peekLast();                    //取出record的最后一项
				int i=0;
				for(;i < freq_group.size();i++) {
					if(bs.get(i))
						continue;
					if(freq_group.get(i).contains(item)) {
						bs.set(i);
						break;    //跳出for循环
					}
				}
				if(i < freq_group.size()) {
					context.write(new IntWritable(i),record);
				}
				record.list.pollLast();
			}
		}
		
	}//  static class end
	
	
	public static class FPReducer extends Reducer<IntWritable,Record ,IntWritable,Text> {
		
		private FPTree fptree = null;
		
		@Override
		public void setup(Context context) {
			fptree = new FPTree();
			fptree.setMinSuport(context.getConfiguration().getInt("FP_Growth_min_sup", 20));   //设置最小支持度
		}
		
		@Override
		public void reduce(IntWritable key,Iterable<Record> values,Context context) throws IOException, InterruptedException {
			List<List<String>> trans = new LinkedList<List<String>>();
			while(values.iterator().hasNext()) {
				Record record = values.iterator().next();
				LinkedList<String> list = new LinkedList<String>();
				for(String ele : record.list) {
					list.add(ele);
				}
				trans.add(list);
			}
				fptree.FPGrowth(trans,null,context);
		}
		

	}
	
	
	/*===================对上面的mapreduce job的结果去冗余====================================================*/
	
	public static class InverseMapper extends Mapper<LongWritable,Text,Record,IntWritable> {
		@Override
		public void map(LongWritable key,Text value ,Context context) throws IOException, InterruptedException{
			String[] arr = value.toString().split("\t");
			int count = Integer.parseInt(arr[0]);    //或取频繁计数
			Record record = new Record();
			for(int i=1; i < arr.length; i++) {
				record.list.add(arr[i]);
			}
			context.write(record, new IntWritable(count));
		}
	}
	
	public static class MaxReducer extends Reducer<Record,IntWritable,IntWritable,Record> {
		@Override
		public void reduce(Record key,Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
			int max = -1;
			for(IntWritable value:values) {
				int i = value.get();
				if(i > max) 
					max = i;
			}
			context.write(new IntWritable(max), key);
		}
		
	}
	
	
	//执行函数

	public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		
		int min_support = Integer.parseInt(args[2]);          //读取最小支持度
		int sub_min_support = Integer.parseInt(args[3]);
		int freq_group = Integer.parseInt(args[4]);             //读取分组数
//		int min_support = 100;
		conf.set("maprd.task.timeout", "18000000");           //增大map的最大运行时间
		conf.setInt("FP_Growth_min_sup", min_support);
		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileSystem fs=FileSystem.get(getConf());
		/*================执行项频数统计和排序=========================================*/
		Job job = new Job(conf,"DC_FP freq count Job");
		job.setJarByClass(DC_FPTree.class);
		
		
		FileInputFormat.setInputPaths(job, args[0]);
		Path freq_out_path = new Path(out,"freq/");
		Path out_1 = freq_out_path;
		
		if(fs.exists(out_1)) {
			fs.delete(out_1,true);
		}
		FileOutputFormat.setOutputPath(job, out_1);
		
		job.setMapperClass(Freq_Mapper.class);
		job.setReducerClass(Freq_Reducer.class);
		
//		job.setNumReduceTasks(1);     //将reduce的个数设置为1
		FileInputFormat.setMinInputSplitSize(job, 1024*1024);         //设置每个job的输入分片的大小为1M
		FileInputFormat.setMaxInputSplitSize(job,10000 );
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		boolean success = job.waitForCompletion(true);

		/*================对生成的词频进行排序=========================================*/
		   List<Pair<String,Integer>> freq_list = new ArrayList<Pair<String,Integer>>();
		   BufferedReader reader = null;
		   try {
			for (FileStatus f : fs.listStatus(freq_out_path)){
				FSDataInputStream fin = fs.open(f.getPath());
				reader = new BufferedReader(new InputStreamReader(fin));
				String readline = null;
				while((readline = reader.readLine()) != null) {
//					System.out.println("raw line:" + readline);
					String[] Pair = readline.split("\\s+");
					freq_list.add(new Pair<String,Integer>(Pair[0].trim(),Integer.parseInt(Pair[1])));
				}
				
				
				Collections.sort(freq_list,new Comparator<Pair<String,Integer>>() {

					public int compare(Pair<String, Integer> o1,
							Pair<String, Integer> o2) {
						return o1.getRight() == o2.getRight() ?0:(o1.getRight() > o2.getRight() ? -1:1);
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}finally{
			reader.close();
		}
		   
		   
//		/*===========================将排好序的列表存到hdfs中===============================================*/
		   Path sorted_Item_list_file = new Path("/user/zhangxiaoshan/MR_FP/sorted");
		   FSDataOutputStream fout = null;
		   try{
			   if(fs.exists(sorted_Item_list_file)) {
				   fs.delete(sorted_Item_list_file,true);
				   }
			   fout = fs.create(sorted_Item_list_file);
			   int index = 0;
			   for (Pair<String,Integer> item: freq_list) {
				   fout.write((item.getLeft()+"\t" + index+"\n").getBytes("UTF-8"));
//				   System.out.println(item.getLeft()+"\t" + index+"\n");
				   index++;
			   }
		   }catch(IOException e) {
			   e.printStackTrace();
		   }finally{
			   if (fout != null) {
				   fout.close();
			   }
		   }
		
//		   fs.close();
		   conf.set("sorted_item_file", sorted_Item_list_file.toString());
		   System.out.println("sorted_item_file is :"+sorted_Item_list_file.toString());
		   

		/*================执行FP-Growth的job==========================================*/
		conf.setInt("dataset_group_num", freq_group);       //设置fp需要的group的个数
		conf.setInt("FP_Growth_min_sup", sub_min_support);
		job = new Job(conf,"DC_FPGrowth Job"); 
		job.setJarByClass(DC_FPTree.class);
	
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		Path out_2 = new Path(out,"FP/");
		if(fs.exists(out_2)) {
			fs.delete(out_2,true);
		}
		FileOutputFormat.setOutputPath(job, out_2);
		
		job.setMapperClass(GroupMapper.class);
		job.setReducerClass(FPReducer.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Record.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.setMinInputSplitSize(job, 5*1024*1024);         //设置每个job的输入分片的大小为1M
		FileInputFormat.setMaxInputSplitSize(job,10000 );
		
		conf.setInt("dataset_group_num", freq_group);
//		job.setNumReduceTasks(conf.getInt("dataset_group_num",100));
		job.setNumReduceTasks(conf.getInt("dataset_group_num", 300));
	    success |= job.waitForCompletion(true);
		
		/*============执行去冗余的job============================================*/
		 job = new Job(conf,"Unique Job");
		 job.setJarByClass(DC_FPTree.class);
		 
		 FileInputFormat.setInputPaths(job, "/user/zhangxiaoshan/MR_FP/out/FP/part-r-*");
		 Path out_3 = new Path("/user/zhangxiaoshan/MR_FP/out/unique/");
		 if(fs.exists(out_3)){
			 fs.delete(out_3,true);
		 }
		 FileOutputFormat.setOutputPath(job, out_3);
		 
		 
		 job.setMapperClass(InverseMapper.class);
		 job.setReducerClass(MaxReducer.class);
		 
		 FileInputFormat.setMinInputSplitSize(job, 10*1024*1024);
		 FileInputFormat.setMaxInputSplitSize(job,10000 );
		 
		 job.setInputFormatClass(TextInputFormat.class);
		 job.setOutputFormatClass(TextOutputFormat.class);
		 job.setMapOutputKeyClass(Record.class);
		 job.setMapOutputValueClass(IntWritable.class);
		 job.setOutputKeyClass(IntWritable.class);
		 job.setOutputValueClass(Record.class);
		 job.setNumReduceTasks(60);
		 
		 success |= job.waitForCompletion(true);
		
		 return success ? 0 : 1;
	}
	
	public static void main(String[] args) throws Exception{
		int res = ToolRunner.run(new Configuration(), new DC_FPTree(),args);
		System.exit(res);
	}

	
}
