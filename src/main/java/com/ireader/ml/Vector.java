package com.ireader.ml;

/**
 * Created by zxsted on 15-9-1.
 */
public class Vector {


    /**
     * norm
     * */
    public static double norm(double[] weights) {

        double ret = 0.0;

        for (int i = 0 ; i < weights.length ; i++) {
            ret += weights[i]* weights[i];
        }

        return Math.sqrt(ret);
    }

    /**
     *  向量按元素相加
     * */
    public static  double[] vecadd(double[] vec1,double[] vec2) {
        double[] retvec = new double[vec1.length];

        for(int i = 0 ; i < vec1.length;i++) {
            retvec[i] = vec1[i] + vec2[i];
        }
        return retvec;
    }

    /**
     *  向量按元素 相减
     * */
    public static  double[] vecmimus(double[] vec1,double[] vec2) {
        double retvec[] = new double[vec1.length];

        for(int i = 0 ; i < vec1.length; i++) {
            retvec[i] = vec1[i] - vec2[i];
        }

        return retvec;
    }

    /**
     *  向量 按值 复制
     * */
    public static  double[]  veccopy(double[] vec){

        double[] retvec = new double[vec.length];

        for (int i = 0 ; i < vec.length; i++) {
            retvec[i] = vec[i];
        }

        return retvec;
    }

    /**
     *  数量乘以向量
     * */
    public static double[] numprod(double val, double[] vec) {
        double[] retvec =new double[vec.length];

        for (int i = 0 ; i < vec.length; i++) {
            retvec[i] = val * vec[i];
//            System.out.println("numprod : " + retvec[i]);
        }

        return retvec;
    }

    /**
     *  行向量乘以列向量
     * */
    public static double innerprod(double[] vec1,double[] vec2) {

        if (vec1.length != vec2.length) {
            System.out.println("lbfgs inner product two vector length is not same!");
            return Double.NaN;
        }

        double ret = 0.0;

        for (int i = 0 ; i < vec1.length; i++) {
            ret += vec1[i] * vec2[i];
        }

        return ret;
    }

    /**
     *   列向量乘以行向量
     * */
    public static double[][] outerprod(double[] vec1,double[] vec2) {

        int n = vec1.length;
        int m = vec2.length;

        double[][] retmat = new double[n][m];

        for (int i = 0 ; i < n ; i++)
            for(int j = 0 ; j < m; j++) {
                retmat[i][j] = vec1[i] * vec2[j];
            }

        return retmat;
    }
}
