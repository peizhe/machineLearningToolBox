package com.ireader.ml.classifier.nn.rnn.LSTM;

import java.io.Serializable;
import java.util.Map;

import static com.ireader.ml.Matrix.*;
import static com.ireader.ml.Vector.*;
import static com.ireader.ml.classifier.nn.Activation.*;
import static com.ireader.ml.classifier.nn.Activation.softmax;

/**
 * Created by zxsted on 15-11-1.
 *
 * LSTM 的一个 cell
 */
public class Cell implements Serializable {

    private static final long serialVersionUID = -7059290852389115565L;

    private String  initType = "";
    private double scale;
    private double miu;
    private double sigma;

    private int inSize;            // 输入size
    private int outSize;           // 输出size
    private int deSize;

    // in gate
    private double[][] Wxi;  // xin
    private double[][] Whi;  // hin
    private double[][] Wci;  // cin
    private double[]   bi;

    // forget gate
    private double[][] Wxf;  // xin
    private double[][] Whf;  // hin
    private double[][] Wcf;  // cin
    private double[]   bf;

    // cell
    private double[][] Wxc;   // xin
    private double[][] Whc;   // hin
    private double[]   bc;

    // out gate
    private double[][] Wxo;   // xin
    private double[][] Who;   // hin
    private double[][] Wco;   // cin
    private double[]   bo;

    // 输出
    private double[][] Why;
    private double[]   by;


    public Cell(int inSize,int outSize,double scale,double miu,double sigma) {

        this.scale = scale;
        this.miu = miu;
        this.sigma = sigma;

        this.inSize = inSize;
        this.outSize = outSize;


        // 初始化参数矩阵

        if (initType == "uniform") {

            this.Wxi = uniformMat(inSize,outSize,scale);
            this.Whi = uniformMat(outSize,outSize,scale);
            this.Wci = uniformMat(outSize,outSize,scale);
            this.bi  = zeroArray(outSize);

            this.Wxf = uniformMat(inSize, outSize, scale);
            this.Whf = uniformMat(outSize, outSize, scale);
            this.Wcf = uniformMat(outSize, outSize, scale);
            this.bf  = zeroArray(outSize);

            this.Wxc = uniformMat(inSize, outSize, scale);
            this.Whc = uniformMat(outSize, outSize, scale);
            this.bc  = zeroArray(outSize);

            this.Wxo = uniformMat(inSize, outSize, scale);
            this.Who = uniformMat(outSize, outSize, scale);
            this.Wco = uniformMat(outSize, outSize, scale);
            this.bo  = zeroArray(outSize);

            this.Why = uniformMat(outSize, inSize, scale);
            this.by  = zeroArray(inSize);

        } else if (initType == "gaussian") {

            this.Wxi = gaussianMat(inSize,outSize,miu,sigma);
            this.Whi = gaussianMat(outSize, outSize, miu,sigma);
            this.Wci = gaussianMat(outSize, outSize, miu,sigma);
            this.bi  = zeroArray(outSize);

            this.Wxf = gaussianMat(inSize, outSize, miu,sigma);
            this.Whf = gaussianMat(outSize, outSize, miu,sigma);
            this.Wcf = gaussianMat(outSize, outSize, miu,sigma);
            this.bf  = zeroArray(outSize);

            this.Wxc = gaussianMat(inSize, outSize, miu,sigma);
            this.Whc = gaussianMat(outSize, outSize, miu,sigma);
            this.bc  = zeroArray(outSize);

            this.Wxo = gaussianMat(inSize, outSize, miu,sigma);
            this.Who = gaussianMat(outSize, outSize, miu,sigma);
            this.Wco = gaussianMat(outSize, outSize, miu,sigma);
            this.bo  = zeroArray(outSize);

            this.Why = gaussianMat(outSize, inSize, miu,sigma);
            this.by  = zeroArray(inSize);
        }
    }


    public Cell(int inSize,int outSize,double scale,double miu,double sigma,int deSize) {
        this(inSize,outSize,scale,miu,sigma);
        this.deSize = deSize;
    }

    public int getInSize() {
        return inSize;
    }

    private int getOutSize() {
        return outSize;
    }

    public int getDeSize() {
        return deSize;
    }


    public void active(int t,Map<String ,double[][]> acts) {

        double[][] xMat = acts.get("x"+t);

        double[][] preH = null;
        double[][] preC = null;

        // 起始 ： 初始化为零向量， 也可以为 cnn 输出特征
        if (t == 0) {
            preH = new double[1][this.outSize];
            preC = new double[1][this.outSize];
        } else {
            preH = acts.get("h" + (t - 1));
            preC = acts.get("c" + (t - 1));
        }


        double[][] i = sigmod(addVec(add(add(dot(xMat, Wxi), dot(preH, Whi)), dot(preC, Wci)),this.bi,1));
        double[][] f = sigmod(addVec(add(add(dot(xMat, Wxf), dot(preH, Whf)), dot(preC, Wcf)),this.bf,1));
        double[][] gc = sigmod(addVec(add(dot(xMat, Wxc), dot(preH, Whc)),this.bc,1));
        double[][] c = add(mul(f,preC),mul(i,gc));
        double[][] o =  sigmod(addVec(add(add(dot(xMat,Wxo),dot(preH,Who)),dot(c,Wco)),this.bo,1));
        double[][] gh = tanh(c);
        double[][] h = mul(o,gh);

        acts.put("i"+t,i);
        acts.put("f"+t,f);
        acts.put("gc"+t,gc);
        acts.put("c"+t,c);
        acts.put("o"+t,o);
        acts.put("gh"+t,gh);
        acts.put("h"+t,h);

    }

    /**
     *  基于时间序列的反向传播
     *
     *  计算各层的 delta
     * */
    public void bptt(Map<String,double[][]> acts,int lastT,double lr) {

        for (int t = lastT; t> -1; t--) {

            // model output errors : error = -(y - py)dpy
            double[][] py = acts.get("py" + t);
            double[][] y = acts.get("y" + t);
            double[][] deltaY = sub(py,y);
            acts.put("dy" + t,deltaY);


            // cell output errors
            double[][] h = acts.get("h"+t);
            double[][] deltaH = null;


            /** =============== deltaH ========================================================= */
            if (t == lastT) {
                deltaH = dot(deltaY,T(Why));
//                deltaH = T(dot(Why,T(deltaY)));
            } else {
                double[][] lateDgc = acts.get("dgc" + (t+1));
                double[][] lateDf  = acts.get("df" + (t+1));
                double[][] lateDo  = acts.get("do" + (t+1));
                double[][] lateDi  = acts.get("di" + (t+1));

                double[][] addMat1 = dot(deltaY,T(Why));
                double[][] addMat2 = dot(lateDgc,T(Whc));
                double[][] addMat3 = dot(lateDi,T(Whi));
                double[][] addMat4 = dot(lateDo,T(Who));
                double[][] addMat5 = dot(lateDf,T(Whf));

                deltaH = add(addMat1,add(addMat2,add(addMat3,add(addMat4,addMat5))));
            }

            acts.put("dh"+t,deltaH);

            /** =============== output gates ========================================================= */
            double[][] gh = acts.get("gh"+t);
            double[][] o =  acts.get("o" + t);
            double[][] deltaO = mul(mul(deltaH, gh), dsigmod(o));
            acts.put("do"+t,deltaO);

            /** =============== status ============================================================== */
            double[][] deltaC = null;

            if (t == lastT) {
                double[][] addMat1 = mul(mul(deltaH, o), dsigmod(gh));
                double[][] addMat2 = dot(deltaO,T(Wco));

                deltaC = add(addMat1,addMat2);
            } else {

                double[][] lateDc = acts.get("dc" + (t + 1));
                double[][] lateDf = acts.get("df" + (t + 1));
                double[][] lateF  = acts.get("f" +  (t + 1));
                double[][] lateDi = acts.get("di" + (t + 1));

                double[][] addMat1 = mul(mul(deltaH, o), dtanh(gh));
                double[][] addMat2 = dot(deltaO,T(Wco));
                double[][] addMat3 = mul(lateF,lateDc);
                double[][] addMat4 = dot(lateDf,T(Wcf));
                double[][] addMat5 = dot(lateDi,T(Wci));

                deltaC = add(addMat1,add(addMat2,add(addMat3,add(addMat4,addMat5))));
            }

            acts.put("dc"+t,deltaC);

            /** =============== cells ================================================================ */
            double[][] gc = acts.get("gc"+t);
            double[][] i  = acts.get("i" + t);
            double[][] deltaGc = mul(mul(deltaC, i), dsigmod(gc));
            acts.put("dgc" + t, deltaGc);

            double[][] preC = null;
            if (t > 0) {
                preC = acts.get("c" + (t - 1));
            } else {
                preC = zeroMat(1,h.length);
            }

            /** =============== forget gates ========================================================= */
            double[][] f = acts.get("f"+t);
            double[][] deltaF = mul(mul(deltaC, preC), dsigmod(f));
            acts.put("df"+t,deltaF);

            /** =============== input gates ========================================================= */
            double[][] deltaI = mul(mul(deltaC,gc),dsigmod(i));
            acts.put("di" + t,deltaI);
        }

        // updateParameters(acts,lastT,lr);
    }

    private void updateParameters(Map<String,double[][]> acts,int lastT,double lr) {

        // in gate
        double[][] gWxi = new double[Wxi.length][Wxi[0].length];
        double[][] gWhi = new double[Whi.length][Whi[0].length];
        double[][] gWci = new double[Wci.length][Wci[0].length];
        double[]   gbi  = new double[bi.length];

        // forget gate
        double[][] gWxf = new double[Wxf.length][Wxf[0].length];
        double[][] gWhf = new double[Whf.length][Whf[0].length];
        double[][] gWcf = new double[Wcf.length][Wcf[0].length];
        double[]   gbf  = new double[bf.length];

        // cell
        double[][] gWxc = new double[Wxc.length][Wxc[0].length];
        double[][] gWhc = new double[Whc.length][Whc[0].length];
        double[]   gbc  = new double[bc.length];

        // out gate
        double[][] gWxo = new double[Wxo.length][Wxo[0].length];
        double[][] gWho = new double[Who.length][Who[0].length];
        double[][] gWco = new double[Wco.length][Wco[0].length];
        double[]   gbo  = new double[bo.length];

        // 输出
        double[][] gWhy = new double[Why.length][Why[0].length];
        double[]   gby  = new double[by.length];


        // 计算梯度
        for (int t = 0 ; t < lastT; t++) {

            double[][] xMat = T(acts.get("x"+t));    // 对输入进行转置

            gWxi = add(gWxi,dot(xMat,acts.get("di" +t)));
            gWxf = add(gWxf,dot(xMat,acts.get("df" +t)));
            gWxc = add(gWxc,dot(xMat,acts.get("dgc" +t)));
            gWxo = add(gWxo,dot(xMat,acts.get("do" +t)));


            if (t >0) {

                double[][] preH = T(acts.get("h"+(t-1)));
                double[][] preC = T(acts.get("c" + (t -1)));

                gWhi = add(gWhi,dot(preH,acts.get("di" +t)));
                gWhf = add(gWhf,dot(preH,acts.get("df" +t)));
                gWhc = add(gWhc,dot(preH,acts.get("dgc" +t)));
                gWho = add(gWho,dot(preH,acts.get("do" +t)));
                gWci = add(gWci,dot(preC,acts.get("di" +t)));
                gWcf = add(gWcf,dot(preC,acts.get("df" +t)));
            }

            gWco = add(gWco,dot(T(acts.get("c"+t)),acts.get("do" + t)));
            gWhy = add(gWhy,dot(T(acts.get("h"+t)),acts.get("dy" + t)));

            gbi = vecadd(gbi,acts.get("di" + t)[0]);
            gbf = vecadd(gbf,acts.get("df" + t)[0]);
            gbc = vecadd(gbc,acts.get("dgc" + t)[0]);
            gbo = vecadd(gbo,acts.get("do" + t)[0]);
            gby = vecadd(gby,acts.get("dy" + t)[0]);

         }


        // TODO : 使用上面计算得到的梯度 对参数进行更新

    }

    public double[][] decode(double[][] ht) {
        return softmax(addVec(dot(ht,Why),by,1));
    }




}
