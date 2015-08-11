package com.ireader.ml.feature;

import com.ireader.ml.Driver;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-6.
 */
public abstract class Transformer extends Driver{

    @Override
    public boolean fit() throws IOException, InterruptedException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean transform() throws IOException, InterruptedException, ClassNotFoundException {
        return false;
    }


}
