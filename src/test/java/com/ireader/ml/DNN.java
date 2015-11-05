package com.ireader.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zxsted on 15-10-13.
 *
 *  一个默认使用ReLU的 deep ann
 */
public class DNN {

    public int N;
    public int n_in;
    public int[] hidden_layer_sizes;   // 各个隐藏层的大小
    public int n_out;
    public int n_layers;
    public HiddenLayer[] hiddenLayers;
    public LogisticRegressionTest softmaxLayer;
    public Random rng;


    public DNN(int N,int n_in,int[] hidden_layer_sizes,int n_out,Random rng,String activation) {

        this.N = N;
        this.n_in = n_in;
        this.hidden_layer_sizes = hidden_layer_sizes;
        this.n_layers = hidden_layer_sizes.length;
        this.n_out = n_out;

        this.hiddenLayers = new HiddenLayer[n_layers];   // 暂存隐藏层

        if (rng == null) rng = new Random(1234);
        this.rng = rng;

        if (activation == null) activation = "ReLU";

        // 构建各个层
        int input_size;
        for (int i = 0; i < this.n_layers; i++) {
            if ( i == 0 ) {
                input_size = n_in;
            } else {
                input_size = hidden_layer_sizes[i-1];
            }

            this.hiddenLayers[i] = new HiddenLayer(N,input_size,hidden_layer_sizes[i],null,null,rng,activation);
        }

        // 构建分类器层
        this.softmaxLayer = new LogisticRegressionTest(N, hidden_layer_sizes[this.n_layers-1], n_out);

    }

    // 训练
    public void train(int epochs,double[][] train_X,double[][] train_Y,boolean  dropout,double p_dropout,double lr){

        List<int[]> dropout_masks;
        List<double[]> layer_inputs;
        double[] layer_input;
        double[] layer_output = new double[0];

        for (int epoch = 0; epoch < epochs; epoch++) {

            for (int n = 0 ; n < N; n++) {

                dropout_masks = new ArrayList<int[]>(n_layers);
                layer_inputs = new ArrayList<double[]>(n_layers + 1);

                // forward hiddenLayers
                for (int i = 0 ; i < n_layers;i++) {

                    if(i == 0)
                        layer_input = train_X[n];
                    else
                        layer_input = layer_output.clone();

                    layer_inputs.add(layer_input.clone());

                    layer_output = new double[hidden_layer_sizes[i]];
                    hiddenLayers[i].forward(layer_input,layer_output);

                    // 如果使用 dropout
                    if (dropout) {
                        int[] mask;
                        mask = hiddenLayers[i].dropout(layer_output.length,p_dropout,rng);

                        // dropout 向量直接与输出相乘
                        for(int j = 0 ; j < layer_output.length; j++)
                            layer_output[j] *= mask[j];

                        dropout_masks.add(mask.clone());   // 存储每次的 mask 向量
                    }

                }

                // forward & backward softmax Layer
                double[] logistic_layer_dy;
                logistic_layer_dy = softmaxLayer.train(layer_output,train_Y[n],lr);
                layer_inputs.add(layer_output.clone()); // 保存每次的输入

                // backforward HiddenLayer
                double[] prev_dy = logistic_layer_dy;
                double[][] prev_W;
                double[] dy = new double[0];

                for(int i = n_layers - 1; i >= 0; i--) {

                    if ( i == n_layers -1 ) {
                        prev_W = softmaxLayer.W;
                    } else {
                        prev_dy = dy.clone();
                        prev_W = hiddenLayers[i+1].W;
                    }

                    dy = new double[hidden_layer_sizes[i]];
                    hiddenLayers[i].backward(layer_inputs.get(i), dy, layer_inputs.get(i+1), prev_dy, prev_W, lr);

                    // 如果使用 dropout  那么 直接将其乘在 残差上
                    if(dropout) {
                        for (int j = 0 ; j < dy.length; j++) {
                            dy[j] *= dropout_masks.get(i)[j];
                        }
                    }
                }

            }
        }
    }


    public void pretest(double p_dropout) {

        for(int i = 0 ; i < n_layers; i++) {
            int in;
            int out;

            if ( i == 0) in = n_in;
            else in = hidden_layer_sizes[i];

            if ( i == n_layers - 1) out = n_out;
            else  out = hidden_layer_sizes[i+1];

            for(int l = 0 ; l < out; l++) {
                for (int m = 0 ; m < in; m++) {
                    hiddenLayers[i].W[l][m] *= 1 - p_dropout;
                }
            }
        }
    }


    public void predict(double[] x,double[] y) {
        double[] layer_input;
        double[] layer_output = new double[0];

        for (int i = 0 ; i < n_layers;i++) {

            if (i == 0) layer_input = x;
            else layer_input = layer_output.clone();

            layer_output = new double[hidden_layer_sizes[i]];

            hiddenLayers[i].forward(layer_input,layer_output);
        }

        softmaxLayer.predict(layer_output,y);
    }


    public static void test_dropout(){

        Random rng = new Random(123);

        double learning_rate = 0.1;
        int n_epochs = 5000;

        int train_N = 4;
        int test_N = 4;
        int n_in = 2;
        int[] hidden_layer_sizes = {10,10};
        int n_out = 2;

        boolean dropout = true;
        double p_dropout = 0.5;

        double[][] train_X = {
                {0., 0.},
                {0., 1.},
                {1., 0.},
                {1., 1.},
        };

        double[][] train_Y = {
                {0, 1},
                {1, 0},
                {1, 0},
                {0, 1},
        };

        DNN classifier = new DNN(train_N,n_in,hidden_layer_sizes,n_out,rng,"ReLU");

        classifier.train(n_epochs,train_X,train_Y,dropout,p_dropout,learning_rate);

        if (dropout) classifier.pretest(p_dropout);
    }



}

















