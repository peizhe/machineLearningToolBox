package com.ireader.ml.optimize.lbfgs;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-28.
 */
public interface CostFunc {

    public double compute(double[][] dataset,double[] targets,double[] weights) throws InterruptedException, IOException, ClassNotFoundException;
}
