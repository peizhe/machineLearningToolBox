package com.ireader.ranklearn.ranknet.model;

/**
 * Created by zxsted on 15-9-28.
 */
public interface TransferFunction {

    public double compute(double x);
    public double computeDerivative(double x);
}
