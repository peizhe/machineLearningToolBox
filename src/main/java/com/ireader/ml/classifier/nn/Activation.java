package com.ireader.ml.classifier.nn;

import static com.ireader.ml.Matrix.*;

import java.util.Random;

/**
 * Created by zxsted on 15-10-21.
 */
public class Activation {

    public static enum ActorType {
        sigmod,tanh,ReLU
    }

    public static double uniform(double min,double max,Random rng) {
        return rng.nextDouble() * (max - min) + min;
    }

    public static int binomial(int n,double p,Random rng) {
        if(p < 0 || p > 1) return 0;

        int c = 0;
        double r;

        for(int i = 0 ; i < n; i++) {
            r = rng.nextDouble();
            if(r < p) c++;
        }

        return c;
    }


    /**================= sigmod ================================================*/

    public static double sigmod(double x) {
        return 1. / (1. + Math.pow(Math.E,-x));
    }

    public static double dsigmod(double x) {
        return x*(1-x);
    }

    /**=================== tanh  ================================================*/

    public static double tanh(double x) {
        return Math.tanh(x);
    }

    public static double dtanh(double x) {
        return 1. - x*x;
    }

    /**==================== ReLU ================================================*/

    public static double ReLU(double x) {

        return  (x > 0)? x:0.0;
    }

    public static double dReLU(double x) {

        return (x > 0)? 1.0:0.0;
    }

    /**==========================================================================*/

    public static Operator sigmod = new Operator() {
        @Override
        public double process(double value) {
            return sigmod(value);
        }
    };

    public static Operator dsigmod = new Operator() {
        @Override
        public double process(double value) {
            return dsigmod(value);
        }
    };

    public static Operator tanh = new Operator() {
        @Override
        public double process(double value) {
            return tanh(value);
        }
    };

    public static Operator dtanh = new Operator() {
        @Override
        public double process(double value) {
            return dtanh(value);
        }
    };

    public static Operator ReLU = new Operator() {
        @Override
        public double process(double value) {
            return ReLU(value);
        }
    };

    public static Operator dReLU = new Operator() {
        @Override
        public double process(double value) {
            return dReLU(value);
        }
    };


    public static double[][] sigmod(double[][] xmat) {
        return matrixOp(xmat,sigmod);
    }
    public static double[][] dsigmod(double[][] xmat) {
        return matrixOp(xmat,dsigmod);
    }



    public static double[][] tanh(double[][] xmat) {
        return matrixOp(xmat,tanh);
    }
    public static double[][] dtanh(double[][] xmat) {
        return matrixOp(xmat,dtanh);
    }



    public static double[][] ReLU(double[][] xmat) {
        return matrixOp(xmat,ReLU);
    }
    public static double[][] dReLU(double[][] xmat) {
        return matrixOp(xmat,dReLU);
    }

    /**
     *  softmax 计算函数
     *
     * @param oMat double[][] :  线性输出结果矩阵
     * */
    public static double[][] softmax(double[][] oMat )  {

        double[][] retMat = null;

        final double maxval = max(oMat);

        double[][] adjustMat = matrixOp(oMat, new Operator() {
            @Override
            public double process(double value) {
                return value - maxval;
            }
        });

        double[][] eMat = matrixOp(adjustMat, exp);

        double[] rowsum = dimsum(eMat, 0);

        retMat = edivid(eMat, rowsum, 0);

        return retMat;
    }


}
