package com.ireader.matrix.svd;

import java.util.ArrayList;
import java.util.List;

import static com.ireader.ml.Matrix.*;
import static com.ireader.ml.Vector.*;

/**
 * Created by zxsted on 15-10-30.
 */
public class QR {

    /**
     * Givens旋转又称为平面旋转变换，它能够消去给定向量的某一个分量（使其为0）,
     * 在处理有很多零元素的稀疏向量或者稀疏矩阵的时候Givens旋转就更加有效
     *
     * @param arr   double[] : 待处理向量
     * @param i     int: 旋转基准指的idx
     * @param j     int: 待消去分量的idx
     *
     * @return double[][] 对应的 givens 旋转矩阵
     */
    public static double[][] getGivensMat(double[] arr,int i,int j) {

        double[][] retMat = eyeMat(arr.length);

        double c = 0.0;
        double s = 0.0;

        if (Math.abs(arr[i]) > Math.abs(arr[j])) {
            double t = arr[j] / arr[i];
            c = 1.0 / Math.sqrt(1 + t*t);
            s = arr[j] / Math.sqrt(arr[i] * arr[i] + arr[j] * arr[j]);
        } else if (Math.abs(arr[i]) < Math.abs(arr[j])) {
            double t = arr[i] / arr[j];
            s = 1.0 / Math.sqrt(1 + t*t);
            c = arr[i] / Math.sqrt(arr[i] * arr[i] + arr[j] * arr[j]);
        } else {
            s = arr[j] / Math.sqrt(arr[i] * arr[i] + arr[j] * arr[j]);
            c = arr[i] / Math.sqrt(arr[i] * arr[i] + arr[j] * arr[j]);
        }

        retMat[i][i] = c;
        retMat[j][j] = c;
        retMat[j][i] = -s;
        retMat[i][j] = s;

        return retMat;
    }

    /** houseHolder transform : Householder变换是一个初等反射变换，用Householder矩阵左乘一个向量或者矩阵，
     * 即实现Householder变换
     *
     *  v=c+∥c∥_2e
     *             2
     *  H = I -   ----- * uu^T
     *            u^Tu
     *
     *   实际上不用显示计算出 H 矩阵， 只需要计算Hx : 全部是向量积计算 ， 没有矩阵计算
     *                2                            u^Tx
     *  H =  （I -   ----- * uu^T ） * x = x - 2 * ----- * u
     *               u^Tu                          u^Tu
     *
     *  但是为了 得到 Q 矩阵 所以还是用到 矩阵计算
     *
     * 将(nxn)矩阵A分解为正交阵Q和上三角阵R的过程需要n−1步
     *
     *  step 1: 向量c是矩阵A的第一列，向量e是长度为n的列向量，如果c的第一个元素是正数的话e的第一个元素是1，否则是-1
     *  得到c和e之后，便可以计算出v，然后得到矩阵H1
     *  将H1作用于A，此时Q1=H1,R1=H1A。其中Q1是正交阵，R1的第一列第一个元素以下的元素都是0。
     *
     *  step 2: 向量c是矩阵R1的第二列，并且第一个元素设置为0，向量e是长度为n的列向量，不过第二个元素是±1，如果c的第二
     *  个元素是正数的话e的第二个元素是1，否则是-1。得到c和e之后，便可以得到矩阵H2
     *  H2作用于R1，此时Q2=Q1H2,R2=H2R1。其中Q2是正交阵，R2的第二列第二个元素以下的元素都是0
     *
     *
     *  重复与上面类似的过程，直到第(n-1)步，得到Qn−1=Qn−2Hn−1=H1H2⋯Hn，同时Rn−1=Hn−1Rn−2=Hn−1Hn−2⋯H2H1，
     *  此时Rn−1是一个上三角阵，Qn−1是一个正交阵，得到矩阵A的QR分解式为A=Qn−1Rn−1。
     *
     *
     * @param arr  待旋转的列
     * @param i    不为0 的元素的idx
     * @return  double[][] Householder Matrix
     */
    public static double[][] oneStepHouseholder(double[] arr,int i) {

        int len = arr.length;

        double[] evec =  new double[len];
        evec[i] = (arr[i] > 0)? 1:-1;

        double norm = norm(arr);
        double[] uVec = vecadd(arr, numprod(norm, evec));

        final double a = 2. / innerprod(uVec,uVec);

        double[][] hMat = matrixOp(eyeMat(len), outerprod(uVec, uVec), null, new Operator() {
            @Override
            public double process(double value) {
                return a * value;
            }
        }, minus);

        return hMat;
    }


    /**
     * 使用 HouseHolder 变换对 矩阵aMat 进行QR 分解
     *
     * 分解结果保存在 Q矩阵和R矩阵中
     *
     * @param aMat
     * @param Q
     * @param R
     */
    public static void HouseHolder(double[][] aMat,double[][] Q,double[][] R) {

        int m = aMat.length;             // 矩阵行个数
        int n = aMat[0].length;          // 矩阵列个树

        double[][] hMat = null;
        double[][] rMat = aMat;
        double[][] qMat = new double[m][m];        // Q 矩阵计算的栈存数组


        double[][] taMat = T(aMat);

        for (int i = 0 ; i < n; i++ ) {

            double[] arr = taMat[i];            // 取出当前列

            hMat = oneStepHouseholder(arr,i);   // 计算当前列的HouseHolder变换

            rMat = matrixOp(hMat, rMat, null, null, multiply);
            qMat = (i == 0)?hMat:matrixOp(qMat,hMat,null,null,multiply);
        }

        for (int i = 0 ; i < m; i++)
            for (int j = 0 ; j < m; j++)
                Q[i][j] = qMat[i][j];

        for (int i = 0 ; i < m; i++)
            for (int j = 0 ; j < n; j++)
                R[i][j] = rMat[i][j];

    }

    /**
     *  Lanczos算法是一种将对称矩阵通过正交相似变换变成对称三对角矩阵的算法
     *
     *  可应用于对称矩阵线性方程组求解的Krylov子空间方法以及对称矩阵的特征值问题
     *
     * @param aMat double[][] : 对称举证
     * @param triMat double[][] :
     * @param vecArr： List<double[]> ：
     * @return
     */
    public static void lanczos(double[][] aMat,double[][] triMat,List<double[]> vecArr) {

        int len = aMat.length;

        double[][] TripeMat = new double[len][len];

        double[] alpha = new double[len];
        double[] beta  = new double[len];

        double[] vec1 = randomArray(len);
        double norm = norm(vec1);
        vec1 = numprod(1./norm,vec1);

        double[] vec0 = zeroArray(len);

        vecArr.add(vec0);
        vecArr.add(vec1);

        double belta1 = 0.;


        double wi[] = null;

        for (int i = 0;i < len-1; i++) {
            wi = dot(aMat,vecArr.get(i+1));
            alpha[i] = innerprod(wi,vecArr.get(i+1));

            // w_i  <- wi^T - a_i * v_i - b_i * v_i
            wi = vecmimus(wi,vecadd(numprod(alpha[i],vecArr.get(i+1)),numprod(beta[i],vecArr.get(i))));

            beta[i+1] = norm(wi);
            vecArr.add(numprod(1./beta[i+1] , wi));
        }

        wi = dot(aMat, vecArr.get(len));
        alpha[len] = innerprod(wi,vecArr.get(len));


        /** ================== generate triMat ============================================ */
        for (int i = 0 ; i < len; i++) {
            triMat[i][i] = alpha[i];
            triMat[i][i+1] = beta[i+1];
            triMat[i+1][i] = beta[i+1];
        }

    }





    /** ============ test main ================================================================= */

    public static void main(String[] args) {

        // TODO : test Givens
        double[] arr = randomArray(10);

        printArray(arr);

        double[][] gMat = getGivensMat(arr, 1, 5);

        printMatrix(gMat);

        printArray(dot(gMat,arr));


        // TODO : test Houseolder



        // TODO : test lanczos
    }
}
