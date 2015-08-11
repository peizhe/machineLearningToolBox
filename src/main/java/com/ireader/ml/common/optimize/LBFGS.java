package com.ireader.ml.common.optimize;

/**
 * Created by zxsted on 15-7-27.
 *
 * L-BFGS 的实现用于处理 参数个数很大的模型的训练
 */
public class LBFGS {


    public static class ExceptionWithIflag extends Exception {

        private int iflag ;

        public  ExceptionWithIflag(int i,String s){
            super(s);
            iflag = i;
        }
        public String toString() {
               return getMessage()+"(iflag == "  + iflag + ")";
           }
    }



    public static double gtol = 0.9;    // 控制一维搜索精度

    public static double stepmin = 1e-20;  // 线性搜索的 步长的最小值

    public static double stpmax = 1e20;     // 线性搜索的最大值

    // 缓存变量

    public static double[] solution_cache = null;
    private static double gnorm=0, stp1 = 0,ftol = 0,stp[] = new double[1],ys=0,yy=0,sq=0,yr=0,beta=0,xnorm=0;
    private static int iter=0,nfun=0,point=0,ispt=0,iypt=0,maxfev=0,infp[]=new int[1],bound=0,npt =0,cp=0,i=0,nfev[] = new int[1],inmc=0,iycn=0,iscn=0;

    private boolean  finish = false;
    private static double[] w = null;

    public static int nfevaluations(){
        return nfun;
    }




}
























