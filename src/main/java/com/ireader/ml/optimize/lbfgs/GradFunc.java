package com.ireader.ml.optimize.lbfgs;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-28.
 */
public interface GradFunc {

    public double[] compute(double[][] dataset,double[] target,double[] w0) throws InterruptedException, IOException, ClassNotFoundException;
}
