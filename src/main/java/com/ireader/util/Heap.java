package com.ireader.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by zxsted on 15-10-31.
 *
 * java 实现的对排序
 *
 * 在程序中传入一个带排序的数组，同时也可指定排序的方式（降序 或 升序）
 */
public class Heap<T extends  Comparable> {

    private T[] arr;
    private int heapSize;
    public static int DESC = 0,ASC = 1;

    public Comparator<T> comp;

    String comptype = null;


    /**
     *
     * @param arr 待排序序列
     * @param sort  0：降序 或 1：升序
     */
    public Heap(T[] arr,int sort) {
        this.arr = arr;
        heapSize = arr.length;

        if (sort == DESC) {
            comp = less;
        } else {
            comp = gratter;
        }

        makeHeap();
    }


    public Heap(T[] arr) {
        this.arr = arr;
        heapSize = arr.length;
        comptype = "less";
        makeHeap();
    }

    public T popHeap(int i ) {
        T t = arr[0];
        arr[0] = arr[i];  // 将
        arr[i] = t;
        adjustHeap(0, i);
        return t;
    }

    /**
     *
     *
     */
    public void makeHeap() {
        int last,curr;
        last = heapSize;
        curr = (heapSize - 2) /2;  // 最后一个节点

        while (curr >= 0) {
            adjustHeap(curr,last);
            curr --;
        }
    }

    /**
     *
     * @param first
     * @param last
     */
    public void adjustHeap(int first,int last) {

        int curr,child;
        T target ;
        curr = first;
        child = curr * 2 + 1;
        target = arr[first];

        while(child < last) {
            // 选出 字节点中较大者
            if ((child + 1) < last && (comp.compare(arr[child + 1], arr[child])>0)) {
                child++;
            }

            if (comp.compare(arr[child],target) >0) {  // 如果字节点大于父节点，交换
                arr[curr] = arr[child];
                curr = child;
                child = child * 2 + 1;
            } else {
                break;
            }
        }

        arr[curr] = target;
    }


    public void heapSort() {
        int len = heapSize;

        for (int i = len - 1; i >= 0; i--) {
            popHeap(i);
        }
    }


    @Override
    public String toString() {
        return Arrays.toString(arr);
    }

    /** ================ 比较器 ================================================= */

    private Comparator<T> gratter = new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
            return  o1.compareTo(o2);
        }
    };

    private Comparator<T> less = new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
            return  o1.compareTo(o2);
        }
    };


    public static void main(String[] args) {

        Integer[] arr = new Integer[100];

        for (int i = 0 ; i < 100; i++) {
            arr[i] = i;
        }

        Heap<Integer> heap = new Heap<Integer>(arr);

        System.out.println(Arrays.toString(arr));
        heap.heapSort();
        System.out.println(Arrays.toString(arr));

    }



}

























