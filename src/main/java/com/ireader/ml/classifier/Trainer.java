package com.ireader.ml.classifier;

import com.ireader.ml.Driver;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-5.
 */
public abstract class Trainer  extends Driver{


    /**
     *  监督型 model 的同一训练接口
     * */
    public boolean train() throws InterruptedException, IOException, ClassNotFoundException {

        return fit();
    }

    /**
     *  监督模型的同一批量预测接口
     * */
    public boolean predict() throws InterruptedException, IOException, ClassNotFoundException {

        return transform();

    }


//    @Override
//    public boolean fit() {
//        return false;
//    }
//
//    @Override
//    public boolean transform() {
//        return false;
//    }
}
