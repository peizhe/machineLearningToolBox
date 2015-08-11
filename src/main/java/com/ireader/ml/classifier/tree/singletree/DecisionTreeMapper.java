package com.ireader.ml.classifier.tree.singletree;

import com.ireader.ml.classifier.tree.singletree.model.Rule;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by zxsted on 15-8-6.
 *
 * 决策树算法的Mapper类
 *
 * 输入 ：1.训练数据集D， D是文本文件， 每行是一个训练样本， 每个属性字段之间使用逗号分割
 *       2.条件集队列文件Q （通过DistributedCache方式传入）
 * 输出：<key,value> 对， 其中key 是一个符合类型， 具体为key = <条件号#属性号，属性值，元素标号> value = 1
 */
public class DecisionTreeMapper extends Mapper<Object,Text,Text,IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private List<Rule> ruleQueue = new LinkedList<Rule>();

    /**
     *  配置函数， 主要负责从DistributedCache 中读取条件集队列
     * */
    public void setup(Context context) throws IOException {

        Path[] filePath = DistributedCache.getLocalCacheFiles(context.getConfiguration());

        // 因为只有一个文件， 所以该文件应该是Queue
        assert(filePath.length == 1);

        // 载入条件队列信息
        loadQueueFile(filePath[0],context.getConfiguration());
    }

    // 从filePath 中读取Queue文件
    private void loadQueueFile(Path filePath,Configuration conf) throws FileNotFoundException {
        //System.err.println("Qeueue_file_path = " + filePath.toString());
        Scanner scanner = new Scanner(new File(filePath.toString()));

        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.length() == 0)
                continue;
            Rule rule = Rule.parse(line);
            ruleQueue.add(rule);
        }

        scanner.close();

        if(ruleQueue.size() == 0) {
            // 说明ruleQueue 文件是空的， 那么此时应该正在处理根节点
            // 我们为根节点生成一个空白的规则，加入队列
            // 确保队列非空
            ruleQueue.add(new Rule());
        }
    } // end func

    /**
     *  Map 函数
     * */
    @Override
    public void map(Object key, Text value,Context context) throws IOException,InterruptedException{
        String line = value.toString();
        // 将读取输入的行数据解析成记录
        String[] aValues = line.split("\\,");
        String label = aValues[aValues.length - 1]; // 最后一个属性是类标号
        // 对于每一条记录， 按次序处理

        int nid = 0;

        // nid 从1 号开始编号
        for(nid = 1; nid <= ruleQueue.size(); nid++) {
            Rule rule = ruleQueue.get(nid - 1);
            if(isFitRule(rule,aValues)) {
                // 如果训练样本符合规则，则继续生成
                for(int aid = 1; aid <= aValues.length - 1; aid++) {
                    // 遍历每一个属性，看看是否出现在规则中
                    if(!rule.conditions.containsKey((new Integer(aid)))) {
                        // 之前(该条件的路径上)没有使用过， 这是一个可能的候选属性
                        String newKey =
                                nid+"#"+aid+","+aValues[aid-1]+","+label;
                        context.write(new Text(newKey),one);
                    }
                }// end for aid
            }
        } // end for nid
    } // map


    /**
     *  判断一个样本记录是否符合规则要求
     * */
    private boolean isFitRule(Rule rule,String[] aValues) {
        boolean statisfied = true;

        // 测验 rule中已有的 feat 是否在该记录中，如果有一个不一样那么就认为不是
        for(Integer aid: rule.conditions.keySet()){
            if(!aValues[aid.intValue() - 1].equals(rule.conditions.get(aid))) {
                statisfied = false;
                break;
            }
        }

        return statisfied;
    }

}























