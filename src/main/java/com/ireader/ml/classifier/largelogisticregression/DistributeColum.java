package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.struct.DataPoint;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-23.
 *
 * job1: 将每行特征进行分块 与特征进行计算:
 * */
public class DistributeColum {

    /**
     *  Mapper ： 将一个特征向量分段后分发到不同的reducer
     * */
    public static class DistributeColumMapper extends Mapper<LongWritable,Text,Text,WDataPoint> {

        private int slicelen = -1;
        private String filename = null;

        protected void setup(Context context) throws IOException,InterruptedException{

            // 从配置中 得到每个特征分片的长度

            slicelen = context.getConfiguration().getInt("LLR_SliceLeangth",10);

            //  得到文件名称 与行偏移组成唯一 primary key

            FileSplit fileSplit =(FileSplit) context.getInputSplit();
            filename = fileSplit.getPath().getName();
        }


        protected void map(LongWritable key, Text value,Context context)
                                               throws IOException ,InterruptedException{

            String rowkey_prefix = filename + ","+key.get();

            // 输入数据的格式是 ：  uid\tbid\tlabel\tweigth\tweigth\tweigth\tweigth\tweigth\tweigth\t
            DataPoint point = new DataPoint(value.toString());

            if (point.getFeatures() == null) return;

            ArrayList<Double> features = point.getFeatures().getValue();

            int slicenum = 0;
            ArrayList<Double> slice = new ArrayList<Double>(slicelen);
            for(int i = 0; i < features.size();i++) {
                int index = i - slicenum * slicelen;     // sub-feature index
                if(index < slicelen){
                    slice.add(features.get(i));
                }else{
                    // 注意到 这里的arraylist 用的是deepcopy  ， 长度满足sliceLen 就输出
                    WDataPoint  sub_dp = new WDataPoint(point.getUid(),point.getBid(),point.getLabel(),slice,0.0,slicenum);

                    context.write(new Text(rowkey_prefix+"," + slicenum),sub_dp);

                    // 降档前特征加入下一条子特征数组内
                    slicenum++;
                    slice.clear();   // 可以清空， 最底层实现了安置复制
                    slice.add(features.get(index));  // 这时sub_index == 0
                }


            }

            if (slice.size()!=0) {
                   WDataPoint  sub_dp = new WDataPoint(point.getUid(),point.getBid(),point.getLabel(),slice,0.0,slicenum);
                   context.write(new Text(rowkey_prefix + "+" + slicenum), sub_dp);
                   slice.clear();   // 可以清空， 最底层实现了安置复制
               }


        }

    }

}
