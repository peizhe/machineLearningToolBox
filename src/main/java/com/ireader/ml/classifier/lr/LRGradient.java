package com.ireader.ml.classifier.lr;

import com.ireader.ml.common.struct.DataPoint;
import com.ireader.ml.optimize.Gradient;

import java.util.List;

/**
 * Created by zxsted on 15-9-14.
 */
public class LRGradient extends Gradient {



    @Override
    public double loss(DataPoint dp, List<Double> weightVec) {
        return 0;
    }

    @Override
    public double[] gradient(DataPoint dp, List<Double> weightVec) {
        return new double[0];
    }

    @Override
    public double[] gradient(List<DataPoint> dp, double[] weights) {
        return new double[0];
    }

    @Override
    public double loss(List<DataPoint> dp, double[] weightVec) {
        return 0;
    }
}
