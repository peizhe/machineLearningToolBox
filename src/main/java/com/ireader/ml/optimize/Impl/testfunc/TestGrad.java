package com.ireader.ml.optimize.Impl.testfunc;

import com.ireader.ml.optimize.lbfgs.GradFunc;

/**
 * Created by zxsted on 15-9-1.
 */
public class TestGrad  implements GradFunc{

    @Override
    public double[] compute(double[][] dataset, double[] target, double[] w0) {
        double[] gradvec = new double[2];

        gradvec[0] = 400 * w0[0] *(Math.pow(w0[0],2) - w0[1]) + 2 * (w0[0] - 1);
        gradvec[1] = -200 *(Math.pow(w0[0],2) - w0[1]);

        return gradvec;
    }
}
