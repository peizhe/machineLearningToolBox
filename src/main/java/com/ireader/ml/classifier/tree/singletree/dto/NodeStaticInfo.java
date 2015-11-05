package com.ireader.ml.classifier.tree.singletree.dto;

import java.util.*;

/**
 * Created by zxsted on 15-10-25.
 *
 *  决策树节点的统计信息
 */
public class NodeStaticInfo {

    // aid  staticList
    Map<Integer,List<StatisticRecord>> infoMap =
            new HashMap<Integer,List<StatisticRecord>>();


    /**
     *  获取该节点内某个候选分裂属性的统计信息列表
     * @param aid
     * @return aid 候选分裂属性的ID（从1开始）
     *
     * @return 统计信息
     */
    public List<StatisticRecord> getAttributeStaticRecords(Integer aid) {

        if (infoMap.containsKey(aid)) {
            return infoMap.get(aid);
        } else {
            return null;
        }
    }

    /**
     *  TODO: 需要添加 返回节点 预测值的 功能
     *
     * */
    public  double getPreValue() {

        List<StatisticRecord> recordList = getRecords(new Integer(-1));
        if (recordList.size() == 0) {
            throw new RuntimeException(" 当前nodeInfo 的record个数为0！");
        }

        Set<Double> avalset = new HashSet<Double>() ;
        for (StatisticRecord record : recordList) {
            avalset.add(record.avalue);
        }

        assert avalset.size() != 0;

        return  getPreValue(recordList) / avalset.size();
    }


    /**
     *  获取指定统计列表中的预测信息
     *
     *  根据model损失函数具体计算 ： 这里model 的损失函数取的是 均方误差
     *
     * @param recordList
     * @return
     */
    public static double getPreValue(List<StatisticRecord> recordList) {

        double mean = 0.0;
        int count = 0;

        for (StatisticRecord record : recordList) {
            count += record.count;

        }

        assert count != 0;

        for (StatisticRecord record : recordList) {
            double curMean = record.mean;
            int curcount = record.count;
            mean = curcount / count;
        }

        return mean;
    }




    public void insertAttributeStaticRecords(Integer aid, List<StatisticRecord> records) {
        infoMap.put(aid,records);
    }


    /**
     * 根据特征id 和特征值取出recordlist
     *
     * @param aid
     * @param value
     * @return
     */
    public List<StatisticRecord> getRecords(Integer aid,double value) {

        if (aid.intValue() == 0) {
            // 找到第一个有效的属性ID
            // 这里可能会因为infoMap.isEmpty() 而导致错误
            aid = infoMap.keySet().iterator().next();
        }

        LinkedList<StatisticRecord> records = new LinkedList<StatisticRecord>();

        for (StatisticRecord record : infoMap.get(aid)) {
            if (value == record.avalue )
                records.add(record);
        }

        return records;
    }

    /**
     * 根据特征id ,特征值 和方向 取出recordlist
     *
     * @param aid
     * @param value
     * @return
     */
    public List<StatisticRecord> getRecords(Integer aid,double value,String direct) {

        if (aid.intValue() == 0) {
            // 找到第一个有效的属性ID
            // 这里可能会因为infoMap.isEmpty() 而导致错误
            aid = infoMap.keySet().iterator().next();
        }

        LinkedList<StatisticRecord> records = new LinkedList<StatisticRecord>();

        for (StatisticRecord record : infoMap.get(aid)) {
            if (value == record.avalue && direct.equalsIgnoreCase(record.direction))
                records.add(record);
        }

        return records;
    }

    /**
     * 根据 aid取出所有的list
     *
     * @param aid
     * @return
     */
    public List<StatisticRecord> getRecords(Integer aid) {

        if (aid.intValue() == 0) {
            // 找到第一个有效的属性ID，aid 是从1开始计数的
            // 这里可能会因为infoMap.isEmpty() 而导致错误
            aid = infoMap.keySet().iterator().next();
        }

        LinkedList<StatisticRecord> records = new LinkedList<StatisticRecord>();

        for (StatisticRecord record : infoMap.get(aid)) {
            records.add(record);
        }

        return records;
    }


    /**
     *  获取当前节点的样本中出现的所有属性ID
     * */
    public Set<Integer> getAvailabelAIDSet() {
        return infoMap.keySet();
    }

}
