package com.ireader.util.distance;

import java.util.List;

/**
 * Created by zxsted on 15-7-23.
 */
public interface Distance<T> {
    double getDistance(List<T> a,List<T> b) throws Exception;

}
