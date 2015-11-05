package com.ireader.nlp.tool.match;

import java.util.TreeMap;

/**
 * Created by zxsted on 15-8-20.
 */
public class ACTest {


    public static void main(String[] args) {

        TreeMap<String,String> map = new TreeMap<String,String>();

        String[] keyArray = new String[] {
                "hers",
                "his",
                "she",
                "he",
                "her",
                "er",
                "ers",
                "rs"
        };

        int i = 0;
        for(String key:keyArray)
        {
            map.put(key,key +":"+i++);
        }

        AhoCorasickDoubleArrayTrie<String> act = new AhoCorasickDoubleArrayTrie<String>();   // ac自动机实例

        act.build(map);

//        act.parseText("users",new AhoCorasickDoubleArrayTrie.IHit<String>() {
//
//            @Override
//            public void hit(int begin,int end,String value)
//            {
//                System.out.printf("[%d:%d]=%s\n", begin, end, value);
//            }
//        });

        System.out.println(act.parseText("uhers"));


    }
}























