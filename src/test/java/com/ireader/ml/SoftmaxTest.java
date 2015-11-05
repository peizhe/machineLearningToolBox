package com.ireader.ml;



import static com.ireader.ml.Matrix.*;



/**
 * Created by zxsted on 15-10-27.
 *
 *
 * softmax 的测试函数
 */
public class SoftmaxTest {


    private double[][] xMat = null;
    private double[][] yMat = null;
    private int n_in = 0;
    private int n_out = 0;

    private double[][] wMat;
    private double[] barr;

    private double[][] d_y;

    private double lr=0.0;
    private double alpha = 0.0;
    private String reguler = "L2";


    private OperatorOnTwo L2 = new OperatorOnTwo() {

        final double L2_reg = alpha;
        @Override
        public double process(double a, double b) {
            return a - L2_reg * lr * a + lr * b;
        }
    };



    public SoftmaxTest(){}

    public SoftmaxTest( int n_in,int n_out,double lr,double alpha,String regulaer) {

        this.n_in = n_in;
        this.n_out = n_out;

        this.lr = lr;
        this.alpha = alpha;

        this.reguler = regulaer;

        this.wMat = Matrix.randomMatrix(n_in, n_out);
        this.barr = Matrix.randomArray(n_out);

    }


    public void train(double[][] xMat,double[] yArr, final double lr, final double L2_reg) {

        this.xMat = xMat;
        this.yMat = label4softmaxout(n_out,yArr);
        this.lr = lr;
        this.alpha = L2_reg;

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
        for (int i = 0 ; i < n_out; i++) {
            this.barr[i] += lr * gbarr[i];
        }


        double[][] newwMat = matrixOp(this.wMat, gmat, null, null, L2);

//        System.out.println("权重误差：" + sum(matrixOp(this.wMat,newwMat,null,null,minus)));
        this.wMat = newwMat;

        this.d_y = d_y;

    }

    /**
     *  预测接口
     * */
    public double[][] predict(double[][] xMat) {
        return output(xMat);
    }



    /**
     *  本层的输出
     * */
    public double[][] output(double[][] xMat) {

//        printMatrix(this.wMat);

        double[][] linearMat = dot(xMat, this.wMat);
        linearMat = addVec(linearMat, this.barr, 1);
        return softmax(linearMat);

    }

    /**
     *  softmax 函数
     * */
    public double[][] softmax(double[][] oMat) {

        double[][] retMat = null;

        final double maxval = max(oMat);

        double[][] adjustMat = matrixOp(oMat, new Operator() {
            @Override
            public double process(double value) {
                return value - maxval;
            }
        });

        double[][] eMat = matrixOp(adjustMat,exp);

        double[] rowsum = dimsum(eMat, 0);

        retMat = edivid(eMat, rowsum, 0);

        return retMat;
    }


    public double crossEntropy(double[][] xMat,double[] yArr) {

        double[][] yMat = label4softmaxout(this.n_out,yArr);

        double[][] outMat = this.output(xMat);


        double[][] logMat =  matrixOp(outMat, new Operator() {
            @Override
            public double process(double value) {
                return Math.log(value);
            }
        });

        double[][] elemprod = matrixOp(yMat, logMat, null, null, multiply);

        return -(1.0/xMat.length) * (sum(elemprod));
    }







    public void test(int epochs) {



        double[][] xMat = new double[100][];
        double[]   yArr = new double[100];

        loadData("/home/zxsted/data/cnn/iris.txt" ,xMat, yArr,0);

//        System.out.println("===============================================");
//        printMatrix(xMat);
//        System.out.println("-----------------------------------------------");
//        printArray(yArr);

        double[][] xtMat = new double[50][];
        double[]   ytArr = new double[50];

        loadData("/home/zxsted/data/cnn/iris.txt" ,xtMat, ytArr,100);


        ptestname("开始softmax的训练测试");


        double lastcost = Double.MAX_VALUE;
        for (int epoch = 0 ; epoch < epochs; epoch++) {


            for (int i = 0 ; i < 10; i++) {

                double[][] xbatch = getxBatch(xMat,10,i);
                double[] ybatch = getyBatch(yArr,10,i);

                train(xbatch, ybatch, lr, alpha);

            }

            lr *= 0.995;
            /**
             *  每隔10 次循环 调用一次损失计算函数
             * */
            if (epoch % 10 == 0) {
                double curcost = crossEntropy(xMat, yArr);
                System.out.println("Iterator :" + epoch + " 上一次损失值为：" + lastcost);
                System.out.println("Iterator :" + epoch + " 当前损失值为：" + curcost);

                if (Math.abs(lastcost - curcost)  < 0.01)  {
                    System.out.println("两次损失的误差足够小，结束循环！");
                    break;
                }
                lastcost = curcost;

            }
        }

        double pre_result[][] = predict(xtMat);

        for (int i = 0 ; i < pre_result.length; i++) {
            System.out.println(getMaxIndex(pre_result[i]) + " " + ytArr[i]);

        }



    }

    public static void main(String[] args) {

        SoftmaxTest softmax = new SoftmaxTest(4,3,0.5,0.01,"L2");
//        SoftmaxTest test = new SoftmaxTest();
        softmax.test(3000);

    }
}
