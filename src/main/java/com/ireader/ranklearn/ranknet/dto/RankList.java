package com.ireader.ranklearn.ranknet.dto;

import com.ireader.ranklearn.ranknet.util.Sorter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-3.
 *
 * 这是一组数据的封装类，每个元素都是一个DataPoint
 */
public class RankList {

    protected List<ADataPoint> rl = null;

    public RankList()
    {
        rl = new ArrayList<ADataPoint>();
    }

    public RankList(RankList rl) {
        this.rl = new ArrayList<ADataPoint>();

        for(int i = 0 ; i < rl.size(); i++) {
            this.rl.add(rl.get(i));
        }
    }

    /**
     *  按照指定的顺序，和对应的RankList 构建一个新的Rankist
     * */
    public RankList(RankList rl, int[] idx) {
        this.rl = new ArrayList<ADataPoint>();
        for (int i = 0 ; i < idx.length; i++) {
            this.rl.add(rl.get(idx[i]));
        }
    }

    // 使用第一个dp作为ranklist 的id
    public String getID() {
        return get(0).getID();
    }

    public int size() {
        return rl.size();
    }

    public ADataPoint get(int k) {
        return rl.get(k);
    }

    public void add(ADataPoint p) {
        rl.add(p);
    }

    public void remove(int k)
    {
        rl.remove(k);
    }

    /**
     *  根据指定的特征进行排序
     * */
    public RankList getRanking(int fid) {
        double[] score = new double[rl.size()];
        for (int i = 0 ; i < rl.size(); i++)
            score[i] = rl.get(i).getFeatureValue(fid);

        int[] idx = Sorter.sort(score, false);  // 递减排序
        return new RankList(this,idx);
    }

    /**
     *  根据label 进行排序
     * */
    public RankList getCorrectRanking()
    {
        double[] score = new double[rl.size()];
        for(int i = 0 ; i < rl.size();i++)
            score[i] = rl.get(i).getLabel();
        int[] idx = Sorter.sort(score,false);
        return new RankList(this,idx);
    }

    /**
     *  返回根据label值进行从小到大进行排序
     * */
    public RankList getWorstRanking()
    {
        double[] score = new double[rl.size()];
        for(int i = 0 ; i < rl.size(); i++)
            score[i] = rl.get(i).getLabel();

        int[] idx = Sorter.sort(score,true); // 进行排序
        return new RankList(this,idx);
    }

}
