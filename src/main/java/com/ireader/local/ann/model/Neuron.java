package com.ireader.local.ann.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-3.
 *
 * 神经网络的节点类
 */
public class Neuron {

    public static double mometum = 0.9;            // 更新动量
    public static double learningRate = 0.001;     //

    // 传输函数（包括正向传播和反向传播的计算）
    protected TransferFunction tfunc = new LogitFunction();

    protected double output ; // sigmoid(wsum)(range from 0.0 to 1.0) ; 当前节点的输出
    protected List<Double> outputs = null; // 存储前向传播是，前一层节点的输出值

    protected double delta_i = 0.0;       // 存储反向传播 节点的残差
    protected double[] deltas_j = null;   // 存储反向传播的残差列表 （即下一层的残差） * 乘以 权重，    <-  多对一  即多个后面的节点 对应一个前一层的节点

    protected List<Synapse> inLinks = null;
    protected List<Synapse> outLinks = null;


    public Neuron()
    {
        output = 0.0;
        inLinks = new ArrayList<Synapse>();      // 神经元的输入链接
        outLinks = new ArrayList<Synapse>();     // 神经元的输出链接

        outputs = new ArrayList<Double>();
        delta_i = 0.0;
    }

    public double getOutput()
    {
        return output;
    }

    public double getOutput(int  k )
    {
        return outputs.get(k);
    }

    //返回入链接
    public List<Synapse>  getInLinks()
    {
        return inLinks;
    }

    public List<Synapse> getOutLinks()
    {
        return outLinks;
    }

    public void setOutput(double output)
    {
        this.output = output;
    }

    public void addOutput(double output)
    {
        outputs.add(output);
    }


    /**
     *  计算输出值
     * */
    public void computeOutput()
    {
        Synapse s = null;
        double wsum = 0.0;
        for(int j = 0 ; j < inLinks.size();j++)
        {
            s = inLinks.get(j);  // 获取所有的入边
            // 累加当前边的原点的输出值与当前边的权重之和
            wsum += s.getSource().getOutput() * s.getWeight();
        }

        this.output = (double) tfunc.compute(wsum);
    }

    /**
     *
     * */
    /**
     * 这个也是计算输出值，但是是将输出值存储在一个列表中，这是要计算 一个ranklist 中 比当前节点小的节点的  输出
     * */
    public void computeOutput(int i)
    {
        Synapse s = null;
        double wsum = 0.0;
        for (int j = 0; j < inLinks.size(); j++){
            s = inLinks.get(j);
            wsum += s.getSource().getOutput(i) * s.getWeight();
        }
        output = (double) tfunc.compute(wsum);

        outputs.add(output);
    }

    public void clearOutputs(){
        outputs.clear();
    }

    /**
     *  要实现的两个函数  delta 的计算 隐藏节点  输出节点
     * */


    /**
     *  计算输出层的残差， 只是用与输出层
     *
     *  计算本节点的delta
     * */
    public void computeDelta(PropParameter param) {
        /**
         * double pij = (double) (1.0 / (1.0 + Math.exp(-(prev_output - output))));
         * prev_delta = (targetValue - pij) * func.computeDerivative(prev_output);
         * delta = (targetValue - pij) * func.computeDerivative(output);
         * */
        int[][] pairMap = param.pairMap;   //pariMap存储了 一个RankList 中比当前DataPoint 排序小的所有节点
        int current = param.current;      //当前数据节点所在的ranked list的索引标志

        delta_i = 0.0;
        deltas_j = new double[pairMap[current].length];   // 下一层各个节点的传给当前节点的残差

        for(int k = 0; k < pairMap[current].length; k++) {
            int j = pairMap[current][k];
            float weight = 1;
            double pij = 0;

            // 如果 param 中存储的链接权重矩阵是空的
            if(param.pairWeight == null)
            {
                weight = 1;
                pij = (double) (1.0 / (1.0 + Math.exp(outputs.get(current) - outputs.get(j) )));
                // 这实际上不是真正的 pij ，而是 "target value - pij" : 1 - 1/（1 + e ^{-o_i}）
            } else {  // LambdaRank
                weight = param.pairWeight[current][k];
                pij = (double)(param.targetValue[current][k] - 1.0 / (1.0 + Math.exp( - (outputs.get(current) - outputs.get(j)))));

            }

            double lambda = weight * pij;
            delta_i += lambda;  // 计算偏置
            deltas_j[k] = lambda * tfunc.computeDerivative(outputs.get(j));
        }
        delta_i *= tfunc.computeDerivative(outputs.get(current));
    }


    // 更新 delta
    public void updateDelta(PropParameter param)
    {
        /*
		  dobule errorSum = 0.0;
		  Synapse s = null;
		  for (int i = 0; i< outLinks.size();i++) {
		  	s = outLinks.get(i);
		  	errorSum += (s.getTarget().getPrevDelta() - s.getTarget().getDelta()) * s.getWeight();
		  }
		  prev_delta = errorSum * tfunc.computeDerivative(prev_output);
		  delta = errorSum * tfunc.computeDeriveative(output)
		  */

        int[][] pairMap = param.pairMap;
        float[][] pairWeight = param.pairWeight;
        int current = param.current;

        delta_i = 0;
        deltas_j = new double[pairMap[current].length];
        for(int k = 0; k < pairMap[current].length;k++) {
            int j = pairMap[current][k];
            float weight = (pairWeight != null)?pairWeight[current][k]:1.0f;
            double errorSum = 0.0;
            for(int l = 0; l < outLinks.size(); l++) {
                Synapse s = outLinks.get(l);
                errorSum += s.getTarget().deltas_j[k] * s.weight; //  计算下一级
                if(k == 0)    // k = 0 是计算的偏置
                    delta_i += s.getTarget().delta_i * s.weight;
            }
            if(k == 0)     // k == 0 计算的是偏置
                delta_i *= weight * tfunc.computeDerivative(outputs.get(current));
            deltas_j[k] = errorSum * weight * tfunc.computeDerivative(outputs.get(j));

        }

    }

    /**
     *  更新入链接的权重
     * */
    public void updateWeight(PropParameter param) {
        Synapse s = null;
        for(int k = 0 ; k < inLinks.size(); k++)
        {
            s = inLinks.get(k);
            double sum_j = 0.0;
            for (int l = 0; l < deltas_j.length; l++)
                sum_j += deltas_j[l] * s.getSource().getOutput(param.pairMap[param.current][l]);
            double dw = learningRate * (delta_i * s.getSource().getOutput(param.current) - sum_j);
            s.setWeightAdjustment(dw);
            s.updateWeight();
        }
    }

}
