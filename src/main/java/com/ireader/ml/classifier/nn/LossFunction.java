package com.ireader.ml.classifier.nn;


import java.util.Arrays;

import static com.ireader.ml.Matrix.*;

/**
 * Created by zxsted on 15-10-23.
 *
 *
 * 定义各种损失函数
 *      交叉熵损失函数
 *      mse 损失函数
 */
public class LossFunction {






    /**
     *  计算 交叉熵
     *
     *  @param outMat double[][] : 预测矩阵
     *  @param yMat double[][] : 结果矩阵
     * */
    public static  double crossEntropy(double[][] outMat, double[][] yMat) {


        double[][] logMat =  matrixOp(outMat, new Operator() {
            @Override
            public double process(double value) {
                return Math.log(value);
            }
        });

        double[][] elemprod = matrixOp(yMat, logMat, null, null, multiply);

        return -(1.0/outMat.length) * (sum(elemprod));

    }

    /**
     *  计算 交叉熵的error
     *
     * @param preMat: 概率预测矩阵  size (m,n_out)
     * @param targetMat: 样本label矩阵 size (m,n_out)
     *
     * @return errorMat double[][] : size is (m,n_out)
     * */
    public static double[][]  deltaCrossEntropy(double[][] preMat ,double[][] targetMat) {

        return matrixOp(targetMat,preMat,null,null,minus);

    }


    /**
     *  计算 mse 损失函数
     * */
    public static double mse(double[][] p,double[][] q) {

        final int m = p.length; // 样本个数

        double[][] minusMat = matrixOp(p,q,null,null,minus);

        double[][] squareMat = matrixOp(minusMat, new Operator() {
            @Override
            public double process(double value) {
                return (value * value) / m;
            }
        });

        double ret = sum(squareMat);

        return ret;
    }


    /**
     *  计算 mse 损失函数的 error
     *
     *@param p : double[][] 预测矩阵
     *@param q : double[][] label矩阵
     * */
    public static double[][] deltaMse(double[][] p ,double[][] q) {

        return matrixOp(q,p,null,null,minus);

    }




    /** == 测试上面函数的功能 */
    public static void main(String[] args) {

        double[][] eye =  eyeMat(5);

        double[][] p = fill(0.05,10,20);

        double[][] five = fill(0.2, 5, 5);

        printMatrix(p);


        ptestname("测试 softmax");
        double[][] prob = softmax(p);
        printMatrix(prob);

        ptestname("测试交叉熵");
        double crossEntropy = crossEntropy(five,eye);
        System.out.println("交叉熵 为：" + crossEntropy);

        ptestname("测试mse");
        double mse = mse(five,eye);
        System.out.println("mse  为：" + mse);

        ptestname("测试delta 交叉熵");
        printMatrix(eye);
        double[][] deltaCE = deltaCrossEntropy(five,eye);
        printMatrix(deltaCE);

        ptestname("测试delta mse");
        double[][] deltaMse = deltaMse(five,eye);
        printMatrix(deltaMse);


    }

}
