package com.ireader.ml.classifier.lr;

import com.ireader.ml.classifier.lr.costfunc.LRCostFunc;
import com.ireader.ml.classifier.lr.gradient.LRGradFunc;
import com.ireader.ml.optimize.lbfgs.CostFunc;
import com.ireader.ml.optimize.lbfgs.GradFunc;
import com.ireader.ml.optimize.lbfgs.Lbfgs;


import java.io.IOException;
import java.util.Random;

/**
 * Created by zxsted on 15-9-14.
 */
public class LRDriver {

    Random random = new Random();

    double[] randweight(int size) {
        double ret[] = new double[size];

        for (int i = 0 ; i < size; i++) {
            ret[i] = random.nextDouble();
        }

        return ret;
    }



    public static void main(String[] args) {

        LRDriver lrDriver=  new LRDriver();

        CostFunc func = new LRCostFunc();
        GradFunc gfunc = new LRGradFunc();

        Lbfgs lbfgs = new Lbfgs().setIsonesearch(true);

        double[] w0 = lrDriver.randweight(20);

        try {
            double[] w = lbfgs.compute(func, gfunc, null, null, w0, 3e-3, 20, 500000);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
