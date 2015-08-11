package com.ireader.local.ann.model;

/**
 * Created by zxsted on 15-8-3.
 */
public class LogitFunction implements TransferFunction {

    /**
     *  使用逻辑斯蒂 函数进行预测
     * */
    @Override
    public double compute(double x) {
        return (double) (1.0/(1.0 + Math.exp(-x)));
    }

    /**
     *  计算梯度值
     * */
    @Override
    public double computeDerivative(double x) {
        double output = compute(x);
        //  逻辑斯蒂函数的梯度计算公式是 y*(1-y)
        return (double) (output *(1.0 - output));
    }
}
