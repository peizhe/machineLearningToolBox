package com.ireader.util;

/**
 * Created by zxsted on 15-7-24.
 */
public class FuncUtil {


    public static double sigmoid( double input) {
        double ret = 1+ Math.exp(-1 * input);
        return 1.0/ ret;
    }


}
