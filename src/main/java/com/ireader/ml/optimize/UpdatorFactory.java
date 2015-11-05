package com.ireader.ml.optimize;

/**
 * Created by zxsted on 15-9-14.
 */
public class UpdatorFactory {

    Updator updator = null;


    public Updator getInstance(String className) {
        try {

            updator = (Updator) Class.forName(className).newInstance();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return updator;
    }

}
