package com.ireader.ranklearn.ranknet.model;

import com.ireader.ranklearn.ranknet.Ranker;
import com.ireader.ranklearn.ranknet.dto.ADataPoint;
import com.ireader.ranklearn.ranknet.dto.RankList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-9-28.
 */
public class RankNet extends Ranker {

    //模型参数
    public static int nIteration = 100;
    public static int nHiddenLayer = 1;
    public static int nHiddenNodePerLayer = 10;
    public static double learningRate = 0.00005;

    // 变量
    protected List<Layer> layers = new ArrayList<Layer>();
    protected Layer inputLayer = null;
    protected Layer outputLayer = null;

    //  保存 在验证数据集上的最优model
    protected List<List<Double>> bestModelOnValidation = new ArrayList<List<Double>>();

    protected int totalPairs = 0;
    protected int misorderedPairs = 0;
    protected double error = 0.0;
    protected double lastError = Double.MAX_VALUE;
    protected int straightLoss = 0;

    public RankNet(){}

    public RankNet(List<RankList> samples,int[] features){
        super(samples,features);
    }

    /**
     * 设置网络
     * */
    protected void setInputOutput(int nInput,int nOutput)
    {
        inputLayer = new Layer(nInput + 1);   // 添加偏置
        outputLayer = new Layer(nOutput);
        layers.clear();
        layers.add(inputLayer);
        layers.add(outputLayer);
    }

    protected void setInputOutput(int nInput,int nOutput,int nType){
        inputLayer = new Layer(nInput+1,nType);
        outputLayer = new Layer(nOutput,nType);
        layers.clear();
        layers.add(inputLayer);
        layers.add(outputLayer);
    }

    /**
     *  为网络添加隐藏层
     * */
    protected void addHiddenLayer(int size)
    {
        layers.add(layers.size() - 1, new Layer(size));
    }

    /**
     * 建立各个层之间的链接
     * */
    protected void wire()
    {
        // wire the input layer to the first hidden layer
        for(int i = 0 ; i < inputLayer.size()-1;i++)
            for(int j = 0 ; j < layers.get(1).size();j++)
                connect(0,i,1,j);

        /** 链接中间层的节点 */
        for(int i = 1; i < layers.size(); i++)
            for(int j = 0 ; j < layers.get(i).size();j++)
                for(int k = 0 ; k < layers.get(i+1).size();k++)
                    connect(i,j,i+1,k);

        /** 链接 偏置neuron */
        for(int i = 1; i < layers.size();i++)
            for(int j = 0 ; j < layers.get(i).size(); j++)
                connect(0,inputLayer.size() - 1,i,j);

    }

    /**
     * 链接两个节点
     * */
    protected void connect(int sourceLayer,int sourceNeuron,int targetLayer,int targetNeuron ){
        new Synapse(layers.get(sourceLayer).get(sourceNeuron),layers.get(targetLayer).get(targetNeuron));
    }

    protected void addInput(ADataPoint p)
    {
        for(int k = 0 ; k < inputLayer.size() - 1; k++)
            inputLayer.get(k).addOutput(p.getFeatureValue(features[k]));
        // 初始化 偏置
        inputLayer.get(inputLayer.size() - 1).addOutput(1.0f);
    }

    /**
     *  i 是 example 在ranklist 中的索引 序号 i
     * */
    protected void propagate(int i)
    {
        for(int k = 1; k < layers.size(); k++)   // skip the input layer
            layers.get(k).computeOutput(i);
    }

    protected int[][] batchFeedForward(RankList rl)
    {
        int[][] pairMap = new int[rl.size()][];
        for(int i = 0; i < rl.size();i++)
        {
            addInput(rl.get(i));
            propagate(i);           //  传播一个样本

            int count  = 0;
            for(int j = 0 ; j < rl.size(); j++)
                if (rl.get(i).getLabel() > rl.get(j).getLabel())
                    count++;        // 计算list中小于 当前i的label的 数据个数

            pairMap[i] = new int[count];        // pairMap 存储rlist中label小于 当前i 的序号

            /** 填充 pairmap*/
            int k = 0;
            for(int j = 0 ; j < rl.size();j++)
                if (rl.get(i).getLabel() > rl.get(j).getLabel())
                    pairMap[i][k++] = j;

        }

        return pairMap; // size : [rl.length][n(label < i.lebal)]
    }


    /** 反向传播  pairWeight 是 根据 ndcg 计算的 */
    protected void batchBackPropagate(int[][] pairMap,float[][] pairWeight)
    {
        // 后向传播
        for(int i = 0 ; i < pairMap.length;i++) {
            PropParameter p = new PropParameter(i,pairMap);
            outputLayer.computeDelta(p);

            for(int j = layers.size() - 2; j >= 1; j--)
                layers.get(j).updateDelta(p);

            // weight update
            outputLayer.updateWeight(p);
            for(int j = layers.size()-2;j>=1;j--)
                layers.get(j).updateWeight(p);

        }
    }

    /** clear output */
    protected void clearNeuronOutputs()
    {
        for(int k = 0; k < layers.size();k++) // skip the input layer
            layers.get(k).clearOutputs();
    }

    /**
     *  lambda rank use to compute lambda
     * */
    protected float[][] computePairWeight(int[][] pairMap,RankList rl)
    {
        return null;
    }

    protected RankList internalReorder(RankList rl)
    {
        return rl;
    }

    /**
     *  Model validation
     * */
    protected void saveBestModelOnValidation()
    {
        for(int i = 0 ; i < layers.size() - 1; i++)
        {
            List<Double> l = bestModelOnValidation.get(i);
            l.clear();
            for(int j = 0 ; j < layers.get(i).size();j++)
            {
                Neuron n = layers.get(i).get(j);

                for(int k = 0 ; k < n.getOutLinks().size();k++)
                    l.add(n.getOutLinks().get(k).getWeight());
            }
        }
    }


    protected void restoreBestModelOnValidation()
    {
        try{
            for(int i = 0 ; i < layers.size()-1;i++)
            {
                List<Double> l = bestModelOnValidation.get(i);
                int c = 0;
                for(int j = 0; j < layers.get(i).size();j++)
                {
                    Neuron n = layers.get(i).get(j);
                    for(int k = 0 ; k < n.getOutLinks().size();k++)
                        n.getOutLinks().get(k).setWeight(l.get(c++));
                }
            }
        }catch(Exception ex)
        {
            System.out.println("Error in NeuralNetwork,restoreBestModelOnValidation:"+ex.toString());
        }

    }

    /** use cross entropy  as loss function */
    protected double crossEntropy(double o1,double o2,double targetValue){
        double oij = o1 - o2;
        double ce = -targetValue * oij + Math.log(1+Math.exp(oij)) / Math.log(2);
        return (double)ce;
    }

    /** estimate loss */
    protected void estimateLoss()
    {
        misorderedPairs = 0;
        error = 0.0;
        for(int j = 0 ; j < samples.size(); j++)
        {
            RankList rl = samples.get(j);
            for(int k = 0 ; k < rl.size() - 1; k++)
            {
                double o1 = eval(rl.get(k));
                for(int l = k+1; l < rl.size(); l++)
                {
                    if(rl.get(k).getLabel() > rl.get(l).getLabel())
                    {
                        double o2 = eval(rl.get(l));
                        error += crossEntropy(o1,o2,1.0f);

                        if(o1 < o2)
                            misorderedPairs++;
                    }
                }
            }
        }

        error =Math.round(error/totalPairs);

        lastError = error;
    }

    public void init()
    {
        PRINT("Initializing...");

        //Set up the network
        setInputOutput(features.length,1);
        for(int i = 0; i < nHiddenLayer; i++)
            addHiddenLayer(nHiddenNodePerLayer);

        wire();

        totalPairs = 0;

        for(int i = 0 ; i < samples.size();i++)
        {
            RankList rl = samples.get(i).getCorrectRanking();
            for(int j = 0 ; j < rl.size(); j++)
                for(int k = j + 1;k < rl.size(); k++)
                    if(rl.get(j).getLabel() > rl.get(k).getLabel())
                        totalPairs++;
        }

        if(validationSamples != null)
            for(int i = 0 ; i < layers.size(); i++)
                bestModelOnValidation.add(new ArrayList<Double>());

        Neuron.learningRate = learningRate;

        PRINTLN("[Done]");
    }

    public void learn()
    {
        PRINTLN("-----------------------------------------");
        PRINTLN("Training starts...");
        PRINTLN("--------------------------------------------------");
        PRINTLN(new int[]{7, 14, 9, 9}, new String[]{"#epoch", "% mis-ordered", scorer.name()+"-T", scorer.name()+"-V"});
        PRINTLN(new int[]{7, 14, 9, 9}, new String[]{" ", "  pairs", " ", " "});
        PRINTLN("--------------------------------------------------");

        for(int i = 1; i <= nIteration; i++)
        {
            for(int j = 0; j < samples.size(); j++)
            {
                RankList rl = internalReorder(samples.get(j));   // reorder
                int[][] pairMap = batchFeedForward(rl);          //
                float[][] pairWeight = computePairWeight(pairMap,rl);
                batchBackPropagate(pairMap,pairWeight);
                clearNeuronOutputs();
            }

            //
            scoreOnTrainingData = scorer.score(rank(samples));
            estimateLoss();
            PRINT(new int[]{7, 14}, new String[]{i+"", Math.round(((double)misorderedPairs)/totalPairs)+""});

            if(i % 1 == 0)
            {
                PRINT(new int[]{9}, new String[]{Math.round(scoreOnTrainingData)+""});
                if(validationSamples != null)
                {
                    double score = scorer.score(rank(validationSamples));
                    if(score > bestScoreOnValidationData)
                    {
                        bestScoreOnValidationData = score;
                        saveBestModelOnValidation();
                    }
                    PRINT(new int[]{9}, new String[]{Math.round(score)+""});
                }
            }
            PRINTLN("");
        }

        if(validationSamples != null)
            restoreBestModelOnValidation();

        scoreOnTrainingData = Math.round(scorer.score(rank(samples)));
        PRINTLN("--------------------------------------------------");
        PRINTLN("Finished sucessfully.");
        PRINTLN(scorer.name() + " on training data: " + scoreOnTrainingData);

        if(validationSamples != null)
        {
            bestScoreOnValidationData = scorer.score(rank(validationSamples));
            PRINTLN(scorer.name()+ " on validaton data:" + Math.round(bestScoreOnValidationData));
        }
        PRINTLN("--------------------------------------------------");
    }

    public double eval(ADataPoint p)
    {
        // feed input
        for(int k = 0 ; k < inputLayer.size()-1;k++)
            inputLayer.get(k).setOutput(p.getFeatureValue(features[k]));
        // set bias
        inputLayer.get(inputLayer.size() - 1).setOutput(1.0f);

        for(int k = 1; k < layers.size();k++)
            layers.get(k).computeOutput();

        return outputLayer.get(0).getOutput();
    }

    public Ranker clone()
    {
        return new RankNet();
    }

    public String toString()
    {
        String output = "";
        for(int i = 0; i < layers.size()-1;i++)
        {
            for(int j = 0 ; j < layers.get(i).size();j++)
            {
                output += i + " " + j + " ";
                Neuron n = layers.get(i).get(j);
                for(int k = 0; k < n.getOutLinks().size();k++)
                    output += n.getOutLinks().get(k).getWeight() + ((k==n.getOutLinks().size()-1)?"":" ");
                output += "\n";
            }
        }
        return output;
    }

    public String model()
    {
        String output = "## " + name() + "\n";
        output += "## Epochs = " + nIteration + "\n";
        output += "## No. of features = " + features.length + "\n";
        output += "## No. of hidden layers = " + (layers.size()-2) + "\n";
        for(int i=1;i<layers.size()-1;i++)
            output += "## CNNLayer " + i + ": " + layers.get(i).size() + " neurons\n";

        //print used features
        for(int i=0;i<features.length;i++)
            output += features[i] + ((i==features.length-1)?"":" ");
        output += "\n";
        //print network information
        output += layers.size()-2 + "\n";//[# hidden layers]
        for(int i=1;i<layers.size()-1;i++)
            output += layers.get(i).size() + "\n";//[#neurons]
        //print learned weights
        output += toString();
        return output;
    }
    public void load(String fn)
    {
        try {
            String content = "";
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fn), "ASCII"));

            List<String> l = new ArrayList<String>();
            while((content = in.readLine()) != null)
            {
                content = content.trim();
                if(content.length() == 0)
                    continue;
                if(content.indexOf("##")==0)
                    continue;
                l.add(content);
            }
            in.close();
            //load the network
            //the first line contains features information
            String[] tmp = l.get(0).split(" ");
            features = new int[tmp.length];
            for(int i=0;i<tmp.length;i++)
                features[i] = Integer.parseInt(tmp[i]);
            //the 2nd line is a scalar indicating the number of hidden layers
            int nHiddenLayer = Integer.parseInt(l.get(1));
            int[] nn = new int[nHiddenLayer];
            //the next @nHiddenLayer lines contain the number of neurons in each layer
            int i=2;
            for(;i<2+nHiddenLayer;i++)
                nn[i-2] = Integer.parseInt(l.get(i));
            //create the network
            setInputOutput(features.length, 1);
            for(int j=0;j<nHiddenLayer;j++)
                addHiddenLayer(nn[j]);
            wire();
            //fill in weights
            for(;i<l.size();i++)//loop through all layers
            {
                String[] s = l.get(i).split(" ");
                int iLayer = Integer.parseInt(s[0]);//which layer?
                int iNeuron = Integer.parseInt(s[1]);//which neuron?
                Neuron n = layers.get(iLayer).get(iNeuron);
                for(int k=0;k<n.getOutLinks().size();k++)//loop through all out links (synapses) of the current neuron
                    n.getOutLinks().get(k).setWeight(Double.parseDouble(s[k+2]));
            }
        }
        catch(Exception ex)
        {
            System.out.println("Error in RankNet::load(): " + ex.toString());
        }
    }
    public void printParameters()
    {
        PRINTLN("No. of epochs: " + nIteration);
        PRINTLN("No. of hidden layers: " + nHiddenLayer);
        PRINTLN("No. of hidden nodes per layer: " + nHiddenNodePerLayer);
        PRINTLN("Learning rate: " + learningRate);
    }
    public String name()
    {
        return "RankNet";
    }
    /**
     * FOR DEBUGGING PURPOSE ONLY
     */
    protected void printNetworkConfig()
    {
        for(int i=1;i<layers.size();i++)
        {
            System.out.println("CNNLayer-" + (i+1));
            for(int j=0;j<layers.get(i).size();j++)
            {
                Neuron n = layers.get(i).get(j);
                System.out.print("Neuron-" + (j+1) + ": " + n.getInLinks().size() + " inputs\t");
                for(int k=0;k<n.getInLinks().size();k++)
                    System.out.print(n.getInLinks().get(k).getWeight() + "\t");
                System.out.println("");
            }
        }
    }
    protected void printWeightVector()
    {
		/*double[] w = new double[features.length];
		for(int j=0;j<inputLayer.size()-1;j++)
		{
			w[j] = inputLayer.get(j).getOutLinks().get(0).getWeight();
			System.out.print(w[j] + " ");
		}*/
        for(int j=0;j<outputLayer.get(0).getInLinks().size();j++)
            System.out.print(outputLayer.get(0).getInLinks().get(j).getWeight() + " ");
        System.out.println("");
    }


}
