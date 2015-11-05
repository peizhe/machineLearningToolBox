package com.ireader.ml;

import java.util.Arrays;
import java.util.Random;
import static com.ireader.ml.utils.*;


/**
 * Created by zxsted on 15-10-12.
 *
 * Hidden Layer
 */
public class HiddenLayer {

    public int N;
    public int n_in;
    public int n_out;
    public double [][] W;
    public double[] b;
    public Random rng;
    public String activation;
    public String dactivation;
    public double[][] grad ;

    public HiddenLayer(int N,int n_in,int n_out,double[][] W,double[] b, Random rng,String activation){
        this.N = N;
        this.n_in = n_in;
        this.n_out = n_out;


        if (rng == null) this.rng = new Random(1234);
        else this.rng = rng;

        if (W == null) {
            this.W = new double[n_out][n_in];
            double a = 1.0 / this.n_in;

            for (int i = 0 ; i  < n_out; i++) {
                for (int j = 0 ; j < n_in; j++) {
                    this.W[i][j] = uniform(-a,a,rng);
                }
            }
        } else {
            this.W = W;
        }

        this.grad = new double[n_out][n_in+1];

        if (b == null) this.b = new double[n_out];
        else this.b = b;

        this.activation = activation;
    }

    /**
     *  每次批处理时都要从新更新
     * */
    public void setGrad(double[][] grad) {
        this.grad = grad;
    }


    protected double activation(double x) {
        if(this.activation == "sigmod" || this.activation == null) {
           return sigmoid(x);
        } else if(this.activation == "tanh") {
            return tanh(x);
        }else if (this.activation == "ReLU") {
            return ReLU(x);
        } else {
            throw new IllegalArgumentException("activation function not supported"  );
        }
    }

    protected double dactivation(double x) {
        if(this.activation == "sigmod" || this.activation == null) {
            return dsigmoid(x);
        } else if(this.activation == "tanh") {
            return dtanh(x);
        }else if (this.activation == "ReLU") {
            return dReLU(x);
        } else {
            throw new IllegalArgumentException("activation function not supported"  );
        }
    }

    public double output(double[] input,double[] w,double b) {
        double linear_output = 0.0;
        for(int j = 0; j < n_in; j++) {
            linear_output += w[j] * input[j];
        }
        linear_output += b;

        return activation(linear_output);
    }

    public void forward(double[] input,double[] output) {
        for(int i = 0 ; i < n_out; i++) {
            output[i] = this.output(input,W[i],b[i]);
        }
    }

    public double[][] gradient(double[] input,double[] dy,double[] prev_layer_input,
                         double[] prev_layer_dy,double[][] prev_layer_W,double lr){

        double[][] grad = new double[n_out][n_in+1];

        if(dy == null) dy = new double[n_out];

        int prev_n_in = n_out;
        int prev_n_out = prev_layer_dy.length;   // 下一层的输出， prev_layer_dy 是下一层的 delta 是包含偏置的

        for(int i = 0 ; i < prev_n_in; i++) {
            dy[i] = 0;
            for (int j = 0 ; j < prev_n_out; j++) {
                dy[i] += prev_layer_dy[j] * prev_layer_W[j][i];
            }

            dy[i] *= dactivation(prev_layer_input[i]);

        }

//        System.out.println("==================================================");
//        System.out.println("current delta is :" + Arrays.toString(dy));

        for(int i = 0 ; i < n_out; i++) {
            for(int j = 0 ; j < n_in; j++) {
                grad[i][j] += dy[i] * input[j] / N;
            }
            grad[i][n_in] += dy[i] / N;
        }

        System.out.println("==================================================");
        System.out.println("current grad is :" + Arrays.toString(grad[0]));

        return grad;
    }

    public void updateWeight(double[][] grad,double lr) {

        for(int i = 0; i < n_out; i++) {
            for (int j = 0 ; j < n_in; j++) {
                W[i][j] += lr*grad[i][j];
            }
            b[i] += lr*grad[i][n_in];
        }
    }

    public void backward(double[] input,double[] dy,double[] prev_layer_input,
                    double[] prev_layer_dy,double[][] prev_layer_W,double lr){
        double[][] grad = gradient(input,dy,prev_layer_input,
                    prev_layer_dy,prev_layer_W, lr);

        updateWeight(grad,lr);

    }

    public int[] dropout(int size,double p,Random rng) {
        int[] mask = new int[size];

        for(int i = 0 ; i < size; i++) {
            mask[i] = binomial(1,p,rng);
        }

        return mask;
    }




}
