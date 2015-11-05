package com.ireader.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-10-21.
 */
public class MultiTest {


    double[] testArr = null;

    double[] restArr = null;


    public void init(int num) {

        testArr = new double[num * 1000];
         restArr = new double[20];

        new ConcurenceRunner.TaskManager(num) {

            @Override
            public void process(int start, int end) {

                System.out.println("计算任务范围是： (" + start + "," + end+")");
                for (int i = start; i < end; i++)
                    for (int j = 0 ;j < 1000; j++)
                        testArr[i* 1000 + j] = ((double)i*j);
            }
        }.start();
    }

    public void sumbyMultiThread(int num) {

         new ConcurenceRunner.TaskManager(num) {

             @Override
             public void process(int start, int end) {

                 double sum = 0.0;
                 for (int i = start; i < end; i++) {
                   sum +=  testArr[i];
                 }

                 System.out.println("计算任务范围是： (" + start + "," + end+") , 结果是：" + sum );
             }
         }.start();

        ConcurenceRunner.stop();
    }


    public static void main(String[] args) {

        int num = 10;
        MultiTest demo = new MultiTest();

        demo.init(10);

        System.out.println("测试列表长度是：" + demo.testArr.length);
        demo.sumbyMultiThread(demo.testArr.length);



    }


}
