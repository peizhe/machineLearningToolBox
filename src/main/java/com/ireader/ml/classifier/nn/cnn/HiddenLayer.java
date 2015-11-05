package com.ireader.ml.classifier.nn.cnn;
//
//import static com.ireader.ml.Matrix.*;
//
///**
// * Created by zxsted on 15-10-21.
// */
//public class HiddenLayer extends Layer{
//
//
//       // 批量更新的大小
//       int batchSize;
//       // 除数操作符， 对矩阵的每一个元素除以一个值
//       Operator divide_batchSize;
//
//       // 乘数操作符， 对矩阵的每个元素乘以一个值
//       Operator multiply_alpha;
//
//       // 乘数操作符， 对矩阵每个元素 乘以 1 - lambda * alpha 的值
//       Operator multiply_lambda;
//
//       Operator actor;
//
//       Operator dactor;
//
//       String activation;
//
//       double ALPHA;
//
//       double LAMBDA;
//
//
//
//    public  HiddenLayer() {
//        super();
//    }
//
//
//    /**
//     * 初始化操作符
//     */
//    public  void  initPerator() {
//
//        // 除以batchsize
//        divide_batchSize = new Operator() {
//            @Override
//            public double process(double value) {
//                return value / batchSize;
//            }
//        };
//
//        // 乘以 alpha
//        multiply_alpha = new Operator() {
//            @Override
//            public double process(double value) {
//                return value * ALPHA;
//            }
//        };
//
//        // 乘以lambda
//        multiply_lambda = new Operator() {
//            @Override
//            public double process(double value) {
//                return value * (1 - LAMBDA * ALPHA);
//            }
//        };
//
//        // 激活函数
//        if (activation.equalsIgnoreCase("sigmod")) {
//            actor = Activation.sigmod;
//            dactor = Activation.dsigmod;
//        } else if (activation.equalsIgnoreCase("tanh")) {
//            actor = Activation.tanh;
//            dactor = Activation.dtanh;
//        } else if (activation.equalsIgnoreCase("ReLU")) {
//            actor = Activation.ReLU;
//            dactor = Activation.dReLU;
//        } else {
//            throw new RuntimeException("激活函数" + activation + "还未实现");
//        }
//    }
//
//
//    /**
//     * 前向传播
//     *
//     * */
//    public void setHiddenOutput(Layer layer, Layer lastLayer) throws Exception {
//
//        // 本层的权重矩阵
//        double[][] wMat = layer.getWmat();
//
//        double[][] laout = null;
//        // 取出前一层的输出
//        if (lastLayer.getType() == LayerType.samp || lastLayer.getType() == LayerType.conv) {
//
//            double[][][][] maps = lastLayer.getMaps();    // 前一层是 卷积或者采样层 batchix * mapix * row * col
//
//            int batchsize = maps.length;
//
//            laout = new double[batchsize][]; // 前一层的输出
//
//            for (int i = 0 ; i < batchsize; i++) {
//
//                for (int j = 0 ; j < maps[0].length; j++) // 对于每个特征map
//                    laout[i] = reval(maps[i][j]);
//            }
//
//        } else if (lastLayer.getType() == LayerType.hidden) {
////            laout = lastLayer.getAout();
//        }
//
////        layer.setAout(matrixOp(dot(laout, T(wMat)),sigmod));
//
//    }
//
//    /**
//     * 反向传播
//     * */
//    public void setHiddenLayerErrors(Layer layer,Layer nextLayer) throws Exception {
//
//        double[][] error =  nextLayer.getErr();
//
//        double[][] wMat = nextLayer.getWmat();    // 下一层的的权重矩阵
//
//        double[][]  aout = layer.getAout();   // 本层的输出
//
//        double[][] recverror = dot(error, wMat);
//
//        double[][] gradmat = matrixOp(aout,dactor);
//
//        double[][] curLayerError = matrixOp(gradmat,recverror,null,null,plus);
//
//        layer.setErr(curLayerError);
//    }
//
//
//    /**
//     *  参数更新
//     *
//     * */
//    public void upateWeights(Layer layer,Layer lastLayer) {
//
//        // 本层的权重矩阵
//        double[][] wMat = layer.getWmat();
//
//        double[][] laout = null;
//        // 取出前一层的输出
//        if (lastLayer.getType() == LayerType.samp || lastLayer.getType() == LayerType.conv) {
//
//            double[][][][] maps = lastLayer.getMaps();    // 前一层是 卷积或者采样层 batchix * mapix * row * col
//
//            int batchsize = maps.length;
//
//            laout = new double[batchsize][]; // 前一层的输出
//
//            for (int i = 0 ; i < batchsize; i++) {
//
//                for (int j = 0 ; j < maps[0].length; j++) // 对于每个特征map
//                    laout[i] = reval(maps[i][j]);
//            }
//
//        } else if (lastLayer.getType() == LayerType.hidden) {
//            laout = lastLayer.getAout();
//        }
//
//        // 计算梯度
//
//    }
//}
