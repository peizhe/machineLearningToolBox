package com.ireader.ml.classifier.nn.cnn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zxsted on 15-10-19.
 *
 *
 */
public class Dataset {

    // 保存数据
    private List<Record> records;

    // 类别下标 (指示 label 是在前还是最后)
    private  int labelIndex;

    private double maxLable = -1;

    public Dataset(int classIndex) {

        this.labelIndex = classIndex;
        records = new ArrayList<Record>();
    }

    public Dataset(List<double[]> datas) {
        this();
        for (double[] data:datas) {
            append(new Record(data));
        }
    }

    private Dataset() {
        this.labelIndex = -1;
        records = new ArrayList<Record>();
    }

    public int size() {
        return records.size();
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public void append(Record record) {
        records.add(record);
    }

    /**
     *  清空数据
     * */
    public void clear() {
        this.records.clear();
    }

    /**
     * 添加一个记录
     * */
    public void append(double[] attrs,Double lable) {
        records.add(new Record(attrs, lable));
    }

    /***/
    public Iterator<Record> iter() {
        return records.iterator();
    }

    /**
     *  获取第Index 条记录的属性
     *
     * */
    public double[] getAttrs(int index) {
        return records.get(index).getAttrs();
    }



    public Double getLable(int index) {
        return records.get(index).getLable();
    }


    /**
     * 导入数据集
     *
     * */
    public static Dataset load(String filePath,String tag,int lableIndex) {

        Dataset dataset = new Dataset();
        dataset.labelIndex = lableIndex;

        File file = new File(filePath);

        try{

            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;

            while((line = in.readLine()) != null) {
                String[] datas = line.split(tag);
                if (datas.length == 0)
                    continue;

                double[] data = new double[datas.length];
                for (int i = 0 ; i < datas.length; i++) {
                    data[i] = Double.parseDouble(datas[i]);
                }
                Record record = dataset.new Record(data);
                dataset.append(record);

//                System.out.println(Arrays.toString(record.attrs));
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
        System.out.println("导入数据：" + dataset.size());
        return dataset;
    }


    /**
     *  数据记录（实例），记录由属性和类别组成， 类别必须为第一列或者最后一列，或者为空
     * */
    public  class Record {

        // 存储数据
        private double[] attrs;
        private Double lable;

        private Record(double[] attrs,Double lable) {
            this.attrs = attrs;
            this.lable = lable;
        }

        public Record(double[] data) {
            if (labelIndex == -1)
                attrs = data;
            else {
                lable = data[labelIndex];
                if (lable > maxLable)
                    maxLable = lable;
                if (labelIndex == 0)
                    attrs = Arrays.copyOfRange(data,1,data.length);
                else
                    attrs = Arrays.copyOfRange(data,0,data.length);
            }

        }

        /**
         *  记录的属性
         *
         * */
        public double[] getAttrs() {
            return attrs;
        }

        public void setAttrs(double[] attrs) {
            this.attrs = attrs;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("attrs:");
            sb.append(Arrays.toString(attrs));
            sb.append("label:");
            sb.append(lable);
            return sb.toString();
        }

        /**
         *  该记录的类标
         * */
        public Double getLable() {
            if(labelIndex == -1)
                return  null;
            return  lable;
        }

        /**
         *  对类标记进行二进制编码
         * */
        public int[] getEncodeTarget(int n) {
            String binary = Integer.toBinaryString(lable.intValue());
            byte[] bytes = binary.getBytes();
            int[] encode = new int[n];
            int j = n;

            for (int i = bytes.length-1;i >= 0; i--)
                encode[--j] = bytes[i] - '0';
            return encode;
        }

        /**
         *
         * */
        public double[] getDoubleEncodeTarget(int n) {
            String binary = Integer.toBinaryString(lable.intValue());
            byte[] bytes = binary.getBytes();
            double[] encode = new double[n];
            int j = n;
            for (int i = bytes.length - 1; i >= 0; i--)
                encode[--j] = bytes[i] - '0';
            return encode;
        }
    }

    /**
     * 获取 第 index 条记录
     *
     * */
    public Record getRecord(int index) {
        return records.get(index);
    }
}
