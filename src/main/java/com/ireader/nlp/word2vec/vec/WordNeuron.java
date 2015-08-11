package com.ireader.nlp.word2vec.vec;

import com.ireader.nlp.word2vec.util.HuffmanNode;
import com.ireader.nlp.word2vec.util.HuffmanTree;

import java.util.List;
import java.util.Random;

/**
 * Created by zxsted on 15-7-29.
 */
public class WordNeuron  extends HuffmanNeuron{

    private String name;
    private List<HuffmanNode> pathNeurons;


    public WordNeuron(String name,int freq,int vectorSize) {
        super(freq,vectorSize);
        this.name = name;


        // 初始化 word node 的权重
        Random random = new Random();
        for (int i = 0 ; i < vector.length; i++) {
            vector[i] = (random.nextDouble() - 0.5) / vectorSize;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HuffmanNode> getPathNeurons() {
        if(pathNeurons != null) {
            return pathNeurons;
        }

        pathNeurons = HuffmanTree.getPath(this);

        return pathNeurons;
    }
}
