package com.ireader.nlp.word2vec.util;

/**
 * Created by zxsted on 15-7-29.
 */
public interface HuffmanNode  extends Comparable<HuffmanNode>{

    public void setCode(int c);

    public void setFrequency(int freq);

    public int getFrequency();

    public void setParent(HuffmanNode parent);

    public HuffmanNode getParent();

    public HuffmanNode merge(HuffmanNode sibling);

}
