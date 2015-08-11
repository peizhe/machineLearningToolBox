package com.ireader.local.ann.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-3.
 */
public class Layer {

    protected List<Neuron> neurons = null;

    public Layer(int size)
    {
        neurons = new ArrayList<Neuron>();
        for(int i = 0 ; i < size; i++)
            neurons.add(new Neuron());
    }

    public Layer(int size,int nType)
    {
        neurons = new ArrayList<Neuron>();
        for(int i = 0; i < size;i++) {
            if(nType == 0)
                neurons.add(new Neuron());
            else
                neurons.add(new ListNeuron());
        }
    }

    public Neuron get(int k)
    {
        return neurons.get(k);
    }

    public int size() {
        return neurons.size();
    }

    /**
     *  计算本层的所有输出值
     * */
    public void computeOutput(int i)
    {
        for(int j = 0 ; j < neurons.size(); j++)
            neurons.get(j).computeOutput(i);
    }

    /**
     *
     * */
    public void computedOutput()
    {
        for(int j = 0 ; j < neurons.size(); j++)
            neurons.get(j).computeOutput();
    }
    /**
     *  清空输出
     * */
    public void clearOutputs()
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).clearOutputs();
    }

    /**
     *  只有输出层才有， 计算本层所有neuron 的残差 delta
     *
     * */
    public void computeDelta(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size();i++)
            neurons.get(i).computeDelta(param);
    }

    /**
     *  更新根据前一层的所有neuron计算残差
     * */
    public void updateDelta(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).updateDelta(param);
    }

    /**
     *  更新权重
     * */
    public void updateWeight(PropParameter param)
    {
        for(int i = 0 ; i < neurons.size(); i++)
            neurons.get(i).updateWeight(param);
    }
}
