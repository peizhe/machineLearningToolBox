package com.ireader.ml.classifier.knnclassifier;

/**
 * Created by zxsted on 15-7-27.
 *
 * 样本类， 从字符串中解析出 特正值， 标签
 */
public class Instance {


    private double[]  attributeValue = null;
    private double label;



    public Instance(String line) {
        String[] values = line.split(" ");
        attributeValue = new double[values.length - 1];

        for(int i = 0; i < attributeValue.length; i++) {
            attributeValue[i]  = Double.parseDouble(values[i]);
        }

        label = Double.parseDouble(values[values.length -1]);
    }

    public double[] getAttributeValue() {
        return attributeValue;
    }

    public double getLabel() {
        return label;
    }
}
