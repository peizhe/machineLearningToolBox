package com.ireader;

import static com.ireader.ml.Matrix.*;

/**
 * Created by zxsted on 15-11-1.
 */
public class MatrixRandTest {

    public static void main(String[] args) {
        double[][] rm = uniformMat(3, 4, 3);

        printMatrix(rm);

        double[][] gussian = gaussianMat(3,4,1,0.4);
        printMatrix(gussian);
    }
}
