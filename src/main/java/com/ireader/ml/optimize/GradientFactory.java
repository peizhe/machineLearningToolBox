package com.ireader.ml.optimize;

import org.apache.hadoop.conf.Configuration;

/**
 * Created by zxsted on 15-9-14.
 */
public class GradientFactory {

    private Gradient gradient = null;

    public  Gradient getInstance(String className,Configuration conf) {

        try {
           gradient = (Gradient) Class.forName(className).newInstance();

            gradient.setConf(conf);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return gradient;
    }
}
