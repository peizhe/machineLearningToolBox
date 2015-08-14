package com.ireader.ml.common.struct;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zxsted on 15-7-23.
 */
public class DoubleVector implements Writable {

    private  ArrayList<Double> value;

    public DoubleVector(){
        value = new ArrayList<Double>();
    }

    public DoubleVector(String line) {
        String[] valueString = line.split("\\s+");
        value = new ArrayList<Double>();

        for (int i = 0; i < valueString.length;i++) {
            value.add(Double.parseDouble(valueString[i]));
        }
    }

    // deep copy
    public DoubleVector(ArrayList<Double> feats) {
        this.value = new ArrayList<Double>();
        for(int i = 0 ; i < feats.size(); i++){
            double tempfeat = feats.get(i);
            this.value.add(tempfeat);
        }

    }


    // deepcopy
    public DoubleVector(DoubleVector ins) {
        value = new ArrayList<Double>();
        for(int i = 0; i < ins.getValue().size(); i++) {
            value.add(new Double(ins.getValue().get(i)));
        }
    }


    // 使用全零生成一个指定长度实例
    public DoubleVector(int k) {
        value = new ArrayList<Double>();
        for (int i =0 ;i < k ; i++) {
            value.add(0.0);
        }

    }


    public ArrayList<Double> getValue() {
        return value;
    }

    public DoubleVector add(DoubleVector doubleVector) {
        if(value.size() == 0)
            return new DoubleVector(doubleVector);
        else if(doubleVector.getValue().size() == 0)
            return new DoubleVector(this);
        else if(value.size() != doubleVector.getValue().size())
            try{
                throw new Exception("can not add! dimension not compatible1" +
                value.size() +","+ doubleVector.getValue().size());

            }catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        else{
            DoubleVector result = new DoubleVector();
            for (int i = 0; i < value.size(); i++) {
                result.getValue().add(value.get(i) + doubleVector.getValue().get(i));
            }
            return result;
        }
    }


    // 实现向量 与数值相乘
    public DoubleVector multiply(double num) {
        DoubleVector result = new DoubleVector();
        for (int i = 0; i < value.size(); i++) {
            result.getValue().add(value.get(i) * num);
        }
        return result;
    }

    // 实现向量与向量内积
    public double innerMul(DoubleVector other) {
        double ret = 0.0;
        if(this.value.size() != other.getValue().size()) {
            throw new ArithmeticException("two vector do not have same size: vec1 "+ this.value.size() +
                    "  vec2 : " +other.getValue().size());
        }

        for (int i = 0 ; i < value.size(); i++) {
            ret += this.value.get(i) * other.getValue().get(i);
        }

        return ret;
    }
    public DoubleVector divide(double num) {
        double factor = 1.0 / num;
        return this.multiply(factor);
    }

//    // 实现向量element add
//    public DoubleVector add(DoubleVector other) {
//
//        ArrayList<Double> tempList = new ArrayList<Double>();
//        if(this.value.size() != other.getValue().size()) {
//            throw new ArithmeticException("two vector do not have same size: vec1 "+ this.value.size() +
//                    "  vec2 : " +other.getValue().size());
//        }
//
//        for(int i = 0 ;i < this.value.size(); i++) {
//            tempList.add(this.value.get(i) + other.getValue().get(i));
//        }
//
//        return  new DoubleVector(tempList);
//    }


//    public String toString() {
//        String s = new String();
//        for (int i = 0; i < value.size() - 1; i++) {
//            s += (value.get(i) + ",");
//        }
//        s += value.get(value.size() - 1);
//        return s;
//    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(value.size());
        for(int i = 0 ; i < value.size();i++) {
            out.writeDouble(value.get(i));
        }
    }

    @Override
    public void readFields(DataInput in ) throws IOException {
        int size = 0;
        value = new ArrayList<Double>();
        if((size = in.readInt()) != 0) {
            for(int i = 0; i < size; i++) {
                value.add(in.readDouble());
            }
        }
    }

    @Override
    public String toString() {
        int size = this.value.size();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < size; i++) {
            sb.append(((i==0)?"":"\t") + value.get(i));
        }
        return sb.toString();
    }



    public static void main(String[] args) {
        ArrayList<Double> feats = new ArrayList<Double>();

        feats.add(1.0);
        feats.add(2.0);
        feats.add(3.0);
        feats.add(4.0);
        feats.add(5.0);
        feats.add(6.0);

        DoubleVector dvec =  new DoubleVector(feats);

//        feats.clear();

        System.out.println(dvec.value.toString());
        System.out.println(dvec.toString());
    }
}
