package com.ireader.ranklearn.ranknet;

/**
 * Created by zxsted on 15-8-3.
 */
import com.ireader.ranklearn.ranknet.dto.ADataPoint;
import com.ireader.ranklearn.ranknet.dto.RankList;
import com.ireader.util.FileUtils;
import com.ireader.ranklearn.ranknet.util.MergeSorter;
import com.ireader.ranklearn.ranknet.util.MetricScorer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * @descriptio : 各种ranker的父类
 *@Author ： ted
 *@Date : 2015-01-15
 * */
public class Ranker {

    public static boolean verbose = true;                                 //

    protected List<RankList> samples = new ArrayList<RankList> ();             // trains samples
    protected int[] features = null;
    protected MetricScorer scorer = null;        //还没有定义 ，评价ranker 性能的 度量器
    protected double scoreOnTrainingData = 0.0;
    protected double bestScoreOnValidationData = 0.0;

    protected List<RankList>  validationSamples = null;                       //验证数据集
    protected List<RankList> testSamples = null;                              //测试数据集

    public Ranker() {

    }

    public Ranker(List<RankList> samples,int[] features) {
        this.samples = samples;
        this.features = features;
    }

    /*==============工具函数==============================*/
    public void set(List<RankList> samples,int[] features){
        this.samples = samples;
        this.features = features;
    }

    public void setScorer(MetricScorer scorer){
        this.scorer = scorer;
    }

    public void setValidationSet(List<RankList> samples)
    {
        this.validationSamples = samples;
    }
    public void setTestSet(List<RankList> samples){
        this.testSamples = samples;
    }
    //	public void set(MetricScorer scorer){
//		this.scorer = scorer;
//	}
    public double getScoreOnTrainingData()
    {
        return scoreOnTrainingData;
    }
    public double getScoreOnValidationData(){
        return bestScoreOnValidationData;
    }

    public int[] getFeatures()
    {
        return features;
    }
    /**
     * 对一个RankList内部的样本进行排序， 每个ranklist 是一个 同一个用户的数据， 具有相同的用户ID
     * */
    public RankList rank(RankList rl) {
        double[] scores = new double[rl.size()];
        for(int i = 0 ; i < rl.size(); i++) {
            scores[i] = eval(rl.get(i));
        }
        int[] idx = MergeSorter.sort(scores, false);
        return new RankList(rl,idx);     // 返回重新根据 idx 排序后的ranklist
    }

    /**
     * 对全部 数据进行排序， 不过是还属按照 一个用户一个内部排序
     * */
    public List<RankList> rank(List<RankList> l){
        List<RankList> ll = new ArrayList<RankList>();
        for(int i = 0; i < l.size(); i++) {
            ll.add(rank(l.get(i)));
        }
        return ll;
    }

    public void save(String modelFile)
    {
        FileUtils.write(modelFile, "utf-8", model());
    }

    /*=============输出格式化函数================================*/
    public void PRINT(String msg)
    {
        if(verbose)
            System.out.print(msg);
    }

    public void PRINTLN(String msg)
    {
        if(verbose)
            System.out.println(msg);
    }

    /*按照 len 数组中指定的长度 进行左侧格式化输出*/
    public void PRINT(int[] len,String[] msgs) {
        if(verbose)
        {
            for(int i = 0; i < msgs.length; i++) {
                String  msg = msgs[i];
                if(msg.length() > len[i])
                    msg = msg.substring(0,len[i]);
                else
                    while(msg.length() < len[i])         //按照定长 左侧格式化输出
                        msg += " ";
                System.out.print(msg+" | ");
            }
        }
    }

    public void PRINTLN(int[] len,String[] msgs) {
        PRINT(len,msgs);
        PRINTLN("");
    }
    /*输出时间*/
    public void PRINTTIME()
    {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
    }

    /*打印内存使用情况*/
    public void PRINT_MEMORY_USAGE()
    {
        System.out.println("***** " + Runtime.getRuntime().freeMemory() + "  /  " +
                Runtime.getRuntime().maxMemory());
    }

    protected void copy(double[] source , double[] target)
    {
        for(int j = 0 ; j < source.length; j++)
            target[j] = source[j];
    }

    /**
     *
     * */
    public void init(){

    }

    /**
     *
     * */
    public void learn(){

    }

    /**
     * 训练好的模型根据输入数据 DataPoint进行预测
     * */
    public double eval(ADataPoint p) {
        return -1.0;
    }

    public Ranker clone()
    {
        return null;
    }

    public String toString()
    {
        return "[not yet implemented]";
    }

    /**
     * 以字符串的形式输出模型的XML保存内容
     * */
    public String model()
    {
        return "[not yet implemented]";
    }

    /*加载模型*/
    public void load(String fn) {

    }

    /*打印参数*/
    public void printParameters()
    {

    }
    /* 获取ranker的name*/
    public String name()
    {
        return "";
    }

}
