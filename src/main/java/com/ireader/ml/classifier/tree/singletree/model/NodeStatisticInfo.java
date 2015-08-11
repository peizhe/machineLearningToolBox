//package com.ireader.ml.classifier.tree.singletree.model;
//
//import java.util.*;
//
///**
// * Created by zxsted on 15-8-6.
// *
// * 决策树节点的统计信息
// */
//public class NodeStatisticInfo {
//
//    Map<Integer,List<StatisticRecord>> infoMap =
//            new HashMap<Integer,List<StatisticRecord>>();
//
//    /**
//     *  获取该节点内某个候选分裂属性的统计信息
//     *
//     *  @param aid 候选分裂属性的ID （从1开始）
////     *  @param 统计信息， 该节点上的某个属性不可用，（在其父节点中已经被应用过了，或者没有插入过） 返回NULL
//     *
//     * */
//    public List<StatisticRecord> getAttributeStatisticRecords(Integer aid) {
//        if(infoMap.containsKey(aid)) {
//            return infoMap.get(aid);
//        }else{
//            return null;
//        }
//    }
//
//    /**
//     * 向该节点内，插入某个候选分裂属性的统计信息
//     *
//     * */
//    public void insertAttributeStatisticRecords(Integer aid,List<StatisticRecord> records) {
//        infoMap.put(aid,records);
//    }
//
//    /**
//     * 获取当前节点中出现次数最多的label
//     * */
//    public String getMostCommanLabel(){
//        return getMostCommanLabel(getRecords(new Integer(0),""));
//    }
//
//
//    /**
//     * 获取给定记录中出现次数最多的label
//     * */
//    public  static String getMostCommandLabel(List<StatisticRecord> recordList) {
//        String resultLabel = "";
//        Map<String,Integer> labelCounts = new HashMap<String,Integer>();
//        for(StatisticRecord record:recordList) {
//            String label = record.label;
//            // 没有出现过则插入
//            if(!labelCounts.containsKey(label)) {
//                labelCounts.put(label,new Integer(record.count));
//            } else {
//                Integer newCount =
//                        new Integer(labelCounts.get(label).intValue() + record.count);
//                labelCounts.put(label,newCount); // 更新计数
//            }
//        }
//
//        int maxCount = 0;
//        for(String label:labelCounts.keySet()) {
//
//            if(labelCounts.get(label) > maxCount) {
//                maxCount = labelCounts.get(label);
//                resultLabel = label;
//            }
//        }
//
//        return resultLabel;
//    }
//
//
//    /**
//     * 获取满足某个属性条件的所有记录， 如果aid==0，则返回某个有效属性的所有记录
//     *
//     * @param aid 属性ID
//     * @param value 属性值
//     * @return
//     * */
//    public List<StatisticRecord> getRecords(Integer aid,String value) {
//        if(aid.intValue() == 0){
//            // 找到第一个有效的属性ID
//            // 这里可能会因为infoMap.isEmpty而导致错误
//            aid = infoMap.keySet().iterator().next();
//        }
//        LinkedList<StatisticRecord> records = new LinkedList<StatisticRecord>();
//        for (StatisticRecord record :infoMap.get(aid)) {
//            if(value.equals("") || value.equals(record.avalue))
//                records.add(record);
//        }
//        return records;
//    }
//
//    /**
//     *  获取当前节点的样本中出现过的所有属性ID
//     * */
//    public Set<Integer> getAvailableAIDSet(){
//        return infoMap.keySet();
//    }
//
//}
