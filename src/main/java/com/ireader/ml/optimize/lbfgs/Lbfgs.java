package com.ireader.ml.optimize.lbfgs;

import com.ireader.ml.optimize.Impl.testfunc.TestCost;
import com.ireader.ml.optimize.Impl.testfunc.TestGrad;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by zxsted on 15-8-28.
 *
 * LBFGS 算法的实现类
 */
public class Lbfgs {



    private CostFunc func = null;         // 损失函数  如果不使用一维搜索 ， 那么就不用损失函数
    private GradFunc gfunc = null;        // 梯度函数

    private double rou = 0.55;            // 用于一维搜索
    private double sigma = 0.4;           // 用于一维搜索

    private double epsilon = 1e-2;        // 判断是否迭代停止

    private double lambda = 1e-3;         // 迭代步长

    private boolean isonesearch = true;   // 是否是用一维搜索 ， 单机使用 ，分布式代价太大不建议使用 ，


    LinkedList<double[]> sm = null;    // 存储 最近k次的 weight 的改变量
    LinkedList<double[]> ym = null;    // 存储 最近k次的 weight 的改变量
    LinkedList<Double> rho = null;     // 存储 最近k次的 rho    的改变量


    public Lbfgs(){
        sm = new LinkedList<double[]>();
        ym = new LinkedList<double[]>();
        rho = new LinkedList<Double>();
    }



    /**=============================================================================================**/



    /**
     *  使用 Armijo 进行一维搜索
     * */
    private int[] Armijo(CostFunc func,GradFunc gfunc,double[][] featdata,double[] labels,
                         double[] weight,double[] dk) throws InterruptedException, IOException, ClassNotFoundException {

        int [] ret = new int[2];
        int m0 = 0;
        int mk = 0;

        while(m0 < 20) {
            if (func.compute(featdata, labels, vecadd(weight, numprod(Math.pow(rou, m0), dk))) <
                    func.compute(featdata,labels,weight) + sigma * Math.pow(rou,m0)*innerprod(weight,dk)){
                mk = m0;
                break;
            }
            m0+=1;
        }
        System.out.println("一维搜索次数："  + (m0+1));

//        return Math.pow(rou,mk);

        ret[0] = m0;
        ret[1] = mk;

        return ret;
    }

    /**
     * 主区动函数，返回权重向量
     * 步长计算函数
     * */
    public double[] compute(double[][] featdata,double[] labels, double[] weights)
                        throws InterruptedException, IOException, ClassNotFoundException {

       return compute(this.func,
               this.gfunc,
               featdata,
               labels,
               weights,
               this.epsilon,
               10,
               500000);

    }

    /**
     *  主驱动函数 返回权重向量
     *  使用mapreduce 计算梯度
     * */
    public double[] compute(CostFunc func,GradFunc gfunc,double[] weights)
                   throws InterruptedException, IOException, ClassNotFoundException {

        return compute( func,               //  mapreduce driver  : compute 返回一次计算的 Cost
                gfunc,                      //  mapreduce driver  : compute 返回一次计算的 梯度
                null,                       //  特征数据 不需要
                null,                       //  label 数据 不需要
                weights,                    //  当前权重向量
                this.epsilon,               //  梯度向量收敛阈值 ，小于该阈值则停止计算
                10,                         //  保存步长
                500000);                    //  最大循环次数

    }

    /**
     *
     * 主区动函数
     * @param func : 损失函数
     * @param gfunc： 更新step向量计算函数
     *
     * @return 权重向量
     * */
    public double[] compute( CostFunc func, GradFunc gfunc, double[][] featdata,double[] labels,
                             double[] weight,double epsilon,int m,int maxit) throws InterruptedException, IOException, ClassNotFoundException {


        int k = 0;
        int n = weight.length;

        double grad[] = gfunc.compute(featdata,labels,weight);   // 计算新的梯度 ; 这里可以使用 MapReduce 计算梯度


        int onesearchsteps = 0;
        while (k < maxit) {

            System.out.println("iter at :" + k + "\n grad is " + grad[0]+ "\t" + grad[1]);


            if(norm(grad) < epsilon) break;   // 如果 梯度足够小则停止 循环


            double dk[] = numprod(-1.0 , twoloop(sm,ym,rho,grad));

            /**
             *  执行一维搜索  确定步长 (可选)
             * */

            if(isonesearch) {
                int[] nStep =  Armijo(func, gfunc, featdata, labels, weight, dk);
                onesearchsteps += nStep[0]+ 1;
                lambda =Math.pow(rou , nStep[1]);
            }

            double[] newweight= vecadd(weight,numprod(lambda,dk));

            double[] sk = vecmimus(newweight, weight);

            double[] newgrad = gfunc.compute(featdata, labels, newweight);

            double[] yk = vecmimus(newgrad, grad);


            grad = newgrad;            // 更新梯度

            double skyk = innerprod(sk,yk);

            if(skyk > 0) {    // 更新缓存
                rho.addLast(1.0/skyk);
                sm.addLast(sk);
                ym.addLast(yk);
            }

            if(sm.size() > m) {
                rho.pollFirst();
                sm.pollFirst();
                ym.pollFirst();
            }

            k+=1;
            weight = veccopy(newweight);
        System.out.println("current weight is :" + Arrays.toString(weight));
        }

        System.out.println("One search step is :" + onesearchsteps);

        return weight;
    }


    /**
     *  双循环函数，计算梯度向量改变量数组
     * */
    public double[]  twoloop(LinkedList<double[]> sm,LinkedList<double[]> ym,LinkedList<Double>rho,double[] gk){
        double[] ret = null;

        int n = sm.size();       // 缓存的长度

        double h0 = 0.0;
        if (sm.size() >= 1){
            h0 = 1.0 * innerprod(sm.peekLast(),ym.peekLast()) / innerprod(ym.peekLast(),ym.peekLast());
        }else{
            h0 = 1.0;
        }

        double[] alpha = new double[n];

        double[] q = veccopy(gk);

        // 第一次循环
        for (int i = n-1; i > -1; i--) {
            alpha[i] = rho.get(i) * innerprod(sm.get(i),q);
            q = vecmimus(q,numprod(alpha[i],ym.get(i)));
        }

        double[] z = numprod(h0,q);

        // 第二次循环
        for(int i = 1; i < n ; i++) {
            double b = rho.get(i)*innerprod(ym.get(i),z);
            z = vecadd(z,numprod(alpha[i] - b,sm.get(i)));
        }

        ret = z;

        return ret;
    }


    /**=================================  参数设置  =================================================**/

    public Lbfgs setCostFunc(CostFunc func) {
        this.func = func;
        return this;
    }

    public Lbfgs setGradFunc(GradFunc gfunc) {
        this.gfunc = gfunc;
        return this;
    }

    public Lbfgs setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public Lbfgs setLambda(double lambda) {
        this.lambda = lambda;
        return this;
    }

    public Lbfgs setIsonesearch(boolean isonesearch) {
        this.isonesearch = isonesearch;
        return this;
    }

    public Lbfgs setSigma(double sigma) {
        this.sigma = sigma;
        return this;
    }

    public Lbfgs setRou(double rou) {
        this.rou = rou;
        return this;
    }



    /**=====================================================================================================
     * 下面是 向量 工具函数
     * ====================================================================================================*/

    /**
     * norm
     * */
    public double norm(double[] weights) {

        double ret = 0.0;

        for (int i = 0 ; i < weights.length ; i++) {
            ret += weights[i]* weights[i];
        }

        return Math.sqrt(ret);
    }

    /**
     *  向量按元素相加
     * */
    double[] vecadd(double[] vec1,double[] vec2) {
        double[] retvec = new double[vec1.length];

        for(int i = 0 ; i < vec1.length;i++) {
            retvec[i] = vec1[i] + vec2[i];
        }
        return retvec;
    }

    /**
     *  向量按元素 相减
     * */
    double[] vecmimus(double[] vec1,double[] vec2) {
        double retvec[] = new double[vec1.length];

        for(int i = 0 ; i < vec1.length; i++) {
            retvec[i] = vec1[i] - vec2[i];
        }

        return retvec;
    }

    /**
     *  向量 按值 复制
     * */
    double[]  veccopy(double[] vec){

        double[] retvec = new double[vec.length];

        for (int i = 0 ; i < vec.length; i++) {
            retvec[i] = vec[i];
        }

        return retvec;
    }

    /**
     *  数量乘以向量
     * */
    public double[] numprod(double val, double[] vec) {
        double[] retvec =new double[vec.length];

        for (int i = 0 ; i < vec.length; i++) {
            retvec[i] = val * vec[i];
        }

        return retvec;
    }

    /**
     *  行向量乘以列向量
     * */
    public double innerprod(double[] vec1,double[] vec2) {

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
    public double[][] outerprod(double[] vec1,double[] vec2) {

        int n = vec1.length;
        int m = vec2.length;

        double[][] retmat = new double[n][m];

        for (int i = 0 ; i < n ; i++)
            for(int j = 0 ; j < m; j++) {
                retmat[i][j] = vec1[i] * vec2[j];
            }

        return retmat;
    }


    /**=====================================================================================================
     * 功能测试
     * ====================================================================================================*/

    public static void main(String[] args) {


        CostFunc func = new TestCost();
        GradFunc gfunc = new TestGrad();

        Lbfgs lbfgs = new Lbfgs().setIsonesearch(true);

        Random random = new Random();
        double[] w0 = new double[]{random.nextDouble(),random.nextDouble()};

//        double[] w0 = new double[]{0.37,0.58};
        double[] w = new double[0];
        try {
            w = lbfgs.compute(func,gfunc,null,null,w0,3e-3,20,500000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("result is :" + w[0] + "\t"+w[1]);
        System.out.println("init weight is : " + Arrays.toString(w0));

    }
}
