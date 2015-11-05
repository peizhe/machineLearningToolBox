package com.ireader.ml.fp.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class AssociationRules {
	
	
	private LinkedList<LinkedList<String>>  allAprioriList =null;      //存储所有的频繁项集列表 按照不同长度
	private HashMap<String,Integer> supportDataMap =null;          //存储 各个频繁项集到其支持度的映射字典
	
	
	public AssociationRules () {
		this. allAprioriList = new LinkedList<LinkedList<String>>(); 
		 this.supportDataMap = new HashMap<String,Integer>();
	}  
	
	/**使用HDFS中的文件初始化私有变量
	 * @param oneFreqFile            存储长度为1的频繁项的文件
	 * @param freqFile                     存储结果频繁项集的文件
	 * @throws IOException
	 */
		public void init(String oneFreqFile,String freqFile,Configuration conf) throws IOException {
			
			FileSystem fs = FileSystem.get(conf);
			//读取一次频繁项
			FSDataInputStream fsin = fs.open(new Path(oneFreqFile)); 
			BufferedReader br = null;
			allAprioriList.add(new LinkedList<String>());
			try{
					 br = new BufferedReader(new InputStreamReader(fsin));
					 String line = null;
					 while((line = br.readLine()) != null){
						 String pair[] = line.trim().split("\t");
						 int freq = Integer.parseInt(pair[0]);
						 supportDataMap.put(pair[1],freq);
						 allAprioriList.get(0).addLast(pair[1]);           //将1次频繁项添加到第0个list的末尾
					 }
			}finally{
				if(br != null)
					br.close();
			}
			//读取长度为2以上的频繁项集
			FileStatus[] fstats = fs.listStatus(new Path(freqFile));
			for (int i = 0; i < fstats.length; i++) {
				Path currentPath = fstats[i].getPath();
				if(fs.isDirectory(currentPath)) {
					continue;
				}
			   BufferedReader curbr = null;
			   try{
				   curbr = new BufferedReader(new InputStreamReader(fs.open(currentPath)));
				   String templine = null;
				   while ( (templine = curbr.readLine()) != null) {
					   String fileds[] = templine.split("\\s+");
					   int len = fileds.length - 1;       //当前频繁项的长度
					   int lenAllApri = allAprioriList.size();        //当前所有频繁项集的列表的长度
					   //下面是为了保证allAprioriList的下标索引 index 的列表中存储的 频繁项集的长度 等于 index + 1
					   if(len  >  lenAllApri ) {                                  //如果频繁项集的长度大于当前 allAprioriList 的长度 
						   for(int index = lenAllApri; index < len; index++) {
							   allAprioriList.add(new LinkedList<String>());
						   }
					   }
					   int first_index = templine.indexOf('\t');
					   String freqListStr = templine.substring(first_index);      //截取频繁项集的字串
					   for(int k = 1; k < fileds.length ; k ++) {
						   allAprioriList.get(len - 1).addLast(freqListStr);
					   }
					   
					   //下面对item按照字典排序，以确保唯一性
					  String[] fields = freqListStr.split("\t");
					  List<String> tempList = new LinkedList<String>();
					  for (int index = 0 ; index < fields.length; index++) {
						  tempList.add(fields[index]);
					  }
					  Collections.sort(tempList);
					  
					   supportDataMap.put(frozenSet(tempList), Integer.parseInt(fileds[0]));					   
				   }
			   }finally{
				   if (curbr != null) {
					   curbr.close();
				   }
			   }
			}// for
		}
		
		/**
		 * 生成候选集的方法,通过检查当前长度为k的列表的前k-1项是否相同合并该记录
		 * @param H : 长度为k的频繁项集的列表
		 * @param k   :  当前频繁项的长度
		 * */
		public LinkedList<LinkedList<String>> aprioriGen(List<LinkedList<String>> H , int k) {
			LinkedList<LinkedList<String>> retList = new LinkedList<LinkedList<String>>();
			int lenLk = H.size();
			for(int i = 0; i < lenLk; i++) {
				for (int j = 0; j < lenLk; j++) {
					LinkedList<String> tempList_1 = H.get(i);
					LinkedList<String> tempList_2 = H.get(j);
					
					boolean is_equals = true;
					
					for (int index = 0 ; index < tempList_1.size() - 1;index++){
						if (!tempList_1.get(index).equals(tempList_2.get(index))){
							is_equals = false;
							break;
						}
					}
					
					if(is_equals) {
						if(tempList_1.get(tempList_1.size() -1 ).compareTo(tempList_2.get(tempList_2.size() -1 )) < 0 ){
							  tempList_1.addLast(tempList_2.pollLast());
							  retList.add( tempList_1);
						}else{
							 tempList_2.addLast(tempList_1.pollLast());
							 retList.add( tempList_2);
						}
					}
  				} // for j
			}  // for i
			
			return retList;
		}
		
	/**
	 *  计算最小置信度
	 * @param freqSet : List<String> 一个频繁项集
	 * @param H   ：List<String>   支持度的右部预选列表
	 * @param supportData ：HashMap<String,Float>   频繁项集与其支持度的字典
	 * @param brl ：List<String>   存储挖掘到的关联规则的列表
	 * @param minConf  ：float   最小置信度
	 * @return
	 */
private List<LinkedList<String>> calConf(List<String> freqSet,List<LinkedList<String>> H , HashMap<String,Float> supportData,List<String> brl, float minConf ) {
	List<LinkedList<String>> prundH = new LinkedList<LinkedList<String>>();
	for (LinkedList<String> conseq : H) {
		String freqSetStr = frozenSet(freqSet);
		for(String item : conseq) {
			freqSet.remove(item);
		}
		String subfreqSetStr = frozenSet(freqSet);
		float conf = supportData.get(freqSetStr) / (float)supportData.get(subfreqSetStr);
		if (conf > minConf) {
			String rules=subfreqSetStr + "-->" + frozenSet(conseq) + "\t"+ conf;
			System.out.println(subfreqSetStr + "-->" + frozenSet(conseq) + "\t conf is: "+ conf);
			brl.add(rules);
			prundH.add(conseq);
		}
	}
	return prundH;
}
		
/**
 *  计算频繁项集长度大于2时的支持度
 * @param freqSet : List<String> 一个频繁项集
 * @param hmpl   ：List<String>   支持度的右部预选列表
 * @param supportData ：HashMap<String,Float>   频繁项集与其支持度的字典
 * @param brl ：List<String>   存储挖掘到的关联规则的列表
 * @param minConf  ：float   最小置信度
 */
private void rulesFromConseq(List<String> freqSet,List<LinkedList<String>> hmpl , HashMap<String,Float> supportData,List<String> brl, float minConf) {
	int m = hmpl.get(0).size();
	if (freqSet.size() > (m+1)) {
		List<LinkedList<String>> Hmpl = aprioriGen(hmpl,m+1);
		Hmpl = calConf(freqSet,Hmpl,supportData,brl,minConf);
		if(Hmpl.size() > 1) {
			rulesFromConseq(freqSet,Hmpl,supportData,brl,minConf);
		}
	}
}

/**关联规则生成函数
 * @param allAprioriList :  List<LinkedList<String>>  所有的频繁项
 * @param supportData: HashMap<String,Float>    频繁项与支持度的查询字典
 * @param minConf : 最小支持度
 * */
public List<String> generateRules(List<LinkedList<String>>  allAprioriList, HashMap<String,Float> supportData,float minConf ) {
	List<String> bigRuleList = new LinkedList<String>();
	for ( int i = 1 ; i < allAprioriList.size() ; i++) {
		for (String freqSet : allAprioriList.get(i)){
			List<LinkedList<String>>  H1 = new LinkedList<LinkedList<String>>();
			String[] fileds =  freqSet.split("\t");
			for (int j = 0; j < fileds.length; j ++) {
				LinkedList<String> tempList = new LinkedList<String>();
				tempList.add(fileds[i]);
				H1.add(tempList);
			}
			if (i > 1){
				rulesFromConseq(String2list(freqSet),H1,supportData,bigRuleList,minConf);
			}else {
				calConf(String2list(freqSet),H1,supportData,bigRuleList,minConf);
			}
		}
	}
	return bigRuleList;
}

	/*=========================util functiion=========================================================*/
	/**将列表转换为String
	 * @param inList : 项集列表
	 * @return retString： 项集列表经过排序后的列表转化成的string ，以模拟frozenSet
	 */
	private String frozenSet(List<String> inList) {
		StringBuffer sb = new StringBuffer();
		if (inList.size() == 0) return "";
		 Collections.sort(inList);                        //将列表进行排序
		sb.append(inList.get(0));
		for(int i = 1; i < inList.size() - 1; i ++ ) {
			sb.append("\t" + inList.get(i));
		}
		return sb.toString();
	}
	
	/**将字符串转换为list
	 * @param frezonSet  ：String 一个字符串，用以模拟frozenset
	 * @return retList  ： List<String> ,一个项集列表
	 */
	private LinkedList<String> String2list(String frezonSet) {
		LinkedList<String> retList = new LinkedList<String>();
		String[]  items =  frezonSet.split("\t");
		for (int i = 0 ; i < items.length; i++) {
			retList.addLast(items[i]);
		}
		Collections.sort(retList);                            //排一次序
		return retList;
 	}
	
	
	
	
	
}
