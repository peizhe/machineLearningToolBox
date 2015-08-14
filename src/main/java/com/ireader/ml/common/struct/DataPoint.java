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

    public DataPoint(){ }

    public DataPoint(String line) {

        String[] fields = line.split("\\s+",4);

        System.out.println(fields.length);


        this.uid = fields[0];
        System.out.println(this.uid);
        this.bid = fields[1];
        System.out.println(this.bid);

//        System.out.println(fields[0]);
//        System.out.println("---------------------------------------------------");
//        System.out.println(fields[1]);
//        System.out.println("---------------------------------------------------");
//        System.out.println(fields[2]);
//        System.out.println("---------------------------------------------------");
//        System.out.println(fields[3]);

//        if(fields.length > 2) {
            this.label = Double.parseDouble(fields[2]);
            this.features = new DoubleVector(fields[3]);
//        }
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
        String testStr = "zzz16000  10197731    0   1.00562358539   1.00729447364   1.00025897857   1.0 -0.885061777357 -0.694383834747 -0.999393276119 -1.0    -0.00231598580168   -0.00363036737699   -0.0016476292035    -0.00204569620767   -0.522378271668\t-0.141863129269    -0.992703881844 -0.126611762977 0   1   0   0.019426877211  -0.000757175845277  1.02061387429   0.0163576229486 0.521494857215  2.90665298655   -0.840506794351 1.31260967292   10  0.474779011072  0.359505679465  0.0573898335701 0.0412286874657 0.0435639898883 0.243550880343  0.34515566441   0.494290245347  0.232991380683  0.0947906458479 0.0332309625227 0.00505039925377    0.0035981334307 0.00377090170283    0.000225178591086   0.479348779754  0.114171133294  0.233082366311  2   -0.0178760256842    -0.00168974490763   -0.0184435943145    1.608260364 0.133703202134  0.0104421426519 0.446088874274  1.12552874389   -7.92972975709e-06  -2.44175796115e-05  -6.094906891e-05";

        DataPoint dp = new DataPoint(testStr);

//        System.out.println("feat size is: " + dp.features.getValue().size());

        System.out.println(dp.toString());

    }
}
