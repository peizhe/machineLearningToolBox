package com.ireader.ml.fp.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.hadoop.io.WritableComparable;

/*
 * 用于mr的list类型的序列化类
 * */
public class Record implements WritableComparable<Record>{

		LinkedList<String> list;
		
		public Record() {
			list = new LinkedList<String>();
		}
		
		public Record(String[] arr) {
			list = new LinkedList<String> ();
			for (int i = 0 ; i < arr.length; i++) {
				list.add(arr[i]);
			}
		}
		
		@Override
		public String toString() {
			String str = list.get(0) ;
			for (int i = 1; i < list.size(); i++) {
				str += "\t" + list.get(i);
			}
			return str;
		}
		
		public void readFields(DataInput in ) throws IOException {
			list.clear();    //清空list中的所有内容
			String line = in.readUTF();
			String[] arr = line.split("\t");
			for(int i = 0; i < arr.length; i++) 
				list.add(arr[i]);
		}

		public void write(DataOutput out) throws IOException {
			out.writeUTF(this.toString());
		}

		public int compareTo(Record o) {
			Collections.sort(list);
			Collections.sort(o.list);
			return this.toString().compareTo(o.toString());
		}
		
		
}
