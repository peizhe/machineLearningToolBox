package com.ireader.ranklearn.ranknet.util;

/**
 * Created by zxsted on 15-8-3.
 */
import java.util.ArrayList;
import java.util.List;

/**
 * 序列排序工具类， 内部方法都是静态的，返回的是原列表中的 下标列表
 * */
public class Sorter {
    /**
     * 对一个double类型的数组进行排序.
     * @param sortVal 待排序的double 数组
     * @param asc TRUE 是上升
     * @return 拍过序的index类表.
     */
    public static int[] sort(double[] sortVal, boolean asc)
    {
        int[] freqIdx = new int[sortVal.length];
        for(int i=0;i<sortVal.length;i++)
            freqIdx[i] = i;
        for(int i=0;i<sortVal.length-1;i++)
        {
            int max = i;
            for(int j=i+1;j<sortVal.length;j++)
            {
                if(asc)
                {
                    if(sortVal[freqIdx[max]] > sortVal[freqIdx[j]])
                        max = j;
                }
                else
                {
                    if(sortVal[freqIdx[max]] <  sortVal[freqIdx[j]])
                        max = j;
                }
            }
            //swap
            int tmp = freqIdx[i];
            freqIdx[i] = freqIdx[max];
            freqIdx[max] = tmp;
        }
        return freqIdx;
    }
    /*同上但是是float 类型的数组*/
    public static int[] sort(float[] sortVal, boolean asc)
    {
        int[] freqIdx = new int[sortVal.length];
        for(int i=0;i<sortVal.length;i++)
            freqIdx[i] = i;
        for(int i=0;i<sortVal.length-1;i++)
        {
            int max = i;
            for(int j=i+1;j<sortVal.length;j++)
            {
                if(asc)
                {
                    if(sortVal[freqIdx[max]] > sortVal[freqIdx[j]])
                        max = j;
                }
                else
                {
                    if(sortVal[freqIdx[max]] <  sortVal[freqIdx[j]])
                        max = j;
                }
            }
            //swap
            int tmp = freqIdx[i];
            freqIdx[i] = freqIdx[max];
            freqIdx[max] = tmp;
        }
        return freqIdx;
    }
    /**
     * 对 整数数组进行快速排序
     */
    public static int[] sort(int[] sortVal, boolean asc)
    {
        return qSort(sortVal, asc);
    }
    /**
     *对整数列表进行快速排序
     */
    public static int[] sort(List<Integer> sortVal, boolean asc)
    {
        return qSort(sortVal, asc);
    }
    public static int[] sortString(List<String> sortVal, boolean asc)
    {
        return qSortString(sortVal, asc);
    }
    /**
     * 对长整型列表进行快速排序
     */
    public static int[] sortLong(List<Long> sortVal, boolean asc)
    {
        return qSortLong(sortVal, asc);
    }
    /**
     * 对double 类型列表进行快速排序
     */
    public static int[] sortDesc(List<Double> sortVal)
    {
        return qSortDouble(sortVal, false);
    }

    private static long count = 0;
    /**
     * 快速排序
     */
    private static int[] qSort(List<Integer> l, boolean asc)
    {
        count = 0;
        int[] idx = new int[l.size()];
        List<Integer> idxList = new ArrayList<Integer>();
        for(int i=0;i<l.size();i++)
            idxList.add(i);
        //System.out.print("Sorting...");
        idxList = qSort(l, idxList, asc);
        for(int i=0;i<l.size();i++)
            idx[i] = idxList.get(i);
        //System.out.println("[Done.]");
        return idx;
    }

    /*
     * 对字符串 进行快速排序
     * */
    private static int[] qSortString(List<String> l, boolean asc)
    {
        count = 0;
        int[] idx = new int[l.size()];
        List<Integer> idxList = new ArrayList<Integer>();
        for(int i=0;i<l.size();i++)
            idxList.add(i);
        System.out.print("Sorting...");
        idxList = qSortString(l, idxList, asc);
        for(int i=0;i<l.size();i++)
            idx[i] = idxList.get(i);
        System.out.println("[Done.]");
        return idx;
    }

    /**
     * 对长整型进行快速排序
     * */
    private static int[] qSortLong(List<Long> l, boolean asc)
    {
        count = 0;
        int[] idx = new int[l.size()];
        List<Integer> idxList = new ArrayList<Integer>();
        for(int i=0;i<l.size();i++)
            idxList.add(i);
        System.out.print("Sorting...");
        idxList = qSortLong(l, idxList, asc);
        for(int i=0;i<l.size();i++)
            idx[i] = idxList.get(i);
        System.out.println("[Done.]");
        return idx;
    }
    /**
     * 对double 类型数组进行快速排序
     */
    private static int[] qSortDouble(List<Double> l, boolean asc)
    {
        count = 0;
        int[] idx = new int[l.size()];
        List<Integer> idxList = new ArrayList<Integer>();
        for(int i=0;i<l.size();i++)
            idxList.add(i);
        //System.out.print("Sorting...");
        idxList = qSortDouble(l, idxList, asc);
        for(int i=0;i<l.size();i++)
            idx[i] = idxList.get(i);
        //System.out.println("[Done.]");
        return idx;
    }
    /**
     * 对整形进行快速排序
     */
    private static int[] qSort(int[] l, boolean asc)
    {
        count = 0;
        int[] idx = new int[l.length];
        List<Integer> idxList = new ArrayList<Integer>();
        for(int i=0;i<l.length;i++)
            idxList.add(i);
        System.out.print("Sorting...");
        idxList = qSort(l, idxList, asc);
        for(int i=0;i<l.length;i++)
            idx[i] = idxList.get(i);
        System.out.println("[Done.]");
        return idx;
    }
    /**
     * 快速排序的真正实现函数  ，实际上要排序的是 l 中 对应idxList中保存的的索引的元素， 而他们的长度可以是不同的。
     * l ： 是原始序列
     * idxList: 是输入序列的索引列表
     */
    private static List<Integer> qSort(List<Integer> l, List<Integer> idxList, boolean asc)
    {
        int mid = idxList.size()/2;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        List<Integer> pivot = new ArrayList<Integer>();
        for(int i=0;i<idxList.size();i++)
        {
            if(l.get(idxList.get(i)) > l.get(idxList.get(mid)))
            {
                if(asc)
                    right.add(idxList.get(i));
                else
                    left.add(idxList.get(i));
            }
            else if(l.get(idxList.get(i)) < l.get(idxList.get(mid)))
            {
                if(asc)
                    left.add(idxList.get(i));
                else
                    right.add(idxList.get(i));
            }
            else
                pivot.add(idxList.get(i));
        }
        count++;
        if(left.size() > 1)
            left = qSort(l, left, asc);
        count++;
        if(right.size() > 1)
            right = qSort(l, right, asc);
        List<Integer> newIdx = new ArrayList<Integer>();
        newIdx.addAll(left);
        newIdx.addAll(pivot);
        newIdx.addAll(right);
        return newIdx;
    }
    /*
     * 同上，只不过是对 string 类表进行排序
     * */
    private static List<Integer> qSortString(List<String> l, List<Integer> idxList, boolean asc)
    {
        int mid = idxList.size()/2;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        List<Integer> pivot = new ArrayList<Integer>();
        for(int i=0;i<idxList.size();i++)
        {
            if(l.get(idxList.get(i)).compareTo(l.get(idxList.get(mid)))>0)
            {
                if(asc)
                    right.add(idxList.get(i));
                else
                    left.add(idxList.get(i));
            }
            else if(l.get(idxList.get(i)).compareTo(l.get(idxList.get(mid)))<0)
            {
                if(asc)
                    left.add(idxList.get(i));
                else
                    right.add(idxList.get(i));
            }
            else
                pivot.add(idxList.get(i));
        }
        count++;
        if(left.size() > 1)
            left = qSortString(l, left, asc);
        count++;
        if(right.size() > 1)
            right = qSortString(l, right, asc);
        List<Integer> newIdx = new ArrayList<Integer>();
        newIdx.addAll(left);
        newIdx.addAll(pivot);
        newIdx.addAll(right);
        return newIdx;
    }
    /**
     * 同上，是对 整形进行排序
     */
    private static List<Integer> qSort(int[] l, List<Integer> idxList, boolean asc)
    {
        int mid = idxList.size()/2;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        List<Integer> pivot = new ArrayList<Integer>();
        for(int i=0;i<idxList.size();i++)
        {
            if(l[idxList.get(i)] > l[idxList.get(mid)])
            {
                if(asc)
                    right.add(idxList.get(i));
                else
                    left.add(idxList.get(i));
            }
            else if(l[idxList.get(i)] < l[idxList.get(mid)])
            {
                if(asc)
                    left.add(idxList.get(i));
                else
                    right.add(idxList.get(i));
            }
            else
                pivot.add(idxList.get(i));
        }
        count++;
        if(left.size() > 1)
            left = qSort(l, left, asc);
        count++;
        if(right.size() > 1)
            right = qSort(l, right, asc);
        List<Integer> newIdx = new ArrayList<Integer>();
        newIdx.addAll(left);
        newIdx.addAll(pivot);
        newIdx.addAll(right);
        return newIdx;
    }
    /**
     * 同上是对double类型进行排序
     */
    private static List<Integer> qSortDouble(List<Double> l, List<Integer> idxList, boolean asc)
    {
        int mid = idxList.size()/2;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        List<Integer> pivot = new ArrayList<Integer>();
        for(int i=0;i<idxList.size();i++)
        {
            if(l.get(idxList.get(i)) > l.get(idxList.get(mid)))
            {
                if(asc)
                    right.add(idxList.get(i));
                else
                    left.add(idxList.get(i));
            }
            else if(l.get(idxList.get(i)) < l.get(idxList.get(mid)))
            {
                if(asc)
                    left.add(idxList.get(i));
                else
                    right.add(idxList.get(i));
            }
            else
                pivot.add(idxList.get(i));
        }
        count++;
        if(left.size() > 1)
            left = qSortDouble(l, left, asc);
        count++;
        if(right.size() > 1)
            right = qSortDouble(l, right, asc);
        List<Integer> newIdx = new ArrayList<Integer>();
        newIdx.addAll(left);
        newIdx.addAll(pivot);
        newIdx.addAll(right);
        return newIdx;
    }
    /**
     * 对长整型进行排序
     */
    private static List<Integer> qSortLong(List<Long> l, List<Integer> idxList, boolean asc)
    {
        int mid = idxList.size()/2;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>();
        List<Integer> pivot = new ArrayList<Integer>();
        for(int i=0;i<idxList.size();i++)
        {
            if(l.get(idxList.get(i)) > l.get(idxList.get(mid)))
            {
                if(asc)
                    right.add(idxList.get(i));
                else
                    left.add(idxList.get(i));
            }
            else if(l.get(idxList.get(i)) < l.get(idxList.get(mid)))
            {
                if(asc)
                    left.add(idxList.get(i));
                else
                    right.add(idxList.get(i));
            }
            else
                pivot.add(idxList.get(i));
        }
        count++;
        if(left.size() > 1)
            left = qSortLong(l, left, asc);
        count++;
        if(right.size() > 1)
            right = qSortLong(l, right, asc);
        List<Integer> newIdx = new ArrayList<Integer>();
        newIdx.addAll(left);
        newIdx.addAll(pivot);
        newIdx.addAll(right);
        return newIdx;
    }
}

