package com.ireader.ml.fp.local;

import com.ireader.ml.fp.local.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;


public class FPTree extends Reducer {

	private int minSuport;

	public int getMinSuport() {
		return minSuport;
	}

	public void setMinSuport(int minSuport) {
		this.minSuport = minSuport;
	}
	
	
	//从多个文件中读取Transaction Record
	public List<List<String>> readTransRecords(String... filenames ) {
		List<List<String>> transaction = null;
		
		if(filenames.length > 0) {
			transaction = new LinkedList<List<String>> ();
			for(String filename : filenames) {
				try{
					FileReader fr = new FileReader(filename);
					BufferedReader br = new BufferedReader(fr);
					
					try{
						String line;
						List<String> record;
						while((line = br.readLine()) != null) {
							if(line.trim().length() > 0) {
								String str[] = line.split("\\s+");
//								System.out.println("元素个数是："+ str.length);
								record = new LinkedList<String> ();
								for(String w : str) {
									record.add(w);
								   System.out.print(w+"\t");}
								transaction.add(record);
							}
							System.out.println();
						}
					}finally{
						br.close();
					}
				}catch(IOException ex){
					System.out.println("Read transaction records failed." + ex.getMessage());
					System.exit(1);
				}
			}
		}// end if
		return transaction;
	}
	
	//FP-Growth算法
	public void FPGrowth(List<List<String>> transRecords, 
			           List<String> postPattern) {
//		System.out.println("进行一次FP树的构造，接收到的数据集大小是：" + transRecords .size());
		//构建项头表， 同时也是频繁1项集
		ArrayList<TreeNode> HeaderTable = buildHeaderTable(transRecords);
		//构建FP-Tree
		TreeNode treeRoot = buildFPTree(transRecords,HeaderTable);
		//如果FP-Tree为空则返回
		if(treeRoot.getChildren() == null || treeRoot.getChildren().size() ==  0 )
			return;
		
		//输出项头表每一项+postPattern (条件模式基的前缀)
		if(postPattern != null) {
			for(TreeNode header : HeaderTable) {
				System.out.print(header.getCount() + "\t" + header.getName());
				for (String ele:postPattern) 
					System.out.print("\t" + ele);
				System.out.println();
			}
		}
		
		//找到项头表的每一项的条件模式基 ，进入递归迭代
//		System.out.println("headertable 长度为："+ HeaderTable.size());
		for ( TreeNode header : HeaderTable) {
			//后缀模式增加一项
			List<String> newPostPattern = new LinkedList<String> ();
			newPostPattern.add(header.getName());
			if (postPattern != null) 
				newPostPattern.addAll(postPattern);         //上面三行的添加方法，保证了前缀是后添加的项在前面
			
			//寻找header的条件模式基CPB，放入newTransecords中
				List<List<String>> newTransRecords = new LinkedList<List<String>> ();
				TreeNode backnode = header.getNextHomonym();   //找到下一个同名节点
				while(backnode != null) {
					int counter = backnode.getCount();
					List<String> prenodes = new ArrayList<String> ();
					TreeNode parent = backnode;
					//遍历backnode的祖先节点，放到prenodes中   这里是向上遍历
					while((parent = parent.getParent()).getName() != null) {  
						prenodes.add(parent.getName());
					}
					while(counter -- > 0) {                                  //本次记录有几个计数，就将本记录向新的交易数据中添加几次
						newTransRecords.add(prenodes);
					}
					backnode = backnode.getNextHomonym();
				}
//				System.out.println("得到的新的数据集为："+newTransRecords.size());
				//递归迭代
				FPGrowth(newTransRecords,newPostPattern);
			}
		
	}
	
	//FP-Growth for mr
	public void FPGrowth(List<List<String>> transRecords, 
			           List<String> postPattern,Context context) throws IOException, InterruptedException {
		//构建项头表， 同时也是频繁1项集
		ArrayList<TreeNode> HeaderTable = buildHeaderTable(transRecords);
		//构建FP-Tree
		TreeNode treeRoot = buildFPTree(transRecords,HeaderTable);
		//如果FP-Tree为空则返回
		if(treeRoot.getChildren() == null || treeRoot.getChildren().size() ==  0 )
			return;
		
		//输出项头表每一项+postPattern (条件模式基的前缀)
		if(postPattern != null) {
			for(TreeNode header : HeaderTable) {
				String outStr = header.getName();
				int count = header.getCount();
//				System.out.print(header.getCount() + "\t" + header.getName());
//				String head_str = header.getName() + "\t";
				for (String ele:postPattern) 
//					System.out.print("\t" + ele);
					outStr += "\t" + ele;
				context.write(new IntWritable(count),new Text(outStr));
			}
		}
		
		//找到项头表的每一项的条件模式基 ，进入递归迭代
		for ( TreeNode header : HeaderTable) {
			//后缀模式增加一项
			List<String> newPostPattern = new LinkedList<String> ();
			newPostPattern.add(header.getName());
			if (postPattern != null) 
				newPostPattern.addAll(postPattern);         //上面三行的添加方法，保证了前缀是后添加的项在前面
			
			//寻找header的条件模式基CPB，放入newTransecords中
				List<List<String>> newTransRecords = new LinkedList<List<String>> ();
				TreeNode backnode = header.getNextHomonym();   //找到下一个同名节点
				while(backnode != null) {
					int counter = backnode.getCount();
					List<String> prenodes = new ArrayList<String> ();
					TreeNode parent = backnode;
					//遍历backnode的祖先节点，放到prenodes中   这里是向上遍历
					while((parent = parent.getParent()).getName() != null) {  
						prenodes.add(parent.getName());
					}
					while(counter -- > 0) {                                  //本次记录有几个计数，就将本记录向新的交易数据中添加几次
						newTransRecords.add(prenodes);
					}
					backnode = backnode.getNextHomonym();
				}
				//递归迭代
				FPGrowth(newTransRecords,newPostPattern,context);
			}
		
	}
	
	/*==========================================子功能函数=============================================*/
	
	/**
	 * 构建项头表，同时也是频繁项1集
	 * */
	public ArrayList<TreeNode> buildHeaderTable(List<List<String>> transRecords) {
		ArrayList<TreeNode> F1 = null;
		if(transRecords.size() > 0 ) {
			F1 = new ArrayList<TreeNode>();
			Map<String,TreeNode> map = new HashMap<String,TreeNode>();
			//计算事务数据库中的各项的支持度
			for (List<String> record : transRecords) {
				for( String item : record) {
					if(!map.keySet().contains(item)){
						TreeNode node = new TreeNode(item);
						node.setCount(1);
						map.put(item, node);
					}else{
						map.get(item).countIncrement(1);
					}
				}
				for(String item : map.keySet()) {
//					System.out.println(item + "\t" + map.get(item).getCount());
				}
			}
			
			//将支持度大于（或等于）minSup的项加入到F1中
			Set<String> names = map.keySet();
			for(String name : names) {
				TreeNode tnode = map.get(name);
				if(tnode.getCount() >= minSuport) {
					F1.add(tnode);
				}
			}
			Collections.sort(F1);
			return F1;
		}else{
			return null;
		}
	}
	
	// 构建FP-Tree
	public TreeNode buildFPTree(List<List<String>> transRecords,
			ArrayList<TreeNode> F1) {
		TreeNode root = new TreeNode();  // 创建树的根节点
		for(List<String> transRecord : transRecords) {
			LinkedList<String> record =  sortByF1(transRecord,F1);
			TreeNode  subTreeRoot = root;
			TreeNode tmpRoot = null;
			if(root.getChildren() != null) {
				while(!record.isEmpty()
						&& (tmpRoot = subTreeRoot.findChild(record.peek())) != null) {
					tmpRoot.countIncrement(1);
					subTreeRoot = tmpRoot;
					record.poll();
				}
			}
			addNodes(subTreeRoot,record,F1);
		}
		return root;
	}
	
	/*
	 * 将交易记录按照项的频繁程度降序排列
	 */
	public LinkedList<String> sortByF1(List<String> transRecord,
			ArrayList<TreeNode> F1) {
		Map<String,Integer> map = new HashMap<String,Integer> ();
		for(String item : transRecord) {
			//因为F1已经是按照降序排列的
			for(int i = 0; i < F1.size(); i ++) {
				TreeNode  tnode= F1.get(i);
				if(tnode.getName().equals(item)) {
					map.put(item, 1);
				}
 			}
		}
		
		//排序
		ArrayList<Entry<String,Integer>> al = new ArrayList<Entry<String,Integer>> (
							map.entrySet());
		Collections.sort(al,new Comparator<Entry<String,Integer>> () {

			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				//降序排列
				return o1.getValue() - o2.getValue();
			}
		});
		
		LinkedList<String> rest = new LinkedList<String> ();
		for(Entry<String,Integer> entry:al) {
			rest.add(entry.getKey());
		}
		return rest;
	}
	
	/*
	 * 将record作为ancestor的后代插入树中
	 * */
	public void addNodes (TreeNode acestor,LinkedList<String> record,
			ArrayList<TreeNode> F1) {
		if(record.size() > 0) {
			while(record.size() > 0) {
				String item = record.poll();
				TreeNode leafnode = new TreeNode(item);
				leafnode.setCount(1);
				leafnode.setParent(acestor);
				acestor.addChildren(leafnode);
				
				for (TreeNode f1 : F1) {
					if(f1.getName().equals(item)) {
						while (f1.getNextHomonym() != null) {
							f1 = f1.getNextHomonym();
						}
						f1.setNextHomonym(leafnode);
						break;
					}
				}
				//递归添加子树
				addNodes(leafnode,record,F1);
			}
		}
	}
	

	
	//测试主函数
	public static void main(String[] args) {
		 FPTree fptree = new FPTree();
		 fptree.setMinSuport(2);
		 List<List<String>> transRecords = fptree  
	                .readTransRecords("/home/zxsted/data/market");  
//		    System.out.println("开始构建FP-Tree ，数据集的长度是：" + transRecords.size());
	        fptree.FPGrowth(transRecords, null);  
	}
	
}
