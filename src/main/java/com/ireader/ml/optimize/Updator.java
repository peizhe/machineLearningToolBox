package com.ireader.ml.optimize;

import java.util.List;

/**
 * Created by zxsted on 15-8-10.
 */
public interface Updator {

    /**
     *  更新梯度值
     *  weightvec： 旧的梯度值
     *  gridant : 梯度
     *  step: 更新步长
     *  regParam: 正则化项
     *
     *  return：  更新的梯度结果
     * */
    public List<Double> update(List<Double> weightvec,List<Double> gridant, Double step,double regParam);
}
