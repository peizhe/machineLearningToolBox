package com.ireader.nlp.word2vec.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by zxsted on 15-7-29.
 *
 * 分割和数出极好
 */
public class Tokenizer {

    private List<String> tokens ;
    private ListIterator<String> tokenIter;

    public Tokenizer() {
        tokens = new LinkedList<String>();
        tokenIter = tokens.listIterator();
    }

    /**
     *  分割一段文本， 得到一列标号
     *
     * */
    public  Tokenizer(String text,String delim ) {
        tokens = Arrays.asList(text.split(delim));
        tokenIter = tokens.listIterator();
    }

    /**
     * 获取标号的书目
     * 书目
     * */
    public int size() {
        return tokens.size();
    }

    /**
     *  遍历标号是， 查询是否还有极好没有遍历
     * */
    public boolean hasMoreTokens() {
        return tokenIter.hasNext();
    }

    /**
     *  遍历标号是， 获得下一个之前为报表集的极好
     * */
    public String nextToken() {
        return tokenIter.next();
    }

    /**
     *  项原油极好序列的末尾添加一个极好
     * */
    public void add(String token) {
        if(token == null) {
            return;
        }

        tokens.add(token);
    }

    // 分割符链接极好并输出
    public String toString(String delim) {
        StringBuilder sb = new StringBuilder();

        if (tokens.size() < 1) {
            return sb.toString();
        }

        ListIterator<String> tempTokenIter = tokens.listIterator();
        sb.append(tempTokenIter.next());
        while(tempTokenIter.hasNext()) {
            sb.append(" ").append(tempTokenIter.next());
        }

        return sb.toString();
    }


}
