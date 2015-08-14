package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.ml.common.struct.DataPoint;
import org.apache.hadoop.io.Writable;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-23.
 */
public class WDataPoint implements Writable {

    private DataPoint sub_dp = null;
    private Double weight = null;
    private Integer col = null;

    public WDataPoint(){

    }

    public WDataPoint(DataPoint sub_dp,Double weight, Integer col){
        this.col = col;
        this.weight = weight;
        this.sub_dp = sub_dp;
    }

    public WDataPoint(String uid,String bid,Double label, ArrayList<Double> feats,Double weight, Integer col) {

        this.col = col;
        this.weight = weight;
        this.sub_dp = new DataPoint(uid,bid,label,feats);
    }

    public WDataPoint(String line){
        String[] fields = line.split("\\s+",3);
        this.col = Integer.parseInt(fields[0]);
        this.weight  = Double.parseDouble(fields[1]);
        this.sub_dp = new DataPoint(fields[2]);
    }

    @Override
    public String toString(){

        return this.col+"\t"+this.weight+"\t"+this.sub_dp.toString();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.col);
        out.writeDouble(this.weight);
        this.sub_dp.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.col = in.readInt();
        this.weight = in.readDouble();
        this.sub_dp.readFields(in);
    }

    public ArrayList<Double> getFeat() {
        return this.getSub_dp().getFeatures().getValue();
    }


    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public DataPoint getSub_dp() {
        return sub_dp;
    }

    public void setSub_dp(DataPoint sub_dp) {
        this.sub_dp = sub_dp;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }
}
