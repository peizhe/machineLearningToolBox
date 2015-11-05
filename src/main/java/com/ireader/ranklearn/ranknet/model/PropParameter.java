package com.ireader.ranklearn.ranknet.model;

/**
 * Created by zxsted on 15-9-28.
 */
public class PropParameter {

    // RankNet 使用的 数据结构
    public int current = -1;  // 当前数据点在Ranklist 中的index
    public int[][] pairMap = null;

    public PropParameter(int current,int[][] pairMap)
    {
        this.current = current;
        this.pairMap = pairMap;
    }

    //  LambdaRank使用的数据结构： RankNet 使用的 + 和下面的
    public float[][] pairWeight = null;       // 数据对权重
    public float[][] targetValue = null;

    public PropParameter(int current,int[][] pairMap,float[][] pairWeight,float[][] targetValue) {
        this.current = current;
        this.pairMap = pairMap;
        this.pairWeight = pairWeight;
        this.targetValue = targetValue;
    }

    //  ListNet
    public float[] labels = null;
    public PropParameter(float[] labels)
    {
        this.labels = labels;
    }


}
