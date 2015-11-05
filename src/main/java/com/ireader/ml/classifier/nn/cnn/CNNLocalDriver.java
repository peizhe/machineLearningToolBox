package com.ireader.ml.classifier.nn.cnn;

import com.ireader.ml.Matrix;

/**
 * Created by zxsted on 15-10-22.
 *
 * 卷积神经网络的 本地调用测试
 */
public class CNNLocalDriver {

    public static void runCnn() {
        // 创建一个卷积神经网络
        CNN.LayerBuilder builder = new CNN.LayerBuilder();

        // 向builder中添加layer
        builder.addLayer(Layer.buildInputLayer(1,new Matrix.Size(8, 8)));
        builder.addLayer(Layer.buildConvLayer(6, new Matrix.Size(3, 3)));
        builder.addLayer(Layer.buildSampLayer(new Matrix.Size(2, 2)));
        builder.addLayer(Layer.buildConvLayer(24, new Matrix.Size(2, 2)));
        builder.addLayer(Layer.buildSampLayer(new Matrix.Size(1, 1)));
        builder.addLayer(Layer.buildOutputLayer(10));

        CNN cnn = new CNN(builder,20);


        // 导入数据集
        String filename = "/home/zxsted/data/cnn/digits.txt";
        Dataset dataset = Dataset.load(filename," ",0);
        cnn.train(dataset,300);

//        String modelName = "/home/zxsted/data/cnn/model.cnn";
//        cnn.saveModel(modelName);
        dataset.clear();
        dataset = null;

        // 预测
//        Dataset testset = Dataset.load("/home/zxted/data/cnn/digits_test_data.txt"," ",-1);
//        cnn.predict(testset,"/home/zxted/data/cnn/test.predict");
//        cnn.predict(testset);


    }

    public static void main(String[] args) {
        runCnn();
    }
}
