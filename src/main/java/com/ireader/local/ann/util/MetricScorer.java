package com.ireader.local.ann.util;

/**
 * Created by zxsted on 15-8-3.
 */
import com.ireader.local.ann.dto.RankList;

import java.util.List;


/**
 * @description :  性能度量方法类的基类
 * */
public class MetricScorer {

    private int k = 10;

    public MetricScorer(){

    }

    public void setK(int k) {
        this.k = k;
    }


    /**
     * 计算整个数据集的 性能评分
     * */
    public double score(List<RankList> rl)
    {
        double score = 0.0;
        for (int i = 0 ; i < rl.size(); i++)
            score += score(rl.get(i));
        return score / rl.size();
    }


    /**
     * 一个ranklist 中的评分函数  ， 必须被重载
     * */
    public double score(RankList rl) {

        return 0.0;
    }

    /**
     * clone方法
     * */
    public MetricScorer clone()
    {
        return null;
    }

    public String name()
    {
        return "";
    }


}