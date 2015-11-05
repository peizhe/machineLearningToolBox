package com.ireader.ml.optimize.conjugate;

import com.ireader.ml.optimize.Impl.testfunc.TestCost;
import com.ireader.ml.optimize.Impl.testfunc.TestGrad;
import com.ireader.ml.Vector;
import com.ireader.ml.optimize.lbfgs.CostFunc;
import com.ireader.ml.optimize.lbfgs.GradFunc;

import java.io.IOException;

/**
 * Created by zxsted on 15-9-1.
 */
public class ConjugateGrad {


    private CostFunc func = null;     // 损失函数计算公式
    private GradFunc gfunc = null;    // 梯度计算公式

    private double rou = 0.55;            // 用于一维搜索
    private double sigma = 0.4;           // 用于一维搜索

    private double epsilon = 1e-5;        // 判断是否迭代停止

    private double lambda = 1e-4;         // 迭代步长

    private boolean isonesearch = false;   // 是否是用一维搜索 ， 单机使用 ，分布式代价太大不建议使用 ，


    private double[] gk_1 = null;    // 上一次梯度的缓存
    private double[] dk_1 = null;

    public ConjugateGrad() {

    }


    /**
     * 使用 Armijo 进行一维搜索
     *
     * */
    private double Armijo(CostFunc func,GradFunc gfunc,double[][] featdata,double[] labels,double[] weight,double[] dk) throws InterruptedException, IOException, ClassNotFoundException {

        int m0 = 0;
        int mk = 0;

        while (m0 < 20) {

            if(func.compute(featdata,labels, Vector.vecadd(weight, Vector.numprod(Math.pow(rou, m0), dk))) <
                    func.compute(featdata,labels,weight)+ sigma * Math.pow(rou,m0) * Vector.innerprod(weight, dk)){
                mk = m0;
                break;
            }
            m0 += 1;
        }

        return Math.pow(rou,mk);
    }

    /**
     * 共轭方向计算函数
     *
     * @param func  : 损失函数
     * @param gfunc : 梯度计算函数
     * */
    public double[] compute(CostFunc func,GradFunc gfunc,double[][] featdata,double[] labels,
                            double[] weight,int m,int maxit) throws InterruptedException, IOException, ClassNotFoundException {

        int maxk = maxit;
        int k = 0;
        int flen = weight.length;                // 特征个数
        int itern = 0;

        double[] dk = new double[flen];         // 共轭方向

        double[] neweights = weight;


        while(k < maxk) {

            System.out.println("iter is :" + k);

            double[] gk = gfunc.compute(featdata, labels, neweights);   // 计算当前的梯度方向

            System.out.println("grad is :" + gk[0] + "\t" + gk[1]);

            itern += 1;
            itern %= flen ;

            if (itern == 1) {     // 如果是第一次循环
                dk = Vector.numprod(-1.0, gk);
                this.dk_1 = dk;
            System.out.println("dk is :" + Vector.numprod(-1.0, gk)[0] + "\t" + Vector.numprod(-1.0, gk)[1]);
                this.gk_1 = gk;    // 缓存 第一次梯度
            } else {

                double beta = 1.0 * Vector.innerprod(gk, gk) / Vector.innerprod(this.gk_1, this.gk_1);

                System.out.println("beta is :" + beta);

                dk = Vector.vecadd(Vector.numprod(-1.0, gk), Vector.numprod(beta, this.dk_1));
                double gd = Vector.innerprod(gk, dk);     //
                if(gd >= 0.0) {     // 如果因为累积误差 使得反向不再共轭， 那么使用梯度重新开始计算共轭
                    dk = Vector.numprod(-1.0, gk);
                }

                if (Vector.norm(gk) < epsilon) {
                    break;
                }

                if(isonesearch)     // 如果在单机的情况下 ， 使用一维搜索可以得到更加准确的方向， 但分布式情况下， 计算代价太大不建议使用
                        lambda = Armijo(func,gfunc,featdata,labels,weight,dk);

                neweights = Vector.vecadd(neweights, Vector.numprod(lambda, dk));

            }

            this.gk_1 = gk;         // 缓存本次 计算的梯度
            this.dk_1 = dk;
            k +=1;
        }

        return neweights;
    }


    /**
     *  测试部分
     *
     * */

    public static void main(String[] args) {

        CostFunc func = new TestCost();
        GradFunc gfunc = new TestGrad();

        ConjugateGrad cg = new ConjugateGrad();

        double[] w0 = new double[]{0.0,0.0};

        double[] w = new double[0];
        try {
            w = cg.compute(func,gfunc,null,null,w0,20,5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("result is :" + w[0] + "\t"+w[1]);


    }

}






















