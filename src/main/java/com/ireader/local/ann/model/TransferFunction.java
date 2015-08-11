package com.ireader.local.ann.model;

/**
 * Created by zxsted on 15-8-3.
 *
 * 这是 神经网络的传输函数的接口
 */
public interface TransferFunction {

    public double compute(double x);             // 计算 costfunc 值
    public double computeDerivative(double x);   // 计算 偏倒数 值

}
