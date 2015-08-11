package com.ireader.local.ann.model;

/**
 * Created by zxsted on 15-8-3.
 */
public class ListNeuron extends Neuron{

    protected double[] d1;
    protected double[] d2;

    public void computedDelta(PropParameter param)
    {
        double sumLabelExp = 0;
        double sumScoreExp = 0;

        for(int i = 0 ; i < outputs.size(); i++){ //outputs[i] ==> the outut of the current neuron on the i-th document
            sumLabelExp += Math.exp(param.labels[i]);
            sumScoreExp += Math.exp(outputs.get(i));
        }

        d1 = new double[outputs.size()];
        d2 = new double[outputs.size()];

        for (int i = 0 ; i < outputs.size(); i++)
        {
            d1[i] = Math.exp(param.labels[i]) / sumLabelExp;
            d2[i] = Math.exp(outputs.get(i)) / sumScoreExp;
        }
    }

    public void updateWeight(PropParameter param) {
        Synapse s = null;
        for(int k = 0; k < inLinks.size();k++){
            s = inLinks.get(k);
            double dw = 0;
            for(int l = 0; l < d1.length;l++) {
                dw += (d1[l] - d2[l]) * s.getSource().getOutput();

            }

            dw *= learningRate;
            s.setWeightAdjustment(dw);
            s.updateWeight();
        }
    }

}
