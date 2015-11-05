package com.ireader.ml;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by zxsted on 15-10-12.
 *
 * 多层感知机
 */
public class MLP {

    public int N;
    public int n_in;
    public int n_hidden;
    public int n_out;
    public HiddenLayer hiddenLayer;
    public LogisticRegressionTest softmaxLayer;
    public Random rng;

    public MLP(int N,int n_in,int n_hidden,int n_out,String activation,Random rng) {
        this.N = N;
        this.n_in = n_in;
        this.n_hidden = n_hidden;
        this.n_out = n_out;

        if(rng == null) rng = new Random(1234);
        this.rng = rng;

        this.hiddenLayer = new HiddenLayer(N, n_in, n_hidden, null, null, rng, activation);

        this.softmaxLayer = new LogisticRegressionTest(N,n_hidden,n_out);

    }

    public void train(double[][] train_X,double[][] train_Y,double lr) {
        double[] hidden_layer_input;
        double[] softmax_layer_input;
        double[] dy;
        for(int n = 0 ; n < N; n++) {
            hidden_layer_input = new double[n_in];
            softmax_layer_input = new double[n_hidden];

            for (int j = 0 ; j < n_in; j++)
                hidden_layer_input[j] = train_X[n][j];

            hiddenLayer.forward(hidden_layer_input,softmax_layer_input);

            dy = softmaxLayer.train(softmax_layer_input,train_Y[n],lr);

            hiddenLayer.backward(hidden_layer_input, null, softmax_layer_input, dy, softmaxLayer.W, lr);
        }
    }

    public void predict(double[] x,double[] y) {
        double[] softmax_layer_input = new double[n_hidden];
        hiddenLayer.forward(x,softmax_layer_input);
        softmaxLayer.predict(softmax_layer_input,y);
    }

    private static void test_mlp() {
        Random rng = new Random(1234);

        double learning_rate = 0.1;
        int n_epochs = 500;

        int train_N = 4;
        int test_N = 4;
        int n_in = 2;
        int n_hidden = 3;
        int n_out = 2;

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

        // construct MLP
        MLP classifier = new MLP(train_N, n_in, n_hidden, n_out,"tanh", rng);

        // train
        for(int epoch=0; epoch<n_epochs; epoch++) {
            classifier.train(train_X, train_Y, learning_rate);
        }

        // test data
        double[][] test_X = {
                {0., 0.},
                {0., 1.},
                {1., 0.},
                {1., 0},
        };

        double[][] test_Y = new double[test_N][n_out];


        // test
        for(int i=0; i<test_N; i++) {
            classifier.predict(test_X[i], test_Y[i]);
            for(int j=0; j<n_out; j++) {
                System.out.print(test_Y[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("====================================");

        double[][] ttrain_Y = new double[train_N][n_out];
        for(int i=0; i<train_N; i++) {
            classifier.predict(train_X[i], ttrain_Y[i]);
            System.out.print("input array : " + Arrays.toString(train_X[i]) + "\t");
            for(int j=0; j<n_out; j++) {
                System.out.print(ttrain_Y[i][j] + "---");
            }
            System.out.println();
        }


    }


    public static void main(String[] args) {
        test_mlp();
    }


}
