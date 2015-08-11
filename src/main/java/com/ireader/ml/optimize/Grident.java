package com.ireader.ml.optimize;

import com.ireader.local.ann.dto.DataPoint;

import java.util.List;

/**
 * Created by zxsted on 15-8-10.
 */
public interface Grident {

    /**
     *  计算单个样本的损失函数
     * */
    public double loss(DataPoint dp,List<Double> weightVec);

    /**
     *  计算单个样本的梯度向量
     * */
    public List<Double> gradient(DataPoint dp,List<Double> weightVec);
}
