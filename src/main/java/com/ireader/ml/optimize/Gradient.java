package com.ireader.ml.optimize;

import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.conf.Configuration;

import java.util.List;

/**
 * Created by zxsted on 15-9-14.
 */
public abstract class Gradient {


    /**
     *  hadoop 的configuation 对象， 用于gradient 的参数的传递 ，
     *  如果是 分布式计算梯度的函数 ， 除了 weight 数组 其他参数是不用传递的，
     *  只需要从 config 中加载即可
     * */
    protected Configuration conf = null;

    public Gradient(){};

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    /**
     *  计算单个样本的损失函数
     * */
    public abstract double loss(DataPoint dp,List<Double> weightVec);

    /**
     *  计算单个样本的梯度向量
     * */
    public abstract double[] gradient(DataPoint dp,List<Double> weightVec);

//    /**
//     *  梯度计算时用到的 prop 解析 函数， 从HDFS中加载梯度函数以及cost函数执行时用到的参数,
//     *  如果使用默认的梯度解析函数BatchOptimizer.getParamsDict()，那么在实现时将本函数返回null。
//     *
//     *  @param  conf : Configuration 配置对象
//     *  @return paramsMap ： HashMap<String,Object>
//     * */
//    public Map<String,Object> getParamsDict(Configuration conf);


    /**==================批量计算梯度和损失函数==========================================*/
    public abstract double[] gradient(List<DataPoint> dp, double[] weights);
    public abstract double loss(List<DataPoint> dp, double[] weightVec);



}
