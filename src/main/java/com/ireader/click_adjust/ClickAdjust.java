package com.ireader.click_adjust;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-31.
 *
 *
 * 点击修正函数 ， 给出 alpha belta 值
 */
public class ClickAdjust {


    //private double alpha;
    //private double belta;

    public ClickAdjust() {};


    public List<Double> estimate(List<Double> views,List<Double> clicks ,double initalpha,double initbelta) throws Exception {

        List<Double> retList = new ArrayList<Double>();

        if(views.size() != clicks.size() || views.size() < 10) {
            throw new Exception("Invalid input.");
        }

        double a = initalpha;
        double b = initbelta;

        int num_iterations = 10000000;
        while(num_iterations-- > 0) {

            double psi_a = Psi(a);
            double psi_b = Psi(b);
            double psi_ab = Psi(a+b);

            double a_numerator = 0.0;
            double denominator = 0.0;
            double b_numerator = 0.0;


            for ( int i = 0 ; i < views.size(); ++i) {
                a_numerator += Psi((double)clicks.get(i) + a)- psi_a;
                b_numerator += Psi((double)views.get(i) + clicks.get(i) + b) - psi_b;
                denominator += Psi((double)views.get(i) + a+b) - psi_ab;
            }

            double new_a = a*a_numerator / denominator;
            double new_b = a*b_numerator / denominator;

            if(isMinDiff(new_a,a) && isMinDiff(new_b,b)) {
                break;
            }

            a = new_a;
            b = new_b;
        }
            retList.add(a);
            retList.add(b);

        return retList;

    }


    /**
     * 两个数 是否足够接近
     * */
    private boolean isMinDiff(double x ,double y) {
        return Math.abs(x - y) < 1e-9;
    }


    /**
     *  计算 phi 函数 ， 即 伽马函数的偏导数
     * */
    private double Psi(double x) {

        double c = 8.5;
        double d1 = - 0.5772156649;
        double r;
        double s = 0.000001;
        double s3 = 0.08333333333;
        double s4 = 0.008333333333;
        double s5 = 0.003968253968;
        double value = 0.0;
        double y = x;


        // 如果参数小于 5 使用估计
        if( y <= s) {
            value = d1 - 1.0/y;
            return value;
        }

        // reduce to DIGAMA(x+N) where (x + n) >= c
        while(y < c)
        {
            value = value - 1.0/y;
            y = y + 1.0;
        }

        // 使用stirling·s (actuallly de moivre`s) expansion if argument  > c
        r = 1.0 / y;
        value = value + Math.log(y) - 0.5*r;
        r = r*r;
        value = value - r*(s3 - r*(s4 - r*s5));

        return value;
    }


}
