package com.ireader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zxsted on 15-10-10.
 */
public class arrayGenericTest {

    public static void main(String[] args) {


        double[][] testM = new double[][]{
                {1,2,3,4},
                {5,6,7,8},
                {9,10,11,12},
                {13,14,15,15}
        };


        Map<String,Object> propMap= new HashMap<String,Object>();

        propMap.put("testMap", testM);

        double[][] rebuildMap = (double[][]) propMap.get("testMap");

        System.out.println("rebuild Matrix from Object type ");

        for(int i = 0 ; i < rebuildMap.length;i++) {
            System.out.println(Arrays.toString(rebuildMap[i]));
        }


    }
}
