package com.ireader.ml.CNN;

/**
 * Created by zxsted on 15-10-22.
 */
public class RunCNN {

    public static void runCnn() {
        //创建一个卷积神经网络
        CNN.LayerBuilder builder = new CNN.LayerBuilder();
        builder.addLayer(Layer.buildInputLayer(new Layer.Size(8, 8)));
        builder.addLayer(Layer.buildConvLayer(6, new Layer.Size(3, 3)));
        builder.addLayer(Layer.buildSampLayer(new Layer.Size(2, 2)));
//        builder.addLayer(Layer.buildConvLayer(12, new Layer.Size(5, 5)));
//        builder.addLayer(Layer.buildSampLayer(new Layer.Size(2, 2)));
        builder.addLayer(Layer.buildOutputLayer(10));
        CNN cnn = new CNN(builder, 50);

        //导入数据集
        String fileName = "/home/zxsted/data/cnn/digits.txt";
        Dataset dataset = Dataset.load(fileName, " ", 0);
        cnn.train(dataset, 100);//
//        String modelName = "model/model.cnn";
//        cnn.saveModel(modelName);
        dataset.clear();
        dataset = null;

//        //预测
//        // CNN cnn = CNN.loadModel(modelName);
//        Dataset testset = Dataset.load("dataset/test.format", ",", -1);
//        cnn.predict(testset, "dataset/test.predict");
    }

    public static void main(String[] args) {

        new TimedTest(new TimedTest.TestTask() {

            @Override
            public void process() {
                runCnn();
            }
        }, 1).test();
        ConcurenceRunner.stop();

    }

}
