package com.ireader.ml;

import static com.ireader.ml.Matrix.*;
/**
 * Created by zxsted on 15-10-27.
 */
public class MatrixTest {

    public static void main(String args[]) {

        ptestname("测试 label4mat");
        double  yArr[] = new double[]{0,1,3,2,0,3,4,1,0,2,3,0,4,1,2};

        double[][] lmat = label4softmaxout(5, yArr);

        printMatrix(lmat);

        ptestname("测试矩阵相减");
        double[][] five = fill(5,5,5);
        double[][] eye5 = eyeMat(5);

        double[][] retMat5 = matrixOp(five, eye5, null, null, minus);
        printMatrix(retMat5);

        ptestname("测试矩阵内积");

        double[][] ones = oneMat(5,5);
        double[][] inprod = dot(retMat5, eye5);
        printMatrix(inprod);

        ptestname("测试矩阵转置");
        double[][] trmat = fill(1,5,7);
        printMatrix(trmat);
        printMatrix(T(trmat));

        ptestname("测试维度相加");

        double[] col = dimsum(trmat, 0);
        printArray(col);
        double[] row = dimsum(trmat,1);
        printArray(row);

        ptestname("测试 addVec");
        printMatrix(addVec(trmat, col, 0));
        printMatrix(addVec(trmat, row, 1));

        ptestname("测试 edivide");
        printMatrix(edivid(trmat, col, 0));
        printMatrix(edivid(trmat, row, 1));


    }
}
