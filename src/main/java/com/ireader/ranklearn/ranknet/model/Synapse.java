package com.ireader.ranklearn.ranknet.model;

import java.util.Random;

/**
 * Created by zxsted on 15-9-28.
 *
 * 网络边的数据结构
 */
public class Synapse {

    static Random random = new Random();

    protected double weight = 0.0;
    protected double dW = 0.0;           // 链接权重
    protected Neuron source = null;      // 连入节点
    protected Neuron target = null;      // 输出节点

    public Synapse(Neuron source,Neuron target) {

        this.source = source;
        this.target = target;
        this.source.getOutLinks().add(this);
        this.target.getInLinks().add(this);

        weight = (random.nextInt(2) == 0?1:-1) * random.nextFloat()/10;
    }


    /*============  set and get function ============================*/

    public Neuron getSource()
    {
        return source;
    }
    public Neuron getTarget()
    {
        return target;
    }
    public void setWeight(double w)
    {
        this.weight = w;
    }
    public double getWeight()
    {
        return weight;
    }
    public double getLastWeightAdjustment()
    {
        return dW;
    }
    public void setWeightAdjustment(double dW)
    {
        this.dW = dW;
    }
    public void updateWeight()
    {
        this.weight += dW;
    }
}
