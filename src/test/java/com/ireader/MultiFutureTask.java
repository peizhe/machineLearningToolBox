package com.ireader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by zxsted on 15-10-11.
 *
 * 测试多核实现，充分的利用CPU来运算数据，并且处理返回的结果,学习API专用
 */
public class MultiFutureTask {


    public static class GetSum implements Callable {

        private Integer total;
        private Integer sum = 0;

        public GetSum(Integer total) {
            this.total = total;
        }


        public Object call() throws Exception {
            for(int i = 1; i < total + 1; i++)
            {
                sum = sum + i;
                Thread.sleep(10);
            }

            System.out.println(Thread.currentThread().getName() + "sum:" + sum);
            return sum;
        }
    }


    public static void main(String[] args) throws InterruptedException {

        /** 存储划分好的任务线程的队列 */
        List<FutureTask<Integer>> list = new ArrayList<FutureTask<Integer>>();

        /** 执行任务的线程池 */
        ExecutorService exec = Executors.newFixedThreadPool(5);

        /** 创建线程任务，添加到任务队列方便取出结果， 然后提交线程池执行 */
        for(int i = 10; i < 20; i++) {
            // 创建对象
            FutureTask<Integer> ft = new FutureTask<Integer>(new GetSum(i));

            // 添加到任务队列
            list.add(ft);

            // 一个个提交给线程池， 也可以一次性的提交给线程池， exec.invokeAll(list);
            exec.submit(ft);
        }


        /** 这个过程中主线程可以执行其他任务*/
        for(int i = 0 ; i < 100; i++) {
            System.out.println("这个过程中主线程可以执行其他任务:" + i);
            Thread.sleep(15L);
        }

        /**开始统计结果*/
        Integer total = 0;
        for(FutureTask<Integer> tempFt:list) {
            try{
                total = total + tempFt.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 处理完毕，一定要记住关闭线程池，这个不能在统计之前关闭，因为如果线程多的话,执行中的可能被打断
        exec.shutdown();
        System.out.println("多线程计算后的总结果是:" + total);

    }

}


