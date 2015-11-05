package com.ireader.ml.classifier.nn;

import com.ireader.ml.Matrix;

import static com.ireader.ml.Matrix.*;
import static com.ireader.ml.Matrix.getMaxIndex;
import static com.ireader.ml.classifier.nn.LossFunction.crossEntropy;
import static com.ireader.ml.classifier.nn.LossFunction.deltaCrossEntropy;

/**
 * Created by zxsted on 15-10-29.
 */
public class OutputLayer {


    // vars

    protected double[][] xMat = null;         // 样本特征批次矩阵
    protected double[]   yArr = null;         // 样本标记
    protected double[][] aout = null;         // 神经节点输出数据矩阵

    protected double[][] deltaMat = null;     // 节点 delta 矩阵

    protected double[][] wMat = null;         // 权重矩阵 size ：   labelnum * featnum
    protected double[]   bias = null;         // 偏置数组

    protected double[][] dWMat = null;        // 权重梯度矩阵
    protected double[]   dbias   = null;      // 偏置梯度数组

    // parameters

    protected int nout = 0;                   // 类别个数
    protected int nin  = 0;                   // 特征个数
    protected String normal = "L2";           // 正则化类型 ()
    protected double lambd ;                  // 正则化系数
    protected double lr;                      // 学习速率


    // L2 正则化操作
    protected Matrix.OperatorOnTwo L2 = new Matrix.OperatorOnTwo() {


        @Override
        public double process(double a, double b) {
            return a - lr * lambd * a + lr * b;
        }
    };

    // L1 正则化操作
    protected Matrix.OperatorOnTwo L1 = new Matrix.OperatorOnTwo(){


        @Override
        public double process(double a, double b) {
            return  a - lambd* lr  * (a==0?a:((a>0)?1:-1)) + lr * b;
        }
    };



    public OutputLayer(){}

    public OutputLayer(int n_in,int n_out){

        setSize(n_in,n_out);

    }

    public OutputLayer setSize(int n_in,int n_out) {

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
    protected double[][] output(double[][] xMat) {

        double[][] linearMat = dot(xMat, this.wMat);
        linearMat = addVec(linearMat, this.bias, 1);
        return softmax(linearMat);
    }

    public double[][] predict(double[][] xMat) {
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
        double[][] targetMat = Matrix.label4softmaxout(this.nout, yArr);

        double[][] preMat = output(xMat);

        double[][] errorMat = deltaCrossEntropy(preMat, targetMat);

        return errorMat;
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
     *  测试使用
     * */
    public void train2(double[][] xMat,double[] yArr, final double lr, final double L2_reg) {

        this.xMat = xMat;
        this.yArr = yArr;
        double[][] yMat = label4softmaxout(nout,yArr);
        this.lr = lr;
        this.lambd = L2_reg;

        final int m = xMat.length;

        double[][] p_y_given_x = this.output(xMat);

        // 计算 error 矩阵
        double[][] d_y = matrixOp(yMat,p_y_given_x,null,null,minus);

        // 计算梯度
        double[][] gmat = dot(T(xMat),d_y);

        // 计算偏置的梯度
        double[] gbarr =  dimsum(matrixOp(d_y, new Operator() {
            @Override
            public double process(double value) {
                return value / m;
            }
        }),1);

        // 更新 偏置
        for (int i = 0 ; i < nout; i++) {
            this.bias[i] += lr * gbarr[i];
        }

        double[][] newwMat = matrixOp(this.wMat, gmat, null, null, L2);
        this.wMat = newwMat;
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
            /**
             *  每隔10 次循环 调用一次损失计算函数
             * */
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
//        }
    }



    public static void main(String[] args) {

        SoftMaxLayer layer = new SoftMaxLayer();

        layer.setSize(4,3);
        layer.test(5000, 0.4,0.01);


    }




    /** ============ setter and getter ==================================================*/
    public OutputLayer setdWMat(double[][] dWMat) {
        this.dWMat = dWMat;
        return this;
    }

    public OutputLayer setwMat(double[][] wMat) {
        this.wMat = wMat;
        return this;
    }

    public OutputLayer setBias(double[] bias) {
        this.bias = bias;
        return this;
    }

    public OutputLayer setDb(double[] db) {
        this.dbias = db;
        return this;
    }

    public OutputLayer setNout(int nout) {
        this.nout = nout;
        return this;
    }

    public OutputLayer setNin(int nin) {
        this.nin = nin;
        return this;
    }

    public OutputLayer setNormal(String normal) {
        this.normal = normal;
        return this;
    }

    public OutputLayer setLambd(double lambd) {
        this.lambd = lambd;
        return this;
    }

    public OutputLayer setLr(double lr) {
        this.lr = lr;
        return this;
    }
}
