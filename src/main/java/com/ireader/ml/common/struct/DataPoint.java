package com.ireader.ml.common.struct;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by zxsted on 15-7-23.
 */
public class DataPoint implements Writable {

    private String uid;
    private String bid;
    private double label;
    private DoubleVector features;

    public DataPoint(String line) {

        String[] fields = line.split("\t",4);


        this.uid = fields[0];
        this.bid = fields[1];

        if(fields.length > 2) {
            this.label = Double.parseDouble(fields[2]);
            this.features = new DoubleVector(fields[3]);
        }
//        System.out.println(fields[0]);
//        System.out.println(fields[1]);
//        System.out.println(fields[2]);
//        System.out.println(fields[3]);
    }

    public DataPoint(DataPoint dp){
        this.uid = dp.uid;
        this.bid = dp.bid;
        this.features = new DoubleVector(dp.features);
    }
    public DataPoint(String uid,String bid,double label,ArrayList<Double> values)
    {
        this.uid = uid;
        this.bid = bid;
        this.label = label;
        this.features = new DoubleVector(values);
    }

    public DataPoint(int k){
        this.uid = "-1";
        this.bid = "-1";
        this.label = Double.NaN;
        this.features = new DoubleVector(k);

    }


    /** 序列化部分*/
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.uid);
        out.writeUTF(this.bid);
        out.writeDouble(this.label);
        this.features.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        this.uid = in.readUTF();
        this.bid = in.readUTF();
        this.label = in.readDouble();

        this.features.readFields(in);
    }

    @Override
    public String toString(){
        return this.uid+"\t"+this.bid+"\t"+this.label+"\t" + this.features.toString();
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public DoubleVector getFeatures() {
        return features;
    }

    public void setFeatures(DoubleVector features) {
        this.features = features;
    }


    public static void main(String[] args) {
        String testStr = "zzz16000\t10197731\t0\t1.00562358539\t1.00729447364\t1.00025897857\t1.0\t-0.885061777357\t-0.694383834747\t-0.999393276119\t-1.0\t-0.00231598580168\t-0.00363036737699\t-0.0016476292035\t-0.00204569620767\t-0.522378271668\t-0.141863129269\t-0.992703881844\t-0.126611762977\t0\t1\t0\t0.019426877211\t-0.000757175845277\t1.02061387429\t0.0163576229486\t0.521494857215\t2.90665298655\t-0.840506794351\t1.31260967292\t10\t0.474779011072\t0.359505679465\t0.0573898335701\t0.0412286874657\t0.0435639898883\t0.243550880343\t0.34515566441\t0.494290245347\t0.232991380683\t0.0947906458479\t0.0332309625227\t0.00505039925377\t0.0035981334307\t0.00377090170283\t0.000225178591086\t0.479348779754\t0.114171133294\t0.233082366311\t2\t-0.0178760256842\t-0.00168974490763\t-0.0184435943145\t1.608260364\t0.133703202134\t0.0104421426519\t0.446088874274\t1.12552874389\t-7.92972975709e-06\t-2.44175796115e-05\t-6.094906891e-05";

        DataPoint dp = new DataPoint(testStr);

        System.out.println("feat size is: " + dp.features.getValue().size());

        System.out.println(dp.toString());

    }
}
