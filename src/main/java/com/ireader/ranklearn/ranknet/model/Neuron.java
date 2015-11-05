package com.ireader.ranklearn.ranknet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-9-28.
 */
public class Neuron {

    public static double mementum = 0.9;     // 网络权重的 更新 动量
    public static double learningRate = 0.001 ;

    /**
     * 节点激活函数
     * */
//    protected TransferFunction tfunc = new HyperTangetFunction();
    protected TransferFunction tfunc =new  LogiFunction();

    protected double output; //   激活函数输出值
    protected List<Double> outputs = null;      // 存储一个list的激活值
    protected double delta_i = 0.0;
    protected double[] deltas_j = null;         // listwise 的

    /** 输入输出链接 */
    protected List<Synapse> inLinks = null;
    protected List<Synapse> outLinks = null;

    public Neuron()
    {
        output = 0.0;
        inLinks = new ArrayList<Synapse>();
        outLinks = new ArrayList<Synapse>();

        outputs = new ArrayList<Double>();
        delta_i = 0.0;
    }


    public double getOutput()
    {
        return output;
    }
    public double getOutput(int k)
    {
        return outputs.get(k);
    }
    public List<Synapse> getInLinks()
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

    public void computeOutput()
    {
        // i 是 本 node在当前层的序号
        Synapse s = null;
        double wsum = 0.0;

        /** 对于每个入链接 */
        for(int j = 0 ; j < inLinks.size(); j++)
        {
            s = inLinks.get(j);
            wsum  +=  s.getSource().getOutput() * s.getWeight();
        }

        output = (double) tfunc.compute(wsum);
    }

    /**
     *  面向list 的使用 i 是 list 中的索引
     * */
    public void computeOutput(int i)
    {
        Synapse s = null;
        double wsum = 0.0;

        for(int j = 0 ; j < inLinks.size();j++)
        {
            s = inLinks.get(j);
            wsum += s.getSource().getOutput(i) * s.getWeight();
        }
        output = (double)tfunc.compute(wsum);
        outputs.add(output);
    }

    public void clearOutputs()
    {
        outputs.clear();
    }

    /**
     *
     *  计算 输出层节点的 delta
     *
     *  这里 只是  pairwise 和 lambdaRankNet 的 ，listwise 需要另行实现
     * */
    public  void computeDelta(PropParameter param)
    {
        /**
         * double pij = (double) (1.0 / (1.0 + Math.exp(-(prev_output - output))));
         * prev_delta = (targetValue - pij) * tfunc.computeDerivative(prev_output);
         * delta = (targetValue - pij) * tfunc.computeDerivative(output)
         * */
        int[][] pairMap = param.pairMap;
        int current = param.current;

        delta_i = 0.0;
        deltas_j = new double[pairMap[current].length];

        for(int k = 0 ; k < pairMap[current].length;k++)
        {
            int j = pairMap[current][k];
            float weight = 1;
            double pij = 0;

            if(param.pairWeight == null)    //  如果是 RankNet 不需要 pair_weight
            {
                weight = 1;
                // 事实上下面不是pij， 而是 targetvalue - pij  1 - 1/(1+e^{-o_ij})
                pij = (double)(1.0 / (1.0 + Math.exp(outputs.get(current) - outputs.get(j))));
            }
            else     // LambdaRank
            {
                weight = param.pairWeight[current][k];
                pij = (double)(param.targetValue[current][k] - 1.0 / (1.0 + Math.exp(-(outputs.get(current) - outputs.get(j)))));
            }

            double lambda  = weight * pij;
            delta_i += lambda;
            deltas_j[k] = lambda * tfunc.computeDerivative(outputs.get(j));  // list wise
        }

        delta_i *= tfunc.computeDerivative(outputs.get(current));
        //(delta_i * input_i) - (sum_{delta_j} * input_j) is the *negative* of the gradient, which is the amount of weight should be added to the current weight
        //associated to the input_i

    }

    /**
     * 根据下一层 的神经元节点计算 delta
     * */
    public void updateDelta(PropParameter param)
    {
        /*double errorSum = 0.0;
	    Synapse s = null;
	    for(int i=0;i<outLinks.size();i++)
	    {
	    	s = outLinks.get(i);
	    	errorSum += (s.getTarget().getPrevDelta()-s.getTarget().getDelta()) * s.getWeight();
	    }
	    prev_delta = errorSum * tfunc.computeDerivative(prev_output);
		delta =      errorSum * tfunc.computeDerivative(output);*/
        int[][] pairMap = param.pairMap;
        float[][] pairWeight = param.pairWeight;

        int current = param.current;

        delta_i = 0;
        deltas_j = new double[pairMap[current].length];
        for (int k = 0 ; k < pairMap[current].length;k++)
        {
            int j = pairMap[current][k];
            float weight = (pairWeight != null) ? pairWeight[current][k]:0.0F;
            double errorSum = 0.0;

            for(int l = 0; l < outLinks.size(); l++)
            {
                Synapse s = outLinks.get(l);
                errorSum += s.getTarget().deltas_j[k] * s.getWeight();
                if(k == 0)
                    delta_i += s.getTarget().delta_i * s.getWeight();
            }

            if(k == 0)
                delta_i *= weight * tfunc.computeDerivative(outputs.get(current));

            deltas_j[k] = errorSum * weight * tfunc.computeDerivative(outputs.get(current));
        }

    }

    /**
     *  更新连入权重
     * */
    public void updateWeight(PropParameter param)
    {
        Synapse s = null;
        for(int k = 0 ; k < inLinks.size(); k++)
        {
            s = inLinks.get(k);
            double sum_j = 0.0;
            for(int l = 0 ; l < deltas_j.length;l++)
                sum_j += deltas_j[l] * s.getSource().getOutput(param.pairMap[param.current][l]);
            double dw = learningRate * (delta_i * s.getSource().getOutput(param.current) - sum_j);
            s.setWeightAdjustment(dw);
            s.updateWeight();
        }
    }



}
