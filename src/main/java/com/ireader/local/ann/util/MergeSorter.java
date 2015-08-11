package com.ireader.local.ann.util;

/**
 * Created by zxsted on 15-8-3.
 */
import java.util.List;

/**
 * @description: 合并排序
 * @autor: ted
 * @Date: 2015-01-14
 * */
public class MergeSorter {


    /**
     * list 版的
     * */
    public static int[] sort(List<Float> list,boolean asc)
    {
        float[] values = new float[list.size()];
        int[] idx = new int[list.size()];
        for(int i = 0; i < list.size();i++)
        {
            idx[i] = i;
            values[i] = list.get(i);
        }
        return sort(values,asc);
    }
    /**排序方法 ， 不带索引的*/
    public static int[] sort(float[] list,boolean asc)
    {
        int[] idx = new int[list.length];
        for(int i = 0 ; i < list.length; i++)
            idx[i] = i;
        return sort(list,idx,asc);
    }

    /**
     * 合并排序的主方法
     * @param list : 原始列表
     * @param idx: 索引
     * @param asc : 排序方法 true： 递增排序， false ： 递减排序
     * */
    public static int[] sort(float[] list,int[] idx,boolean asc)
    {
        if(idx.length == 1)
            return idx;

        int mid = idx.length / 2;
        int[] left = new int[mid];
        int[] right = new int[idx.length - mid ];

        for(int i = 0 ; i < mid; i++)
            left[i] = idx[i];
        for(int i = mid ; i < idx.length; i++)
            right[i-mid] = idx[i];

        left = sort(list,left,asc);
        right = sort(list,right,asc);

        return merge(list,left,right,asc);
    }

    /**
     * 合并方法
     * @param list :原始数据列表，
     * @param left : 左侧列表的 索引
     * @param right： 右侧列表的索引
     * @param asc：排序规则 true：递增    false：递减
     * */
    private static int[] merge(float[] list,int[] left,int[] right,boolean asc) {
        int[] idx = new int[left.length + right.length];
        int i = 0;
        int j = 0;
        int c = 0;

        while (i < left.length && j < right.length)
        {
            if(asc)
            {
                if(list[left[i]] <= list[right[j]])
                    idx[c++] = left[i++];
                else
                    idx[c++] = right[j++];
            }
            else{
                if(list[left[i]] >= list[right[j]])
                    idx[c++] = left[i++];
                else
                    idx[c++] = right[j++];
            }
        }

        for(; i < left.length;i++)
            idx[c++] = left[i];
        for(; j < right.length;j++)
            idx[c++] = right[j];
        return idx;
    }

    /*==========下面是double版的==============================================*/
    public static int[] sort(double[] list, boolean asc)
    {
        int[] idx = new int[list.length];
        for(int i=0;i<list.length;i++)
            idx[i] = i;
        return sort(list, idx, asc);
    }
    public static int[] sort(double[] list, int[] idx, boolean asc)
    {
        if(idx.length == 1)
            return idx;

        int mid = idx.length / 2;
        int[] left = new int[mid];
        int[] right = new int[idx.length-mid];

        for(int i=0;i<mid;i++)
            left[i] = idx[i];
        for(int i=mid;i<idx.length;i++)
            right[i-mid] = idx[i];

        left = sort(list, left, asc);
        right = sort(list, right, asc);

        return merge(list, left, right, asc);
    }
    private static int[] merge(double[] list, int[] left, int[] right, boolean asc)
    {
        int[] idx = new int[left.length + right.length];
        int i=0;
        int j=0;
        int c=0;
        while(i < left.length && j < right.length)
        {
            if(asc)
            {
                if(list[left[i]] <= list[right[j]])
                    idx[c++] = left[i++];
                else
                    idx[c++] = right[j++];
            }
            else
            {
                if(list[left[i]] >= list[right[j]])
                    idx[c++] = left[i++];
                else
                    idx[c++] = right[j++];
            }
        }
        for(;i<left.length;i++)
            idx[c++] = left[i];
        for(;j<right.length;j++)
            idx[c++] = right[j];
        return idx;
    }
}
