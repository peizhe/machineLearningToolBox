package com.ireader.nlp.word2vec.vec;

import com.ireader.nlp.word2vec.util.HuffmanNode;

/**
 * Created by zxsted on 15-7-29.
 *
 * 输出层的哈夫曼树节点
 *
 */
public class HuffmanNeuron implements HuffmanNode {

    protected int frequency = 0;                     // 词的词频
    protected HuffmanNode  parentNeuron;           // 父节点的连接
    protected int code = 0;                          // 词的 哈夫曼编码
    protected double[] vector;                       // 词的嵌入向量

    @Override
    public void setCode(int c) {
        code = c;
    }

    @Override
    public void setFrequency(int freq) {
        frequency = freq;
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public void setParent(HuffmanNode parent) {
        this.parentNeuron = parent;
    }

    @Override
    public HuffmanNode getParent() {
        return parentNeuron;
    }

    @Override
    public HuffmanNode merge(HuffmanNode right) {
        HuffmanNode parent = new HuffmanNeuron(frequency + right.getFrequency(),vector.length);

        parentNeuron = parent;
        this.code = 0;
        right.setParent(parent);
        right.setCode(1);
        return parent;
    }

    @Override
    public int compareTo(HuffmanNode hn) {
        if (frequency > hn.getFrequency()) {
            return 1;
        } else {
            return -1;
        }
    }

    public HuffmanNeuron(int freq,int vectorSize) {
        this.frequency = freq;
        this.vector = new double[vectorSize];
        parentNeuron = null;
    }


}
