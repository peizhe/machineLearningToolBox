package com.ireader.nlp.word2vec.vec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zxsted on 15-7-29.
 */
public class VectorModel  {

    // 词向量映射表
    private Map<String, float[]> wordMap = new HashMap<String ,float[]>();
    private int vectorSize = 200; // 特征数

    private int topNSize = 40;

    public Map<String,float[]> getWordMap() {
        return wordMap;
    }

    public void setWordMap(Map<String,float[]> wordMap) {
        this.wordMap = wordMap;
    }

    /**
     *  获取相似词的数目
     * */
    public int getTopNSize() {
        return topNSize;
    }

    /**
     *  设置最相似词的数目
     * */
    public void setTopNSize(int topNSize) {
        this.topNSize = topNSize;
    }

    public int getVectorSize() {
        return vectorSize;
    }

    public void setVectorSize(int vectorSize) {
        this.vectorSize = vectorSize;
    }

    /**
     *
     * */

}
