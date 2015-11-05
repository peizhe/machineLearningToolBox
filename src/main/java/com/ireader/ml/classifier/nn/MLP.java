package com.ireader.ml.classifier.nn;

import java.util.List;

import static  com.ireader.ml.classifier.nn.Activation.*;

/**
 * Created by zxsted on 15-10-29.
 *
 * MLP: 多层感知机
 */
public class MLP {

    private double[][] xMat;
    private double[]   yArr;

    private int n_in;
    private int n_out;

    private ActorType actorType;

    private int[] hiddenLayerSizes ;
    private int nHiddenLayer;

    private List<HiddenLayer> hiddenLayers;
    private SoftMaxLayer softMaxLayer;
    private SigmodLayer sigmodLayer;
    private String outlayerType ;


    public MLP(int n_in,int n_out,int[] hiddenLayerSizes,String action) {

        this.n_in  = n_in;
        this.n_out = n_out;

        this.nHiddenLayer = hiddenLayerSizes.length;
        assert nHiddenLayer > 0: "没有设置隐藏层！";

        for (int i = 0 ; i < nHiddenLayer; i++) {

            int input_size = (0 == i)?n_in:hiddenLayerSizes[i-1];

            HiddenLayer hiddenLayer = new HiddenLayer(input_size,hiddenLayerSizes[i]);

            this.hiddenLayers.add(hiddenLayer);

        }

        if (outlayerType.equalsIgnoreCase("softmax"))
            this.softMaxLayer = new SoftMaxLayer(hiddenLayerSizes[hiddenLayerSizes.length-1],n_out);
        else if (outlayerType.equalsIgnoreCase("sigmod"))
            this.sigmodLayer = new SigmodLayer(hiddenLayerSizes[hiddenLayerSizes.length-1],n_out);
    }



}
