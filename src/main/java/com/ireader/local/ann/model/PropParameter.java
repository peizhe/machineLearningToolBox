package com.ireader.local.ann.model;

/**
 * Created by zxsted on 15-8-3.
 *
 *
 */
public class PropParameter {

    //RankNet  版本
    public int current = -1;    // （是不是 layer序号？）当前数据节点所在的ranked list 的索引标识
    public int[][] pairMap = null;    // 存储两层之间节点的链接， 数组shape为 m*n

    public PropParameter(int current,int[][] pairMap)   // 输入层
    {
        this.current = current;
        this.pairMap = pairMap;
    }

    // LambdaRank :  RankNet + the flollowing
    public float[][] pairWeight = null;        // 存储节点链接的权重
    public float[][] targetValue = null;       // 是 网络输出 alpha
    public PropParameter(int current,int[][] pairMap,float[][] pairWeight,
                         float[][] targetValue) {
        this.current = current;
        this.pairMap = pairMap;
        this.pairWeight = pairWeight;
        this.targetValue = targetValue;
    }

    //RankNet
    public float[] labels = null;    // 存储网络最后一层的labels
    public PropParameter(float[] labels)
    {
        this.labels = labels;
    }



}
