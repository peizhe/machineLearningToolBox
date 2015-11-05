package com.ireader.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxsted on 15-10-10.
 *
 *  本地多线程工具类
 */
public class ConcurenceRunner {

    private static final ExecutorService exec;

    public static final int cpuNum;

    static {
        // 获取可以使用的cpu个数
        cpuNum = Runtime.getRuntime().availableProcessors();
        System.out.println("cpuNum:" + cpuNum);

        // 根据cpu个数初始化线程池size
        exec = Executors.newFixedThreadPool(cpuNum);
    }

    public static void run(Runnable task) {
        exec.execute(task);
    }

    public static void stop(){
        exec.shutdown();
    }



    /**
     *  具体任务继承 本类， 并提供任务列表 list ， 并在process 函数中处理
     * */
    public abstract static class TaskManager {
        private int workLength ;

        public TaskManager(int workLength) {
            this.workLength = workLength;
        }

        public void start(){
            int runCpu = cpuNum < workLength?cpuNum:workLength;

            // CountDownLatch  是一个计数器
            // CountDownLatch如其所写，是一个倒计数的锁存器，当计数减至0时触发特定的事件。利用这种特性，可以让主线程等待子线程的结束。
            final CountDownLatch gate = new CountDownLatch(runCpu);

            // 将任务平均分配给各个线程 fregLength 是子任务长度
            int fregLength = (workLength + runCpu - 1) / runCpu;

            for(int cpu = 0; cpu < runCpu;cpu++) {
                final int start = cpu * fregLength;
                int tmp = (cpu + 1) * fregLength;

                final int end = tmp <= workLength ? tmp : workLength;

                // 创建一个并发线程 ， 完成 gate 计数减 1
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        process(start,end);
                        gate.countDown();   // 当该线程完成任务就减1
                    }
                };


                ConcurenceRunner.run(task);
            }

            try{
                gate.await();    // start 是主线程， 当gate 计数 为零时 ， start 继续运行

//                ConcurenceRunner.stop();
            } catch(InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }


        /**
         *  process 函数是任务处理类， 由实际线程具体实现
         * */
        public abstract void process(int start,int end);
    }



}
