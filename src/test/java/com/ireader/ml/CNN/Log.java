package com.ireader.ml.CNN;

import java.io.PrintStream;

/**
 * Created by zxsted on 15-10-22.
 */
public class Log {
    static PrintStream stream = System.out;

    public static void i(String tag,String msg){
        stream.println(tag+"\t"+msg);
    }

    public static void i(String msg){
        stream.println(msg);
    }

}
