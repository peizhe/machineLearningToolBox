package com.ireader.nlp.word2vec.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zxsted on 15-7-29.
 *
 * 计数器，
 *
 */
public class Counter<T> {
    private HashMap<T,CountInteger> hm = null;

    public Counter() {
        hm = new HashMap<T,CountInteger>();
    }

    public Counter(int initialCapacity) {
        hm = new HashMap<T,CountInteger>(initialCapacity);
    }





    public class CountInteger {
        private int count;

        public CountInteger(int initCount) {
            count = initCount;
        }

        public void set(int num) {
            count = num;
        }

        public int value(){
            return count;
        }

        @Override
        public String toString() {
            return "Count: "+ String.valueOf(count);
        }
    }


    /**
     *  增加一个元素， 并增加其计数
     *
     *  t : 元素
     *  n ： 计数
     * */
    public void add(T t, int n) {
        CountInteger newCount = new CountInteger(n);
        CountInteger oldCount = hm.put(t,newCount);

        if(oldCount != null) {
            newCount.set(oldCount.value() + 1);
        }
    }

    /**
     *  增加一个元素， 计数默认加1
     *
     * */
    public void add(T t) {
        this.add(t,1);
    }

    /**
     *   获取某个 元素的书目
     *
     * */
    public int get(T t) {
        CountInteger count = hm.get(t);
        if (count == null) {
            return 0;
        } else {
            return count.value();
        }
    }

    /**
     *  获取hash表中间的个数
     * */
    public int size(){
        return hm.size();
    }

    /**
     *  删除一个元素
     * */
    public void remove(T t) {
        hm.remove(t);
    }

    /**
     *  输出已经构建好的哈希计数表
     * */
    public Set<T> keySet() {
        return hm.keySet();
    }

    /**
     *  将计数器转换为字符串
     *  return
     * */
    @Override
    public String toString() {
        Iterator<Map.Entry<T,CountInteger>> iterator  = this.hm.entrySet().iterator();

        StringBuilder sb = new StringBuilder();

        Map.Entry<T,CountInteger> next = null;

        while(iterator.hasNext()) {

            next = iterator.next();
            sb.append(next.getKey());
            sb.append("\t");
            sb.append(next.getValue());
            sb.append("\n");

        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String[] strKeys = {"1", "2", "3", "1", "2", "1", "3", "3", "3", "1", "2"};

        Counter<String> counter = new Counter<String>();

        for (String strKey : strKeys) {
            counter.add(strKey);
        }

        for(String strKey: counter.keySet()) {
            System.out.println(strKey + " : " + counter.get(strKey));
        }

        System.out.println(counter.get("9"));
    }

}
