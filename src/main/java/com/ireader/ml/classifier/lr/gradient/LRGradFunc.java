package com.ireader.ml.classifier.lr.gradient;

import com.ireader.ml.Driver;
import com.ireader.ml.optimize.lbfgs.GradFunc;

import java.io.IOException;

/**
 * Created by zxsted on 15-9-14.
 */
public class LRGradFunc extends Driver implements GradFunc{
    /**
     *  计算 梯度
     * */
    @Override
    public double[] compute(double[][] dataset, double[] target, double[] w0) throws InterruptedException, IOException, ClassNotFoundException {

        fit();  // 调用 MR

        /**
         *  从 HDFS 中读取 权重文件 ，并返回为数组
         * */

        return new double[0];
    }

    /***
     *  调用 gradient MR
     * */
    @Override
    public boolean fit() throws IOException, InterruptedException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean transform() throws IOException, InterruptedException, ClassNotFoundException {
        return false;
    }

}
