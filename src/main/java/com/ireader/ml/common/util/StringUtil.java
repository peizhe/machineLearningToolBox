package com.ireader.ml.common.util;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-24.
 */
public class StringUtil {

    /**
     *  将字符串转化为list
     * */
    public static int StringToList(ArrayList<Double> list, String str,String delim) throws IOException {
        if(str.length() == 0) {
            throw new IOException("load weigths encount error, weight number is zero!");
        }
        String fields[] = str.split(delim);

        for (int i = 0; i < fields.length; i++) {
            list.add(Double.parseDouble(fields[i]));
        }

        return list.size();
    }


}
