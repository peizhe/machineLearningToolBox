package com.ireader.ml.common.measure.distance;

import java.util.List;

/**
 * Created by zxsted on 15-7-23.
 */
public class EuclideanDistance<T extends Number>  implements Distance<T>{
    @Override
    public double getDistance(List<T> a,List<T> b) throws Exception {
        if(a.size() != b.size())
            throw new Exception("size not compatiable!");
        else {
            double sum = 0.0;
            for(int i = 0; i < a.size();i++) {
                sum += Math.pow(a.get(i).doubleValue() - b.get(i).doubleValue(),2);
            }
            sum = Math.sqrt(sum);
            return sum;
        }
    }
}
