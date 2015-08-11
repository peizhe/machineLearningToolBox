package com.ireader.ml.common.measure.distance;

import java.util.List;

/**
 * Created by zxsted on 15-7-23.
 */
public class HammingDistance<T> implements Distance<T> {

    @Override
    public double getDistance(List<T> a , List<T> b) throws Exception {
        if(a.size() !=  b.size())
            throw new Exception("size not compatible!");
        else {
            double sum = 0.0;
            for(int i = 0; i < a.size(); i++) {
                if(a.get(i).equals(b.get(i)))
                    sum = sum+1;
            }
            return sum;
        }
    }
}
