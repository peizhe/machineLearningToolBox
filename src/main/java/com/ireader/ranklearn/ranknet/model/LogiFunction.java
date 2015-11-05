package com.ireader.ranklearn.ranknet.model;

/**
 * Created by zxsted on 15-9-29.
 */
public class LogiFunction implements TransferFunction {
    @Override
    public double compute(double x) {
        return (double) (1.0 / (1.0 + Math.exp(-x)));
    }

    @Override
    public double computeDerivative(double x) {
        double output = compute(x);
        return (double) (output *(1.0 - output));
    }
}
