package com.ireader.local.ann.model;

import com.ireader.local.ann.Ranker;
import com.ireader.local.ann.dto.DataPoint;
import com.ireader.local.ann.dto.RankList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-3.
 */
public class RankNet extends Ranker {

    // 参数
    public static int nIteration = 100;
    public static int nHiddenLayer = 1;
    public static int nHiddenNodePerLayer = 10;
    public static double learningRate = 0.00005;

    // 变量
    protected List<Layer> layers = new ArrayList<Layer>();
    protected Layer inputLayer = null;
    protected Layer outputLayer = null;

    // 存储最优验证结果
    protected List<List<Double>> bestModelOnValidation = new ArrayList<List<Double>>();

    protected int rotalPairs = 0;
    protected int misorderedPairs = 0;
    protected double error = 0.0;
    protected double lastError = Double.MAX_VALUE;
    protected int straightLoss = 0;

    public RankNet() {

    }

    public RankNet(List<RankList> samples,int[] features) {
        super(samples,features);
    }

    /**
     *  计算神经网络的输入层和输出层
     * */
    protected void setInputOutput(int nInput,int nOutput)
    {
        inputLayer = new Layer(nInput + 1) ;         // 加上偏置
        outputLayer = new Layer(nOutput);
        layers.clear();
        layers.add(inputLayer);

    }

    /**
     * 建立神经网络的输入层和输出层 (并指定网络类型)
     * */
    protected void setInputOutput(int nInput,int nOutput,int nType) {
        inputLayer = new Layer(nInput+1,nType);
        outputLayer = new Layer(nOutput,nType);
        layers.clear();
        layers.add(inputLayer);
        layers.add(outputLayer);
    }

    /**
     *  添加隐藏层
     * */
    protected void addHiddenLayer(int size) {
        layers.add(layers.size() - 1, new Layer(size));
    }

    /**
     *  链接网络各层
     * */
    protected void wire()
    {
        // 链接输入层和第一个隐藏层
        for(int i = 0 ; i < inputLayer.size();i++)
            for(int j = 0 ; j < layers.get(1).size(); j++)
                connect(0,i,1,j);

        // 链接各个隐藏层
        for(int i=1; i < layers.size() - 1; i++)
            for (int j = 0 ; j < layers.get(i).size(); j++)
                for(int k =0; k < layers.get(i+1).size(); k++)
                    connect(i,j,i+1,k);

        // 在所有层链接bias node 与下一层的节点
        for(int i = 1; i < layers.size();i++)
            for(int j = 0 ; j < layers.get(i).size();j++)
                connect(0,inputLayer.size()-1,i,j);
    }

    /**
     *  创建层间节点之间的连接
     * */
    protected void connect(int sourceLayer,int sourceNeuron,int targetLayer,int targetNeuron)
    {
        new Synapse(layers.get(sourceLayer).get(sourceNeuron),
                layers.get(targetLayer).get(targetNeuron));
    }

    /**
     *  面向数据对的网络训练辅助函数（）
     * */
    protected void addInput(DataPoint dp) {
        for(int k = 0 ; k < inputLayer.size() - 1; k++)
            inputLayer.get(k).addOutput(dp.getFeatureValue(features[k]));
        inputLayer.get(inputLayer.size() - 1).addOutput(1.0f);
    }

    /**
     *  前向传播
     * */
    protected void propagate(int i)
    {
        for(int k = 1 ; k < layers.size(); k++) { // 跳过 输入层
            layers.get(k).computeOutput(i);
        }

    }

    /**
     * 批量前向传播
     * */
    protected int[][] batchFeedForward(RankList rl)
    {
        int[][] pairMap = new int[rl.size()][];
        for(int i = 0 ; i < rl.size();i++)
        {
            // 添加RankList 中的每个datapoint
            addInput(rl.get(i));
            propagate(i);

            // 对RankList 中比当前节点排序靠前的节点进行计数
            int count = 0;
            for(int j = 0 ; j < rl.size();j++)
                if(rl.get(i).getLabel() > rl.get(j).getLabel())
                    count++;

            // pairmap 存储RankList 中比当前节点小的所有节点的索引
            pairMap[i] = new int[count];
            int k = 0;
            for(int j = 0; j < rl.size();j++)
                if(rl.get(i).getLabel() > rl.get(j).getLabel())
                    pairMap[i][k++] = j;
        }
        return pairMap;
    }

    /**
     *  bp传播
     * */
//    protected void batchBackPropagate(int[][] pairMap,float[][] pairWeight)
//    {
//        for(int i = 0; i < pairMap.length;i++)
//        {
//            // back-propagate
//            PropParameter p = new PropParameter(i,pairMap);
//            outputLayer.computeDelta(p);       // 从 output layer 开始计算残差
//            for (int j = layers.size() - 2; j>=1;j--)  // 反向传播到第一个隐藏层
//                layers.get(i).updateDelta(p);
//
//            // 参数更新
//            outputLayer.updateWeight(p);
//            for(int j = layers.size() - 2; j >= 1;j--)
//                layers.get(j).updateWeight();
//        }
//    }


    /**
     *  清空节点的output
     * */
    protected void clearNeuronOutputs()
    {
        for(int k = 0; k < layers.size(); k++) {
            layers.get(k).clearOutputs();
        }
    }

    /**
     *  计算 pairWeight
     * */
    protected float[][] computePairWeight(int[][] pairMap,RankList rl) {
        return null;
    }

    protected RankList internalReorder(RankList rl)
    {
        return rl;
    }

    /**
     *  保存模型 将权重保存到 bestModelOnValidation中
     * */
    protected void saveBestModelOnValidation()
    {
        for(int i = 0 ; i < layers.size(); i++) {   // 循环所有层
            List<Double> l = bestModelOnValidation.get(i);
            l.clear();
            for(int j = 0 ; j < layers.get(i).size(); j++) {
                Neuron n = layers.get(i).get(j);
                for (int k = 0; k< n.getOutLinks().size();k++)
                    l.add(n.getOutLinks().get(k).getWeight());
            }

        }
    }

    /**
     * 从 bestModelOnValidation 加载模型
     * */
    protected void restoreBestModelOnValidation()
    {
        try{
            for(int i = 0 ; i < layers.size() -1; i++)   //循环所有的层
            {
                List<Double> l = bestModelOnValidation.get(i);
                int c = 0;
                for (int j = 0; j < layers.get(i).size(); j++){     //循环本层的所有的神经元
                    Neuron n = layers.get(i).get(j);
                    for (int k =0; k < n.getOutLinks().size(); k++)  // loop through all out links(synapses) of the current neuron
                        n.getOutLinks().get(k).setWeight(l.get(c++));
                }
            }
        }catch(Exception ex)
        {
            System.out.println("Error in NeuralNetwork.restoreBestModelOnValidation():  "+ex.toString());
        }
    }

//    /**
//     *  计算交叉熵
//     * */
//    protected double crossEntropy(double o1,double o2,double targetValue)
//    {
//        double oij = o1 - o2;
//        double ce = -targetValue * oij + SimpleMath.logBase2(1 + Math.exp(oij));
//        return (double) ce;
//    }

    /**
     *  估计loss
     * */



}
