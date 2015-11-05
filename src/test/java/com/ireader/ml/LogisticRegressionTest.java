package com.ireader.ml;

import java.util.Arrays;

/**
 * Created by zxsted on 15-10-12.
 *
 *
 * 使用 SOFTMAX 来实现 logistic regression
 */
public class LogisticRegressionTest {

    public int N;
    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;

    public LogisticRegressionTest(int train_N, int n_in, int n_out){
        this.N = train_N;
        this.n_in = n_in;
        this.n_out = n_out;

        W = new double[this.n_out][this.n_in];
        b = new double[this.n_out];
    }


    //
    public double[][] gradient(double[] x,double[] y,double[] dy) {
        double[] p_y_given_x = new double[n_out];
//        double[] dy = new double[n_out];   // 梯度

        double[][] grad = new double[n_out][n_in + 1];  // +1 是偏置 bias bias 放在后面

        // 计算线性输出
        for (int i = 0 ; i < n_out;i++) {
            p_y_given_x[i] = 0;
            for(int j = 0 ; j < n_in;j++) {
                p_y_given_x[i] += W[i][j];
            }
            p_y_given_x[i] += b[i];
        }


        // 将线性输出 改为 softmax 概率形式
        softmax(p_y_given_x);

        // 计算梯度
        for(int i = 0 ; i < n_out;i++) {
            dy[i] = y[i] - p_y_given_x[i];

            for(int j = 0 ; j < n_in; j++) {
                grad[i][j] += dy[i] * x[j];
            }

            grad[i][n_in] = dy[i];
        }

        return grad;
    }


    /**
     *  softmax 将线程输出转化成 softmax概率
     * */
    public void softmax(double[] x) {
        double max = 0.0;
        double sum = 0.0;

        for (int i = 0 ; i < n_out; i++)
            if (max < x[i])
                max = x[i];

        for(int i = 0 ; i < n_out;i++) {
            x[i] = Math.exp(x[i] - max);
            sum += x[i];
        }

        for(int i = 0; i < n_out; i++)
            x[i] /= sum;
    }

    /**
     *  更新权重
     * */
    public void updateWeight(double[][] grad,double lr) {

        for(int i = 0; i < n_out; i++) {
            for(int j = 0 ; j < n_in;j++) {
                W[i][j] += lr * grad[i][j] ;
            }

            b[i] += lr * grad[i][n_in];
        }
    }


    /**
     *  训练一条数据
     * */
    public double[] train(double[] x,double[] y,double lr) {

        double[] dy = new double[n_out];
        double[][] grad = gradient(x,y,dy);

        updateWeight(grad,lr);

        return dy;
    }

    /**
     * 预测一条数据
     * */
    public void predict(double[] x,double[] y) {
        for(int i = 0 ; i < n_out;i++) {
            y[i] = 0;
            for(int j = 0 ; j < n_in;j++) {
                y[i] += W[i][j] * x[j];
            }

            y[i] += b[i];
        }

        softmax(y);
    }

    private static void test_lr() {
        double learning_rate = 0.1;
        double n_epochs = 500;

        int train_N = 6;
        int test_N = 2;
        int n_in = 6;
        int n_out = 2;

        double[][] train_X = {
                {1, 1, 1, 0, 0, 0},
                {1, 0, 1, 0, 0, 0},
                {1, 1, 1, 0, 0, 0},
                {0, 0, 1, 1, 1, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 1, 1, 1, 0}
        };

        double[][] train_Y = {
                {1, 0},
                {1, 0},
                {1, 0},
                {0, 1},
                {0, 1},
                {0, 1}
        };

        LogisticRegressionTest classifier = new LogisticRegressionTest(train_N, n_in, n_out);

        /**
         *  训练
         * */
        for (int epoch = 0 ; epoch < n_epochs; epoch++) {
            for (int i = 0 ; i < train_N; i++){
                double[] dy = classifier.train(train_X[i],train_Y[i],learning_rate);

//                System.out.println("current delta is :" + Arrays.toString(dy));
            }
        }

        // test data
        double[][] test_X = {
                {1, 0, 1, 0, 0, 0},
                {0, 0, 1, 1, 1, 0}
        };

        double[][] test_Y = new double[test_N][n_out];

        // test
        for(int i=0; i<test_N; i++) {
            classifier.predict(test_X[i], test_Y[i]);
            for(int j=0; j<n_out; j++) {
                System.out.print(test_Y[i][j] + "\t");
            }
            System.out.println();
        }

        System.out.println("===========================================");
        double[][] dtrain_Y = new double[train_N][n_out];
        // test
        for(int i=0; i<train_N; i++) {
            classifier.predict(train_X[i], dtrain_Y[i]);
            for(int j=0; j<n_out; j++) {
                System.out.print(dtrain_Y[i][j] + "\t");
            }
            System.out.println();
        }

    }

    public static void main(String[] args) {
        test_lr();
    }



}
