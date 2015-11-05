package com.ireader.ranklearn.ranknet.model;

/**
 * Created by zxsted on 15-9-28.
 *
 *  面向 list 的网络使用的neuron
 */
public class ListNeuron  extends Neuron{

    protected double[] d1;
    protected double[] d2;

    public void computeDelta(PropParameter param)
    {
        double sumLabelExp = 0;
        double sumScoreExp = 0;

        for(int i = 0 ; i < outputs.size(); i++)
        {
            sumLabelExp += Math.exp(param.labels[i]);
            sumScoreExp += Math.exp(outputs.get(i));
        }

        d1 = new double[outputs.size()];
        d2 = new double[outputs.size()];

        for(int i = 0 ; i < outputs.size(); i++)
        {
            d1[i] = Math.exp(param.labels[i])/sumLabelExp;
            d2[i] = Math.exp(outputs.get(i)) / sumScoreExp;
        }
    }

    public void updateWeight(PropParameter param)
    {
        Synapse s = null;

        for(int k = 0 ; k < inLinks.size();k++)
        {
            s = inLinks.get(k);
            double dw = 0;
            for(int l = 0 ; l < d1.length;l++)
                dw += (d1[l] - d2[l]) * s.getSource().getOutput(l);

            dw *= learningRate;

            s.setWeightAdjustment(dw);
            s.updateWeight();
        }
    }
}
