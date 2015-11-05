package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.struct.DoubleVector;
import com.ireader.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zxsted on 15-7-24.
 *
 * map 计算sub——vector的乘积
 * reduce 合并sub_vector 的乘积，并附加到 样本上
 */
public class RowGradientCompute {

    public static class RowGradientComputeMapper extends Mapper<Text,Text,Text,Text>{

        private int slicelen = -1;
        private int  featnum = -1;
        private ArrayList<Double> weights = new ArrayList<Double>();
        private Map<Integer, DoubleVector> weightdict = new HashMap<Integer,DoubleVector>();

        @Override
        protected void setup(Context context) throws IOException,InterruptedException {

            Configuration conf = context.getConfiguration();

            slicelen = conf.getInt("LLR_SliceLeangth",-1);
            featnum = conf.getInt("LLR_FeatNum",-1);
            String weightfile = conf.get("LLR_WeightSavePath");


            Path wfile = new Path(weightfile);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            FSDataInputStream fsi =null;
            BufferedReader in = null;

            if(fs.isFile(wfile)) {
                fsi = fs.open(wfile);
                String line = null;
                in  = new BufferedReader(new InputStreamReader(fsi,"utf-8"));

                while((line = in.readLine()) != null) {
                    if(!(featnum == StringUtil.StringToList(weights, line.trim(),"\t"))){
                        throw new IOException("load weights number and featnum is not same!");
                    }
                }
            }
            in.close();
            fsi.close();

            //
            int selicenum = 0;
            ArrayList tempList = new ArrayList<Double>();

            for(int i = 0 ; i < weights.size(); i++) {
                int index = i - selicenum * slicelen;
                if(index < slicelen) {
                    tempList.add(weights.get(i));

                } else{
                    weightdict.put(selicenum,new DoubleVector(tempList));
                    tempList.clear();
                    selicenum++;
                    tempList.add(weights.get(i));
                }

            }

            if (0 != tempList.size()) {
                weightdict.put(selicenum,new DoubleVector(tempList));
                tempList.clear();
            }

        }

        @Override
        protected void map(Text key,Text value, Context context)
                                                   throws IOException,InterruptedException{

            String[] keypair = key.toString().trim().split(",");
            String rowID = keypair[0]+","+keypair[1];    // 文件名加行偏移量
            int colnum = Integer.parseInt(keypair[2]);   // 分片的列

            DoubleVector weigthselice = weightdict.get(colnum);   // 取出当前分片对应的权重分片

            WDataPoint wdp = new WDataPoint(value.toString());

            DoubleVector sub_feat = wdp.getSub_dp().getFeatures();

            double product = sub_feat.innerMul(weigthselice);

            // ADataPoint sub_dp,Double weight, Integer col
            WDataPoint out = new WDataPoint(wdp.getSub_dp(),product,wdp.getCol());

            context.write(new Text(rowID),new Text(out.toString()));

        }


//        /**
//         *  将字符串转化为list
//         * */
//        private int StringToList(ArrayList<Double> list, String str) throws IOException {
//            if(str.length() == 0) {
//                throw new IOException("load weigths encount error, weight number is zero!");
//            }
//            String fields[] = str.split("\t");
//
//            for (int i = 0; i < fields.length; i++) {
//                list.add(Double.parseDouble(fields[i]));
//            }
//
//            return list.size();
//        }
    }


    public static class RowGradientComputeReducer extends Reducer<Text,Text,NullWritable,Text> {

        @Override
        protected void reduce(Text key,Iterable<Text> values,Context context)
                                                        throws IOException, InterruptedException{

            ArrayList<Double> tempList = new ArrayList<Double>();

           HashMap<Integer,ArrayList<Double>> tmpDict = new HashMap<Integer,ArrayList<Double>>();
//            ArrayList<ArrayList<Double>> tmpDict = new ArrayList<ArrayList<Double>>();

//            String lineno = key.toString();

            double product = 0.0;

            String uid = null;
            String bid = null;
            double label = 0.0;

            for(Text val: values) {
                WDataPoint dp = new WDataPoint(val.toString());
                uid = dp.getSub_dp().getUid();
                bid = dp.getSub_dp().getBid();
                label = dp.getSub_dp().getLabel();

                product += dp.getWeight();
                tmpDict.put(dp.getCol(),dp.getFeat());
            }

            for(int i = 0 ; i < tmpDict.size(); i++)
                tempList.addAll(tmpDict.get(i));


            // String uid,String bid,Double label, ArrayList<Double> feats,Double weight, Integer col
            WDataPoint outdp = new WDataPoint(uid,bid,label,tempList,product,-1 );

            context.write(NullWritable.get(), new Text(outdp.toString()));
        }
    }

}
