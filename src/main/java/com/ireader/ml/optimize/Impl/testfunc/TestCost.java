package com.ireader.ml.optimize.Impl.testfunc;

import com.ireader.ml.optimize.lbfgs.CostFunc;

/**
 * Created by zxsted on 15-9-1.
 */
public class TestCost  implements CostFunc{


    @Override
    public double compute(double[][] dataset, double[] targets, double[] weights) {

        return 100*Math.pow(weights[0]*weights[0] - weights[1],2) + Math.pow(weights[0]-1,2);
    }
}
