package com.ireader.local.ann.model;

/**
 * Created by zxsted on 15-8-3.
 */
public class HyperTangentFunction  implements  TransferFunction{


    @Override
    public double compute(double x) {
        return (double) (1.7159 * Math.tanh(x * 3/2));
    }

    @Override
    public double computeDerivative(double x) {
        double output = Math.tanh(x * 3/2);
        return (double)(1.7159 * (1.0 - output * output) * 2/3);
    }
}
