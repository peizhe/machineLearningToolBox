package com.ireader.ml.classifier.nn;

import com.ireader.ml.Matrix;

import static com.ireader.ml.classifier.nn.Activation.*;

import static com.ireader.ml.Matrix.*;

/**
 * Created by zxsted on 15-10-29.
 */
public class HiddenLayer {

    private double[][] xMat = null;                     // 样本特征批次矩阵
    private double[]   yArr = null;                     // 样本标记
    private double[][] aout = null;                     // 神经节点输出数据矩阵

    private double[][] deltaMat = null;                 // 节点 delta 矩阵

    private double[][] wMat = null;                     // 权重矩阵 size ：   labelnum * featnum
    private double[]   bias = null;                     // 偏置数组

    private double[][] dWMat = null;                    // 权重梯度矩阵
    private double[]   dbias   = null;                  // 偏置梯度数组

    private ActorType actType = ActorType.sigmod;       // 激活函数，默认使用sigmod函数

    // parameters
    private int nout = 0;                               // 类别个数
    private int nin = 0;                                // 特征个数
    private String  normal = "L2";                      // 正则化类型
    private double  lambd ;                             // 正则化系数
    private double  lr;                                 // 学习速率
    private boolean dropout = false;                    // 是否使用dropout 默认不使用

    private Operator actor = Activation.sigmod;         // 激活函数
    private Operator dactor = Activation.dsigmod;       // 激活函数的导数


    // L2 正则化操作
    private Matrix.OperatorOnTwo L2 = new Matrix.OperatorOnTwo() {


        @Override
        public double process(double a, double b) {
            return a - lr * lambd * a + lr * b;
        }
    };

    // L1 正则化操作
    private Matrix.OperatorOnTwo L1 = new Matrix.OperatorOnTwo(){


        @Override
        public double process(double a, double b) {
            return  a - lambd* lr  * (a==0?a:((a>0)?1:-1)) + lr * b;
        }
    };


    public HiddenLayer() {}

    public HiddenLayer(int n_in,int n_out) {
        setSize(n_in,n_out);
    }

    public HiddenLayer setSize(int n_in,int n_out) {

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
     *  计算隐藏层的前向输出
     * @param xMat
     * @return
     */
    private double[][] output(double[][] xMat) {

        if (xMat == null)
            throw  new RuntimeException("输入矩阵为空！");

        double[][] linear_out = addVec(dot(xMat,this.wMat),this.bias,0);

        return matrixOp(linear_out,this.actor);
    }

    public double[][] forward(double[][] xMat) {
        return output(xMat);
    }

    /**
     * 向上一层返回 误差
     * @param aout
     * @param errMat double[][] : size is (m,n_out)
     * @return
     */
    public double[][] backward(double[][] aout,double[][] errMat,double[][] dropMask) {

        // dAmat size is (m,n_out)
        double[][]  dAmat= matrixOp(aout,this.dactor);

        double[][] curLayerErrMat = matrixOp(dAmat,errMat,null,null,multiply);

        if (this.dropout == true) {
            curLayerErrMat = matrixOp(curLayerErrMat, dropMask, null, null, multiply);
        }


        // (m,n_out) x (n_out,n_in) = (m,n_in)
        return dot(curLayerErrMat, T(wMat));
    }


    /**
     *
     * @param xMat double[][] : size is (m,n_in)
     * @param errMat
     * @return
     */
    public double[] gradient(double[][] xMat,double[][] errMat) {

        final int m = xMat.length;

        // (n_in,m) x (m,n_out) = (n_in,n_out)
        double[][] gradMat = dot(T(xMat),errMat);

        double[] dbarr = dimsum(matrixOp(errMat, new Operator() {
            @Override
            public double process(double value) {
                return value / m;
            }
        }),1);

        this.dWMat = gradMat;
        this.dbias = dbarr;

        double[][] dbMat = new double[1][];
        dbMat[0] = dbias;

        return reval(hstack(gradMat, dbMat));
    }


    public double[] updateWeight(double[] gradArr) {

        double[][] gradMat = reSize(gradArr,this.nin+1,this.nout);

        double[][] gwmat = slice(gradMat, 0, nin, 0);

        double[][] gbmat = slice(gradMat, nin, nin + 1, 0);

        double[][] oldBiasMat = new double[1][];

        final double lrate = this.lr;

        this.wMat = (this.normal.equalsIgnoreCase("L1"))?
                matrixOp(this.wMat,gwmat,null,null,L1):matrixOp(this.wMat,gwmat,null,null,L2);

        double[][] newBias = matrixOp(oldBiasMat, gbmat, null, null, new OperatorOnTwo() {
            @Override
            public double process(double a, double b) {
                return a + lrate*b;
            }
        });

        this.bias = newBias[0];

        return reval(hstack(this.wMat, newBias));
    }


    /**
     *  根据本层的 输出矩阵创建 mask
     * @param aout
     * @param p
     * @return
     */
    public double[][] dropout(double[][] aout, double p) {

        int m = aout.length;
        int n = aout[0].length;

        double[][] mask = new double[m][n];

        for (int i = 0 ; i < m ;i++)
            for (int j = 0 ; j < n; j++)
                mask[i][j] = binomial(1,1-p);

        return mask;
    }

    /** 根据输入进行采样
     *
     * @param aout
     * @return
     */
    public double[][] sample_h_given_v(double[][] aout) {

        int m = aout.length;
        int n = aout[0].length;

        double[][] retMat = new double[m][n];

        for (int i = 0 ; i < m ;i++)
            for (int j = 0 ; j < n; j++)
                retMat[i][j] = binomial(1,1-aout[i][j]);

        return retMat;
    }






    /** ================================================================================ */
    public double[][] getAout() {
        return aout;
    }

    public HiddenLayer setAout(double[][] aout) {
        this.aout = aout;
        return this;
    }

    public double[][] getDeltaMat() {
        return deltaMat;
    }

    public HiddenLayer setDeltaMat(double[][] deltaMat) {
        this.deltaMat = deltaMat;
        return this;
    }

    public double[][] getwMat() {
        return wMat;
    }

    public HiddenLayer setwMat(double[][] wMat) {
        this.wMat = wMat;
        return this;
    }

    public double[] getBias() {
        return bias;
    }

    public HiddenLayer setBias(double[] bias) {
        this.bias = bias;
        return this;
    }

    public double[][] getdWMat() {
        return dWMat;
    }

    public HiddenLayer setdWMat(double[][] dWMat) {
        this.dWMat = dWMat;
        return this;
    }

    public double[] getDbias() {
        return dbias;
    }

    public HiddenLayer setDbias(double[] dbias) {
        this.dbias = dbias;
        return this;
    }

    public ActorType getActType() {
        return actType;
    }

    public HiddenLayer setActType(ActorType _actType) {

        this.actType = _actType;

        switch(this.actType) {
            case sigmod:
                this.actor = Activation.sigmod;
                this.dactor = Activation.dsigmod;
                break;
            case tanh:
                this.actor = Activation.tanh;
                this.dactor = Activation.dtanh;
                break;
            case ReLU:
                this.actor = Activation.ReLU;
                this.dactor = Activation.dReLU;
                break;
            default:
                break;
        }
        return this;
    }

    public String getNormal() {
        return normal;
    }

    public HiddenLayer setNormal(String normal) {
        this.normal = normal;
        return this;
    }

    public double getLambd() {
        return lambd;
    }

    public void setLambd(double lambd) {
        this.lambd = lambd;
    }

    public double getLr() {
        return lr;
    }

    public HiddenLayer setLr(double lr) {
        this.lr = lr;
        return this;
    }
}
