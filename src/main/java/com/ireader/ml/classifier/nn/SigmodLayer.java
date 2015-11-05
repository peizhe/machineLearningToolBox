package com.ireader.ml.classifier.nn;


import java.util.Arrays;

import static com.ireader.ml.Matrix.*;
import static com.ireader.ml.classifier.nn.LossFunction.*;
import static com.ireader.ml.classifier.nn.Activation.*;

/**
 * Created by zxsted on 15-10-15.
 */
public class SigmodLayer extends Layer{

    // vars

    private double[][] xMat = null;      // 样本特征批次矩阵
    private double[] yArr = null;        // 样本标记
    private double[][] aout = null;         // 神经节点输出数据矩阵

    private double[][] deltaMat = null;  // 节点 delta 矩阵

    private double[][] wMat = null;      // 权重矩阵 size ：   labelnum * featnum
    private double[] bias = null;        // 偏置数组

    private double[][] dWMat = null;     // 权重梯度矩阵
    private double[] dbias   = null;        // 偏置梯度数组

    // parameters

    private int nout = 0;            // 类别个数
    private int nin = 0;             // 特征个数
    private String normal = "L2";          // 正则化类型 ()
    private double lambd ;               // 正则化系数
    private double lr;                   // 学习速率


    // L2 正则化操作
    private OperatorOnTwo L2 = new OperatorOnTwo() {


        @Override
        public double process(double a, double b) {
            return a - lr * lambd * a + lr * b;
        }
    };

    // L1 正则化操作
    private OperatorOnTwo L1 = new OperatorOnTwo(){


        @Override
        public double process(double a, double b) {
            return  a - lambd* lr  * (a==0?a:((a>0)?1:-1)) + lr * b;
        }
    };



    public SigmodLayer(){}

    public SigmodLayer(int n_in,int n_out){

        setSize(n_in,n_out);

    }

    public SigmodLayer setSize(int n_in,int n_out) {

        this.nout = n_out;
        this.nin = n_in;

        if(this.wMat == null ) {
            this.wMat = randomMatrix(nin, nout);

            this.bias = randomArray(n_out);

            this.dWMat = zeroMat(nin, nout);
            this.dbias = zeroArray(nout);
        }

        return this;
    }

    /**
     * 为 MLP 提供的函数
     *  前向计算 ： 返回本层的输出
     * */
    public double[][] forward(double[][] xmat) {

        this.xMat = xmat;

        this.aout = this.output(xmat);
        return this.aout;
    }

    /**
     * 为 MLP 提供的函数
     * 为lastLayer 计算 err
     * */
    public double[][] backward(double[][] xMat,double[] yArr) {

        this.xMat = xMat;
        this.yArr = yArr;

        this.deltaMat = errorcompute(xMat, yArr);

        return dot(deltaMat, T(this.wMat));
    }

    /**
     * @param xMat : double[][] size is (m,n_in)
     *
     * @return predictMat : double[][] size is (m, n_out)
     * */
    private double[][] output(double[][] xMat) {

        double[][] linearMat = dot(xMat, this.wMat);
        linearMat = addVec(linearMat, this.bias, 1);

        return sigmod(linearMat);
    }

    private double[][] predict(double[][] xMat) {

        this.xMat = xMat;

        return this.output(xMat);
    }

    /**
     * 损失函数
     * @param xMat : double[][] size is (m,n_in)
     * @paran yArr : double[]   size is (m,1)
     *
     * return crossEntropy : double
     * */
    public double costFunc(double[][] xMat,double[] yArr) {

        this.xMat = xMat;
        this.yArr = yArr;

        // 将标签扩展为 目标矩阵
        double[][] targetMat = label4softmaxout(this.nout, yArr);

        double[][] preMat = output(xMat);

        double crossEntropy =  crossEntropy(preMat, targetMat);

        return crossEntropy;
    }

    /**
     * 误差计算函数（delta） ， 也可以用于准确律的计算
     * @param xMat : double[][] size is (m,n_in)
     * @param yArr : double[]   size is (m,1)
     *
     *             error is delta
     * @return errorMat :double[][] size is (m,n_out)
     * */
    public  double[][] errorcompute(double[][] xMat,double[] yArr) {

        // 将标签扩展为 目标矩阵
        double[][] targetMat = label4softmaxout(this.nout, yArr);

        double[][] preMat = output(xMat);

        double[][] dsigmodMat = matrixOp(preMat, new Operator() {
            @Override
            public double process(double value) {
                return value*(1-value);
            }
        });

        double[][] errorMat = deltaCrossEntropy(preMat, targetMat);

        double[][] dMat = matrixOp(dsigmodMat,errorMat,null,null,multiply);

        return dMat;
    }

    /**
     * 梯度计算函数
     *
     *
     * @param xMat : double[][] size is (m,n_in)
     * @param yArr : double[]   size is (m,1)
     *
     *             error is delta
     * @return grad :double[]
     * */
    public double[] gradient(double[][] xMat,double[] yArr) {

        this.xMat = xMat;
        this.yArr = yArr;

        final int m = xMat.length;

        // size is (m,n_out)
        double[][] errorMat = errorcompute(xMat, yArr);

        // T(xMat) size is (n_in,m)  wgradMat size is : (n_in,n_out)
        double[][] wgradMat = dot(T(xMat), errorMat);

        double[] baisgrad  = dimsum(matrixOp(errorMat, new Operator() {
            @Override
            public double process(double value) {
                return value / m;
            }
        }), 1);

        double[][] biasgradMat = new double[1][];
        biasgradMat[0] = baisgrad;

        this.dWMat = wgradMat;
        this.dbias = baisgrad;

        return reval(hstack(wgradMat,biasgradMat));
    }


    /**
     *  权重和偏置的更新函数
     *
     * */
    public double[] updateWeight(double[] grad) {

        int nin = this.nin;
        int nout = this.nout;

        double[][] gradMat =  reSize(grad, nin + 1, nout);

        double[][] wgradMat = slice(gradMat, 0, nin, 0);
        double[][] baisgradMat = slice(gradMat, nin, nin + 1, 0);


        double[][] biasMat = new double[1][];
        biasMat[0] = this.bias;

        double[][] newbiasMat = matrixOp(biasMat, baisgradMat, null, new Operator() {
            @Override
            public double process(double value) {
                return value * lr;
            }
        }, plus);

        double[][] newWeightMat = (this.normal.equalsIgnoreCase("L1"))?
                matrixOp(this.wMat, wgradMat, null, null, L1):matrixOp(this.wMat,wgradMat,null,null,L2);

        this.bias = newbiasMat[0];
        this.wMat = newWeightMat;

        return reval(hstack(newWeightMat,newbiasMat));
    }

    /**
     *  更新同一接口 ： 训练调用
     * */
    public void updates(final double[][] xMat,double[] yArr) {

        double[] grads =  gradient( xMat,yArr);
        double[] newWeights =  updateWeight(grads);
    }


    /**
     *  训练一个批次
     *  @param lr : double 学习速率
     *  @param lambda : double 正则化系数
     * */
    public void train( double[][] xMat,double[] yArr,double lr,double lambda ){

        this.lr = lr;
        this.lambd  = lambda;
        updates(xMat,yArr);
    }



    /** ===============================================================================================*/


    public void test(int epochs, double lr,double lambda) {

        double[][] xMat = new double[100][];
        double[] yArr = new double[100];

        loadData("/home/zxsted/data/cnn/iris.txt", xMat, yArr, 0);

        double lastcost = Double.MAX_VALUE;
        for (int epoch = 0 ; epoch < epochs; epoch++) {

            double[][] oldwMat = cloneMatrix(this.wMat);
            for (int i = 0 ; i < 10; i++) {

                double[][] xbatch = getxBatch(xMat,10,i);
                double[] ybatch = getyBatch(yArr,10,i);

                train(xbatch, ybatch, lr, lambda);
            }

            lr *= 0.995;

            /** 每隔10 次循环 调用一次损失计算函数*/
            if (epoch % 10 == 0) {
                double curcost = costFunc(xMat, yArr);
                System.out.println("Iterator :" + epoch + " 当前损失值为：" + curcost);

                if (Math.abs(curcost - lastcost) < 0.001) {
                    System.out.println("损失函数值前后两次相差小于阈值，提前退出！");
                    break;
                }
                lastcost = curcost;
            }
        }


        double[][] txMat = new double[50][];
        double[] tyArr = new double[50];

        loadData("/home/zxsted/data/cnn/iris.txt", txMat, tyArr,100);

        double[][] result = predict(txMat);

        double[] preArr = new double[tyArr.length];

        double pre_result[][] = predict(txMat);

        int count = 0;
        for (int i = 0 ; i < pre_result.length; i++) {
            if (getMaxIndex(pre_result[i]) == tyArr[i]) count++;
        }
        System.out.println("正确率为：" + count/(double) pre_result.length);

//        for (int i = 0 ; i < pre_result.length; i++) {
//            System.out.println(getMaxIndex(pre_result[i]) + " " + tyArr[i]);
//            System.out.println(Arrays.toString(pre_result[i]) + " " + tyArr[i]);
//
//        }
    }



    public static void main(String[] args) {

        SigmodLayer layer = new SigmodLayer();

        layer.setSize(4,3);
        layer.test(5000, 0.4,0.01);
    }




    /** ============ setter and getter ==================================================*/
    public SigmodLayer setdWMat(double[][] dWMat) {
        this.dWMat = dWMat;
        return this;
    }

    public SigmodLayer setwMat(double[][] wMat) {
        this.wMat = wMat;
        return this;
    }

    public SigmodLayer setBias(double[] bias) {
        this.bias = bias;
        return this;
    }

    public SigmodLayer setDb(double[] db) {
        this.dbias = db;
        return this;
    }

    public SigmodLayer setNout(int nout) {
        this.nout = nout;
        return this;
    }

    public SigmodLayer setNin(int nin) {
        this.nin = nin;
        return this;
    }

    public SigmodLayer setNormal(String normal) {
        this.normal = normal;
        return this;
    }

    public SigmodLayer setLambd(double lambd) {
        this.lambd = lambd;
        return this;
    }

    public SigmodLayer setLr(double lr) {
        this.lr = lr;
        return this;
    }
}
