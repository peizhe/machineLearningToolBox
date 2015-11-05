package com.ireader.ml.classifier.tree.singletree.dto;

/**
 * Created by zxsted on 15-10-25.
 *
 * 一条记录的统计信息：
 *
 * key： nid aid avalue direct     value : mean square count
 */
public class StatisticRecord {

    public Integer nid;     // 与之关联的决策树中节点的ID

    public Integer aid;     // 属性ID : 从1 开始计数
    public double avalue;   // 属性取值  一个属性为 value+pos ,即是： 属性+right

    public String direction ;  // 不等号的方向（）

    public double label;    // 预测值(分类使用， 回归不用)

    public double mean ;    // 平均值
    public double squre;    // 方差值
    public int count;       // 条件与该路径相符合的训练集元组个数

    @Deprecated
    public StatisticRecord(Integer nid, Integer aid, double avalue,String direction, double label,
                           int count) {
        this.nid = nid;

        this.aid = aid;
        this.avalue = avalue;

        this.direction = direction;

        this.label = label;
        this.count = count;


    }

    public StatisticRecord(Integer nid, Integer aid, double avalue,String direction,double square,
                           double mean,int count) {
        this.nid = nid;
        this.aid = aid;
        this.avalue = avalue;
        this.count = count;

        this.direction = direction;
        this.mean = mean;
        this.squre = square;
    }

    public String toString() {
        return this.nid + "," + this.aid + "," + this.avalue +","+ this.direction + "," + this.squre+ "," + this.mean
                + "\t" + count;
    }


}
