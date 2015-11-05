package com.ireader.ml.classifier.lr.costfunc;

import com.ireader.ml.Driver;
import com.ireader.ml.optimize.lbfgs.CostFunc;

import java.io.IOException;

/**
 * Created by zxsted on 15-9-14.
 */
public class LRCostFunc extends Driver implements CostFunc{

    /**
     *  计算 损失函数
     * */
    @Override
    public double compute(double[][] dataset, double[] targets, double[] weights) throws InterruptedException, IOException, ClassNotFoundException {

        fit();    // 调用 MR 计算损失

        /**
         *  从 HDFS 中读取 计算结果
         * */


        return 0;
    }

    /**
     *  调用 Cost MR
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
