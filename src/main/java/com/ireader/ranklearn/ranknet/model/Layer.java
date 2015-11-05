package com.ireader.ranklearn.ranknet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-9-28.
 *
 * 神经网络的层的数据结构， 由 Neuron 组成
 */
public class Layer {

    protected List<Neuron> neurons = null;

    public Layer(int size)
    {
        neurons = new ArrayList<Neuron>();
        for(int i = 0 ; i < size; i++)
            neurons.add(new Neuron());
    }

    /**
     * @param size
     * @param nType  : 0 pair  , 1 list
     * */
    public Layer(int size,int nType)
    {
        neurons = new ArrayList<Neuron>();
        for(int i = 0 ; i < size; i++)
            if (nType == 0)
                neurons.add(new Neuron());
            else
                neurons.add(new ListNeuron());
    }

    public Neuron get(int k)
    {
        return neurons.get(k);
    }

    public int size()
    {
        return neurons.size();
    }

    /**
     * 让本层所有节点计算输出
     * */
    public void computeOutput(int i)
    {
        for(int j = 0 ; j < neurons.size();j++)
            neurons.get(j).computeOutput(j);
    }

    public void computeOutput()
    {
        for(int j = 0 ; j < neurons.size();j++)
            neurons.get(j).computeOutput();
    }

    public void clearOutputs()
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).clearOutputs();
    }

    /**
     *  指定输出层所有的 节点 计算delta
     *
     * */
    public void computeDelta(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).computeDelta(param);
    }

    /**
     * 更新 隐藏层的 layer的delta
     * */
    public void updateDelta(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).updateDelta(param);
    }

    public void updateWeight(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size();i++)
            neurons.get(i).updateWeight(param);
    }

}
