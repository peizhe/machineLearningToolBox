package com.ireader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by zxsted on 15-10-11.
 *
 *  测试FutureTask的用法，如果不想分支线程阻塞主线程，又想取得分支线程的执行结果，就用FutureTask
 */
public class FutureTest {

    public  static void main(String[] args) {

        CountNum cn = new CountNum(0);

        FutureTask<Integer> ft = new FutureTask<Integer>(cn);
        Thread td = new Thread(ft);

        System.out.println("futureTask 开始计算：" + System.currentTimeMillis());
        td.start();
        System.out.println("main主线程可以做些其他的事情：" + System.currentTimeMillis());
        try{
            // futureTask 的get方法会阻塞， 直到可以取得结果为止
            Integer result = ft.get();
            System.out.println("计算的结果是：" + result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("取得分支线程的结果后主线程可以处理其他的事情。");

    }
}


/**
 *  一个计算任务类，可以有返回值
 * */
class CountNum implements Callable {

    private Integer sum;

    public CountNum(Integer sum) {
        this.sum = sum;
    }

    public Object call() throws Exception {
        for (int i = 0; i < 100; i++){
            sum = sum + i;
        }

        // 休眠5秒，观察主线程的行为，与其结果是主线程会继续执行，到要取得FutureTask的结果是等待直至完成。

        Thread.sleep(3000);

        System.out.println("futureTask 执行完成" + System.currentTimeMillis());

        return sum;
    }
}