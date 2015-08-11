package com.ireader.local.ann.model;

import java.util.Random;

/**
 * Created by zxsted on 15-8-3.
 *
 * 神经网络的 节点之间的链接类 ,即指向一个节点的入链边和出链边 存储网络间的权重
 */
public class Synapse {

    static Random random = new Random();   // 随机初始化随机数发生器

    protected double weight = 0.0;
    protected double dW = 0.0;         //
    protected Neuron source = null;    // 边的起点
    protected Neuron target = null;    // 边的终点

    public Synapse(Neuron source,Neuron target) {
        this.source = source;
        this.target = target;
        this.source.getOutLinks().add(this);
        this.target.getInLinks().add(this);

        // 随机初始化权重
        weight = (random.nextInt(2) == 0? 1:-1) * random.nextFloat() / 10;
    }

    public Neuron getSource()
    {
        return source;

    }

    public Neuron getTarget()
    {
        return target;
    }

    public void setWeight(double w) {
        this.weight = w;
    }

    public double getWeight(){
        return weight;
    }

    public double getLastWeightAdjustment(){
        return dW;
    }

    public void setWeightAdjustment(double dW)
    {
        this.dW = dW;
    }

    // 更新权重
    public void updateWeight()
    {
        this.weight += dW;
    }

}
