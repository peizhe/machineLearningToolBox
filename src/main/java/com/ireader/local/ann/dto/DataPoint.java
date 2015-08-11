package com.ireader.local.ann.dto;

import java.util.Arrays;

/**
 * Created by zxsted on 15-8-3.
 *
 * description 实现了对样本数据的封装
 */
public class DataPoint {

    public static float INFINITY = -1000000.0f;
    public static int MAX_FEATURE = 51;
    public static int FEATURE_INCREASE = 10;

    public static int featureCount = 0;

    protected float label = 0.0f;   // 每个样本的 实际标记值

    protected String id = "";        // 每个datapoint的id
    protected String itemid ="";     // 每个dataoint 的item id
    protected float[] fVals = null;  // fVals[0] 不可使用，必须从0开始使用
    protected String description = "";  // 对当前样本点的描述

    protected double cached = -1.0;     // 存储学习到的最近一次的估计

    // 从字符串中提取key 和 value 的函数
    private String getKey(String pair)
    {
        return pair.substring(0,pair.indexOf(":"));
    }

    private String getValue(String pair)
    {
        return pair.substring(pair.indexOf(":") + 1);
    }

    /**
     *  从字符串中实例化DATAPOINT
     * */
    public DataPoint(String text) {
        fVals = new float[MAX_FEATURE];
        Arrays.fill(fVals,INFINITY);
        int lastFeature = -1;
        try{

            int idx = text.lastIndexOf("#");
            if(idx != -1) {
                description = text.substring(idx);    // 保存对样本点的描述
                text = text.substring(0,idx).trim();  // 移除记录后面的空白部分

            }

            String[] fs = text.split(" ");
            label = Float.parseFloat(fs[0]);
            id = getValue(fs[1]);         // 用户id
            itemid = getKey(fs[1]);       // 商品id

            String key = "";   //
            String val = "";
            for(int i = 2 ; i < fs.length; i++) {
                key = getKey(fs[i]);
                val = getValue(fs[i]);
                int f = Integer.parseInt(key); //如果特征索引f大于最大的特征个数，那么增大特征的最大个数
                if (f >= MAX_FEATURE) {
                    while (f >= MAX_FEATURE)
                        MAX_FEATURE += FEATURE_INCREASE;
                    float[] tmp = new float[MAX_FEATURE];
                    System.arraycopy(fVals, 0, tmp, 0, fVals.length);
                    Arrays.fill(tmp, fVals.length, MAX_FEATURE, INFINITY);
                    fVals = tmp;
                }

                fVals[f] = Float.parseFloat(val);

                if (f > featureCount)
                    featureCount = f;
                if(f > lastFeature)
                    lastFeature = f;
            }

            // shrink fVals 压缩特征存储空间， 去冗余空间
            float[] tmp = new float[lastFeature + 1];
            System.arraycopy(fVals,0,tmp,0,lastFeature + 1);
            fVals = tmp;

        }catch(Exception ex ) {
            System.out.println("Error in DataPoint(test) constructor");
        }

    }

    public String getItemid(){
        return this.itemid;
    }

    public void setItemid(String Itemid) {
        this.itemid = itemid;
    }

    public float getLabel(){
        return label;
    }

    public void setLabel(float label) {
        this.label = label;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public float getFeatureValue(int fid)
    {
        if (fid >= fVals.length)
            return 0.0f;
        if(fVals[fid] < INFINITY+1)
            return 0.0f;
        return fVals[fid];
    }

    public void setFeatureValue(int fid,float fval) {
        fVals[fid] = fval;
    }

    public int getFeatureCount()
    {
        return featureCount;
    }

    public float[] getFeatureVector(int[] featureID)
    {
        float[] fvector = new float[featureID.length];
        for(int i = 0 ; i < featureID.length; i++){
            fvector[i] = getFeatureValue(featureID[i]);
        }
        return fvector;
    }

    public float[] getFeatureVector()
    {
        return fVals;
    }

    // 获取一个同该特征， 将特征值为INFINITY 的特征值为0.0f
    public float[] getExternalFeatureVector()
    {
        float[] ufVals = new float[fVals.length];
        System.arraycopy(fVals,0,ufVals,0,fVals.length);

        for(int i = 0 ; i < ufVals.length; i++)
            if(ufVals[i] > INFINITY + 1)
                ufVals[i] = 0.0f;
        return ufVals;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description) {
        this.description  = description;
    }

    /**
     *  进行正则化
     * */
    public void normalize(int[] fids,float[] norm)
    {
        for(int i = 0 ; i < fids.length;i++)
            if (norm[i] > 0.0)
                fVals[fids[i]] /= norm[i];
    }

    public String toString()
    {
        String output = label + " " + "id:" + id + " ";
        for (int i = 1; i < fVals.length; i++)
            if(fVals[i] > INFINITY + 1)
                output += i +":"+ fVals[i] + ((i == fVals.length - 1)?"":" ");
        output += " " + description;
        return output;
    }

    public void addFeatures(float[] values)
    {
        float[] tmp = new float[(featureCount + 1) + values.length];
        System.arraycopy(fVals,0,tmp,0,fVals.length);
        Arrays.fill(tmp,fVals.length,featureCount + 1,values.length);
        fVals = tmp;
    }

    public void setCached(double c) {
        cached = c;
    }

    public double getCached()
    {
        return cached;
    }

    public void resetCached() {
        cached = -100000000.0f;
    }

}
