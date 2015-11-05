package com.ireader.ranklearn.ranknet.model;

import com.ireader.ranklearn.ranknet.Ranker;
import com.ireader.ranklearn.ranknet.dto.RankList;

import java.util.List;

/**
 * Created by zxsted on 15-9-28.
 *
 * lambda Rank
 */
public class LambdaRank extends RankNet{

    protected float[][] targetValue = null;     // target value 是类属性

    public LambdaRank()
    {

    }

    public LambdaRank(List<RankList> samples,int[] features) {
        super(samples,features);
    }

    protected int[][] batchFeedForward(RankList rl)
    {
        int[][] pairMap = new int[rl.size()][];
        targetValue = new float[rl.size()][];
        for(int i = 0; i < rl.size(); i++)
        {
            addInput(rl.get(i));
            propagate(i);

            int count = 0;
            for(int j = 0;j < rl.size();j++)
                if (rl.get(i).getLabel() > rl.get(j).getLabel() || rl.get(i).getLabel() <
                        rl.get(j).getLabel())
                    count++;

            pairMap[i] = new int[count];
            targetValue[i] = new float[count];

            int k = 0;
            for(int j = 0 ; j < rl.size();j++)
                if (rl.get(i).getLabel() > rl.get(j).getLabel() || rl.get(i).getLabel() <
                        rl.get(j).getLabel())
                {
                    pairMap[i][k] = j;

                        if(rl.get(i).getLabel() > rl.get(j).getLabel())
                            targetValue[i][k] = 1;
                        else
                            targetValue[i][k] = 0;
                        k++;

                }
        }

        return pairMap;
    }

    protected void batchBackPropagate(int[][] pairMap,float[][] pairWeight)
    {
        for(int i = 0 ; i < pairMap.length;i++)
        {
            PropParameter p = new PropParameter(i,pairMap,pairWeight,targetValue);

            outputLayer.computeDelta(p);
            for(int j = layers.size() - 2; j >= 1;j--)
                layers.get(j).updateDelta(p);

            outputLayer.updateWeight(p);
            for(int j = layers.size() - 2; j >= 1; j--)
                layers.get(j).updateWeight(p);
        }
    }

    protected RankList internalRecorder(RankList rl)
    {
        return rank(rl);
    }

    protected float[][] computePairWeight(int[][] pairMap,RankList rl){

        double[][] changes = scorer.swapChange(rl);
        float[][] weight = new float[pairMap.length][];

        for(int i = 0; i < weight.length;i++)
        {
            weight[i] = new float[pairMap[i].length];
            for(int j = 0 ; j < pairMap[i].length;j++)
            {
                int sign = (rl.get(i).getLabel() > rl.get(pairMap[i][j]).getLabel())?1:-1;
                weight[i][j] = (float)Math.abs(changes[i][pairMap[i][j]]) * sign;
            }
        }

        return weight;
    }

    protected void estimateLoss()
    {
        misorderedPairs = 0;
        for(int j = 0 ; j < samples.size();j++)
        {
            RankList rl = samples.get(j);
            for(int k = 0; k < rl.size() - 1; k++)
            {
                double o1 = eval(rl.get(k));
                for(int l = k + 1; l < rl.size(); l++)
                {
                    if(rl.get(k).getLabel() > rl.get(l).getLabel()){
                        double o2 = eval(rl.get(l));
                        // error += crossEntropy(o1,o2,1.0f);
                        if(o1 < o2)
                            misorderedPairs++;
                    }
                }
            }

        }

        error = 1.0 - scoreOnTrainingData;
        if(error > lastError)
        {
            straightLoss++;
        }else{
            straightLoss = 0;
        }
        lastError = error;
    }

    public Ranker clone()
    {
        return new LambdaRank();
    }

    public String name(){
        return "LambdaRank";
    }
}
