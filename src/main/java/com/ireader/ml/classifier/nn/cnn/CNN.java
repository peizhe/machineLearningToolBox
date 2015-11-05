package com.ireader.ml.classifier.nn.cnn;

import com.ireader.ml.classifier.nn.Activation;
import com.ireader.util.ConcurenceRunner;

import static com.ireader.ml.Matrix.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zxsted on 15-10-14.
 */
public class CNN implements Serializable {

    private static final long serialVersionUID = -5747602504947498067L;

    private static double ALPHA = 0.85;
    protected static final double LAMBDA = 0.0;

    // 网络的各层
    private List<Layer> layers;
    // 层数
    private int layerNum;

    // 批量更新的大小
    private int batchSize;
    // 除数操作符， 对矩阵的每一个元素除以一个值
    private Operator divide_batchSize;

    // 乘数操作符， 对矩阵的每个元素乘以一个值
    private Operator multiply_alpha;

    // 乘数操作符， 对矩阵每个元素 乘以 1 - lambda * alpha 的值
    private Operator multiply_lambda;

    private Operator actor ;

    private Operator dactor;

    private String activation = "sigmod";

    /**
     * 初始化网络
     */
    public CNN(LayerBuilder layerBuilder, final int batchSize) {
        layers = layerBuilder.mLayers;
        layerNum = layers.size();
        this.batchSize = batchSize;
        setup(batchSize);      // 初始化 各层的数据结构
        initPerator();
    }

    /**
     * 初始化操作符
     */
    private void initPerator() {

        // 除以batchsize
        divide_batchSize = new Operator() {
            @Override
            public double process(double value) {
                return value / batchSize;
            }
        };

        // 乘以 alpha
        multiply_alpha = new Operator() {
            @Override
            public double process(double value) {
                return value * ALPHA;
            }
        };

        // 乘以lambda
        multiply_lambda = new Operator() {
            @Override
            public double process(double value) {
                return value * (1 - LAMBDA * ALPHA);
            }
        };

        // 激活函数
        if (activation.equalsIgnoreCase("sigmod")) {
            actor = Activation.sigmod;
            dactor = Activation.dsigmod;
        } else if (activation.equalsIgnoreCase("tanh")) {
            actor = Activation.tanh;
            dactor = Activation.dtanh;
        } else if (activation.equalsIgnoreCase("ReLU")) {
            actor = Activation.ReLU;
            dactor = Activation.dReLU;
        } else {
            throw new RuntimeException("激活函数" + activation + "还未实现");
        }
    }


    /** ====================== cnn 对外训练 预测调用接口 ===============================================*/


    /**
     * 设置 CNN 网络的每一层的参数 (构造函数中调用)
     */
    public void setup(int batchSize) {

        Layer inputLayer = layers.get(0);

        // 每一层都初始化输出map
        inputLayer.initOutmaps(batchSize);

        // 1 开始初始化隐藏层
        for (int i = 1; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            Layer frontLayer = layers.get(i - 1);
            int frontMapNum = frontLayer.getOutMapNum();

            switch (layer.getType()) {

                // 输入层
                case input:
                    break;

                // 卷积层
                case conv:
                    // 设置 map 的大小, 前一层 经过卷积后的size
                    layer.setMapSize(frontLayer.getMapSize().substract(
                            layer.getKernelSize(), 1));
                    // 根据前一层的特征图来决定初始化 kernel 的个数 为： frontMapNum * this.MapNum
                    layer.initKernel(frontMapNum);

                    // 初始化偏置，共有frontMapum * outMapNum 个
                    layer.initBias(frontMapNum);

                    // batch的每个记录都要保持一份残差
                    layer.initErros(batchSize);

                    // 每一层都要初始化输出map
                    layer.initOutmaps(batchSize);

                    break;

                // 如果是采样层
                case samp:
                    // 采样层的map的书目同上一层的个数相同
                    layer.setOutMapNum(frontMapNum);
                    // 采样层的map的大小
                    layer.setMapSize(frontLayer.getMapSize().divide(
                            layer.getScaleSize()));

                    //batch 的每个记录都要保持一份残差
                    layer.initErros(batchSize);

                    // 每一层都要初始化输出map
                    layer.initOutmaps(batchSize);

                    break;

                // 如果是输出层
                case output:
                    // 初始化权重（卷积核），输出层的卷积核大小为上一层的map大小
                    layer.initOutputKerkel(frontMapNum, frontLayer.getMapSize());
                    // 初始化偏置，共有frontMapNum*outMapNum个偏置
                    layer.initBias(frontMapNum);
                    // batch的每个记录都要保持一份残差
                    layer.initErros(batchSize);
                    // 每一层都需要初始化输出map
                    layer.initOutmaps(batchSize);
                    break;

            }
        }

    }


    /**
     * 在训练集上训练网络
     * <p/>
     * 迭代次数
     */
    public void train(Dataset trainset, int repeat) {

        // 监听停止按钮
        new Listenter().start();

        for (int t = 0; t < repeat && !stopTrain.get(); t++) {

            int epochsNum = trainset.size() / batchSize;    // epcichsNum 是循环的批次的个数

            if (trainset.size() % batchSize != 0)
                epochsNum++;    // 多抽取一次，向上取整

            System.out.println("");
            System.out.println(t + "th iter epochsNum:" + epochsNum);

            int right = 0;
            int count = 0;

            for (int i = 0; i < epochsNum; i++) {
                int[] randPerm = randomPerm(trainset.size(), batchSize);
                Layer.prepareForNewBatch();   // 清空 当前层的 batch 内计数

                /** 对当前批次进行计算*/
                for (int index : randPerm) {
                    boolean isRight = train(trainset.getRecord(index));
                    if (isRight)
                        right++;
                    count++;
                    Layer.prepareForNewRecord(); // batch内计数增加1
                }

                /** 跑完一个batch 后进行权重的更新*/
                updateParas();  // 将该函数进行拆分为： gradient 和 updateeight

                /** 跑完50个批次 就显示一下进度 */
                if (i % 50 == 0) {
                    System.out.print("..");
                    if (i + 50 > epochsNum)
                        System.out.println();
                }

            }

            double p = 1.0 * right / count;  // 正确预测的样本个数与已经训练完成的比例

            // 动态的调整学习速率
            if (t % 10 == 1 && p > 0.85 && ALPHA > 0.001 ) {
                ALPHA = 0.001 + ALPHA * 0.9;
                System.out.println("调整学习速率为：" + ALPHA);
            }

            System.out.println("precision " + right + "/" + count + "=" +p);
        }

        ConcurenceRunner.stop();
    }


    /**
     * 训练一条数据， 同时返回是否预测正确当前结果
     * <p/>
     * 注意这里的 流程是， train 一条数据
     * <p/>
     * 而到一个batchsize的 数据时， 使用批量更新
     */
    private boolean train(Dataset.Record record) {

        forward(record);
        boolean result = backPropagation(record);
        return result;
    }


    /**
     * 判断 输出与目标函数是否相同
     */
    private boolean isSame(double[] output, double[] target) {

        boolean r = true;

        for (int i = 0; i < output.length; i++)
            if (Math.abs(output[i] - target[i]) > 0.5) {
                r = false;
                break;
            }

        return r;
    }

    /**
     * 预测
     */
    public double[] predict(Dataset testset) {

        int max = layers.get(layerNum - 1).getClassNum();
        Layer.prepareForNewBatch();
        Iterator<Dataset.Record> iter = testset.iter();

        double[] retArr = new double[testset.size()];

        int i = 0;
        while (iter.hasNext()) {
            Dataset.Record record = iter.next();
            forward(record);
            Layer outputLayer = layers.get(layerNum - 1);

            int mapNum = outputLayer.getOutMapNum();
            double[] out = new double[mapNum];

            for (int m = 0; m < mapNum; m++) {
                double[][] outmap = outputLayer.getMap(m);
                out[m] = outmap[0][0];
            }

            int lable = getMaxIndex(out);
            retArr[i++] = lable;
        }

        return retArr;

    }

    /**
     * 测试准确度
     */
    public double test(Dataset trainset) {

        Layer.prepareForNewBatch();
        Iterator<Dataset.Record> iter = trainset.iter();
        int right = 0;

        while (iter.hasNext()) {

            Dataset.Record record = iter.next();
            forward(record);

            // 取出输出层
            Layer outputLayer = layers.get(layerNum - 1);

            int mapNum = outputLayer.getOutMapNum();
            double[] out = new double[mapNum];

            for (int m = 0; m < mapNum; m++) {
                double[][] outmap = outputLayer.getMap(m);
                out[m] = outmap[0][0];
            }

            if (record.getLable().intValue() == getMaxIndex(out))
                right++;
        }

        // 计算正确预测的个数
        double p = 1.0 * right / trainset.size();

        return p;

    }


    /**
     * 前向传播（计算一条数据）：
     */
    private void forward(Dataset.Record record) {

        // 设置输入层的map
        setConvInLayerOutput(record);

        for (int l = 1; l < layers.size(); l++) {

            Layer layer = layers.get(l);
            Layer lastLayer = layers.get(l - 1);

            String type = null;
            switch (layer.getType()) {

                case conv:
                    setConvOutput(layer, lastLayer);

                    type = "conv";
                    break;
                case samp:
                    setSampOutput(layer, lastLayer);
                    type = "samp";
                    break;
                case output:
                    setConvOutput(layer, lastLayer);
                    type = "output";
                    break;

                default:
                    break;

            }
//            System.out.print("第" + l+"层:"+type+" map size：");
//            printSize(layer.getMap(0));
        }
    }

    /**
     * 反向传播一条记录
     */
    private boolean backPropagation(Dataset.Record record) {

        boolean result = setOutLayerErrors(record);
        setHiddenLayerErrors();
        return result;
    }


    /**
     * 残差计算：
     * 隐藏层统一调用接口
     */
    public void setHiddenLayerErrors() {

        for (int l = layerNum - 2; l > 0; l--) {
            Layer layer = layers.get(l);
            Layer nextLayer = layers.get(l + 1);

            switch (layer.getType()) {

                case samp:
                    setSampErrors(layer, nextLayer);
                    break;

                case conv:
                    setConvErrors(layer, nextLayer);
                    break;
                default:// 只有采样层和卷积层需要处理残差，输入层没有残差，输出层已经处理过
                    break;
            }
        }
    }

    /**
     * 更新参数
     */
    private void updateParas() {

        for (int l = 1; l < layerNum; l++) {

            Layer layer = layers.get(l);
            Layer lastLayer = layers.get(l - 1);

            switch (layer.getType()) {
                case conv:
                case output:
                    updateKernels(layer, lastLayer);
                    updateBias(layer, lastLayer);
//                    System.out.println("当前层的梯度为：" + Arrays.toString(subArray(reval(layer.getKernel()[0][0]), 0, 5)));
                    break;
                default:
                    break;
            }
        }
    }



    /** =============================================================================================*/


    /** ====================== cnn 多线程 训练 预测 模块=================================================*/


    /* ======================= 反向部分 ==============================================================*/

    /**
     * 梯度计算与 权重更新 ： 计算出当前层的error（其实是 delta ，而这里的delta 实际上是梯度）后，计算偏置的导数，然后更新偏置
     *
     * @Param layer
     * @Param lastLayer
     */
    private void updateBias(final Layer layer, Layer lastLayer) {

        final double[][][][] errors = layer.getErrors();

        int mapNum = layer.getOutMapNum();

        new ConcurenceRunner.TaskManager(mapNum) {

            @Override
            public void process(int start, int end) {

                for (int j = start; j < end; j++) {
                    double[][] error = sum(errors, j);    // 本层的各个输出map的error矩阵

                    // 更新偏置，这里的deltaBias 是 梯度
                    double deltaBias = sum(error) / batchSize;   // 计算偏置的梯度 ： error map矩阵 的和

                    double bias = layer.getBias(j) + ALPHA * deltaBias;     // 更新偏置
                    layer.setBias(j, bias);
                }
            }
        }.start();
    }

    /**
     * 梯度计算与 权重更新 ： 计算 出当前层的 error （其实是 delta ，而这里的delta 实际上是梯度）后，计算kernel的导数，然后更新kernel
     */
    private void updateKernels(final Layer layer, final Layer lastLayer) {

        int mapNum = layer.getOutMapNum();
        final int lastMapNum = lastLayer.getOutMapNum();

        new ConcurenceRunner.TaskManager(mapNum) {

            @Override
            public void process(int start, int end) {

//                double[][] deltaKernel = null;
                for (int j = start; j < end; j++) {
                    for (int i = 0; i < lastMapNum; i++) {    // i，j 循环所有的卷积核
                        // 对每个delta 求和
                        double[][] deltaKernel = null;
                        for (int r = 0; r < batchSize; r++) {   // batch 中循环

//                            System.out.print("误差矩阵");
//                            printSize(layer.getError(r, j));
//                            System.out.print("输出矩阵");
//                            printSize(lastLayer.getMap(r, i));

                            double[][] error = layer.getError(r, j);
                            if (deltaKernel == null)  // 此时 r = 0 ， 即 batch中的第一个map
                                // 计算 卷积核 的梯度是 将当前层的的误差图片 与 输入map 进行卷积
                                // 这样恰好就是卷积的梯度 （kernelsize = lastmap.size - curmap.size + 1）
                                deltaKernel = convnValid(lastLayer.getMap(r, i), error);
                            else { // 累加 batch中的所有 样本的 梯度，以及kernel的
                                deltaKernel = matrixOp(convnValid(lastLayer.getMap(r, i), error),
                                        deltaKernel, null, null, plus);
                            }
                        }

//                        System.out.print("误差矩阵");
//                        printSize(layer.getError(0, j));
//                        System.out.print("输出矩阵");
//                        printSize(lastLayer.getMap(0, i));

                        // 计算梯度的平均值， 除以batchsize
                        deltaKernel = matrixOp(deltaKernel, divide_batchSize);

                        // 更新卷积核
                        double[][] kernel = layer.getKernel(i, j);

//                        System.out.print("卷积核");
//                        printSize(kernel);
//                        System.out.print("梯度矩阵");
//                        printSize(deltaKernel);

//                        System.out.println(Arrays.toString(reval(deltaKernel)));

//                        deltaKernel = matrixOp(kernel, deltaKernel, multiply_lambda, multiply_alpha, plus);
                        double[][] newkernel = matrixOp(kernel, deltaKernel, multiply_lambda, multiply_alpha, plus);

//                        layer.setKernel(i, j, deltaKernel);
                        layer.setKernel(i, j, newkernel);

//                       if(i==0&&j==0) System.out.println("当前层 (" + start + "," + end + ") 的梯度为：" + Arrays.toString(subArray(reval(deltaKernel), 0, 5)));

                    }
                }


            }
        }.start();

//        System.out.println("当前层的梯度为：" + Arrays.toString(subArray(reval(layer.getKernel()[0][0]),0,5 )));
    }


    /**
     * 残差计算：
     * 设置采样层的残差 （设置实际上就是计算）
     * <p/>
     * 这里应该改写成 对下一层的类型进行判断 ， 1. 如果下一层是卷积层 2. 如果下一层是全链接层
     *
     * @param layer
     * @param nextLayer
     */
    private void setSampErrors(final Layer layer, final Layer nextLayer) {

        int mapNum = layer.getOutMapNum();
        final int nextMapNum = nextLayer.getOutMapNum();

        new ConcurenceRunner.TaskManager(mapNum) {

            @Override
            public void process(int start, int end) {
                for (int i = start; i < end; i++) {
                    double[][] sum = null;   // 对每一个卷积进行求和
                    for (int j = 0; j < nextMapNum; j++) {
                        double[][] nextError = nextLayer.getError(j);
                        double[][] kernel = nextLayer.getKernel(i, j);

                        // 对卷积核 进行180 度旋转， 然后进行full 模式下的卷积(下一层是卷积层)
                        if (sum == null)
                            sum = convnFull(nextError, rot180(kernel));
                        else
                            sum = matrixOp(convnFull(nextError, rot180(kernel)), sum, null, null, plus);

                    }
                    layer.setError(i, sum);
                }
            }
        }.start();
    }

    /**
     * 残差计算：
     * 设置卷积层的残差
     * <p/>
     * 卷积层的下一层为采样层， 即两层的map个数相同， 且一个map 只与另一层的一个map链接
     * 因此只需要将下一层的残差 惊醒 克罗内科 扩展后再用点积即可
     *
     * @ param layer
     * @ params nextLayer
     */
    private void setConvErrors(final Layer layer, final Layer nextLayer) {
        int mapNum = layerNum;

        new ConcurenceRunner.TaskManager(mapNum) {

            @Override
            public void process(int start, int end) {
                for (int m = start; m < end; m++) {
                    Size scale = nextLayer.getScaleSize();
                    double[][] nextError = nextLayer.getError(m);
                    double[][] map = layer.getMap(m);

                    // 矩阵相乘， 但对第二个矩阵的每个元素value 进行1-value操作
                    double[][] outMatrix = matrixOp(map,
                            cloneMatrix(map), null, one_value, multiply);

//                    double[][] outMatix = matrixOp(map,dactor);


                    if(outMatrix == null) throw new RuntimeException("输出矩阵为空");
                    if(nextError == null) throw new RuntimeException("输出矩阵为空");
                    if(kronecker(nextError, scale) == null) throw new RuntimeException("克罗内克结果为空");

                    outMatrix = matrixOp(outMatrix,
                            kronecker(nextError, scale), null, null, multiply);
                    layer.setError(m, outMatrix);
                }
            }
        }.start();
    }


    /**
     * 残差计算：
     * 计算输出层的残差
     * return boolean 正向预测的结果是否同样本的标签相同
     */
    private boolean setOutLayerErrors(Dataset.Record record) {

        Layer outputLayer = layers.get(layerNum - 1);
        int mapNum = outputLayer.getOutMapNum();

        double[] target = new double[mapNum];
        double[] outmaps = new double[mapNum];

        // 这里默认最后一层采样， 以及将特征map变成了 1*1 大小的了
        for (int m = 0; m < mapNum; m++) {
            double[][] outmap = outputLayer.getMap(m);
            outmaps[m] = outmap[0][0];
        }

        int lable = record.getLable().intValue();
        target[lable] = 1;

        for (int m = 0; m < mapNum; m++) {
            outputLayer.setError(m, 0, 0, outmaps[m] * (1 - outmaps[m])
                    * (target[m] - outmaps[m]));
        }

        // 返回 预测的值是否同训练数据相同
//        System.out.println("数据的label为：" +lable+ "预测结果为：" + getMaxIndex(outmaps));

        return lable == getMaxIndex(outmaps);
    }





    /** ======================= 前向传播部分 ==========================================================*/


    /**
     * 废弃不用
     * 前向传播
     * 设置输入层的输出值, 样本数据是一维数组，
     * 输入层要根据 输入map 将一维数组 变为二位数组
     */
    @Deprecated
    private void setInLayerOutput(Dataset.Record record) {

        final Layer inputLayer = layers.get(0);
        final Size mapSize = inputLayer.getMapSize();
        final double[] attr = record.getAttrs();

        if (attr.length != mapSize.x * mapSize.y)
            throw new RuntimeException("记录数据的大小与定义的map大小不一致！");

        for (int i = 0; i < mapSize.x; i++) {
            for (int j = 0; j < mapSize.y; j++) {
                // 将记录属性的一维向量 resize 为二维矩阵
                inputLayer.setMapValue(0, i, j, attr[mapSize.x * i + j]);
            }
        }

//        System.out.print("input 输入的map size：");
//        printSize( inputLayer.getMap(0));

    }


    /**
     *  全链接输入层
     *  前向传播
     * 设置全链接输入层的输出值, 样本数据是一维数组，
     * **/
    private void setInputLayerOutput(Dataset.Record record) {

        Layer inputlayer = layers.get(0);
        double[] attrs = record.getAttrs();
        inputlayer.setAout(attrs);
    }

    /**
     * 前向传播
     * 设置输入层的输出值, 样本数据是一维数组，
     * 输入层要根据 输入map 将一维数组 变为二位数组
     */
    private void setConvInLayerOutput(Dataset.Record record) {

        final Layer inputLayer = layers.get(0);

        int nmap = inputLayer.getOutMapNum(); // 输出层的 特征图像的个数
        final Size mapSize = inputLayer.getMapSize();  // 输出层特征图像的size

        double[] attr = record.getAttrs();

        if(attr.length != nmap *  mapSize.x * mapSize.y)
            throw new RuntimeException("记录数据的大小与定义的map大小不一致！");

        for (int m = 0 ; m < nmap ; m++)
            for (int i = 0 ; i < mapSize.x ;i++)
                for (int j = 0 ; j < mapSize.y;j++) {
                    inputLayer.setMapValue(m,i,j,attr[mapSize.x * mapSize.y * m + mapSize.x * i + j]);
                }

    }




    /**
     * 前向计算输出值， 每个线程负责一个部分map
     */
    private void setConvOutput(final Layer layer, final Layer lastLayer) {

        int mapNum = layer.getOutMapNum();                 // 输出map个数

        final int lastMapNum;

        if(lastLayer.getType() == Layer.LayerType.hidden)
            lastMapNum = 1;
        else
            lastMapNum = lastLayer.getOutMapNum();   // 输入马匹个数


        /** 使用线程池管理器 ConcurenceRunner： 进行多线程计算 各个输出map*/
        new ConcurenceRunner.TaskManager(mapNum) {

            @Override
            public void process(int start, int end) {
                for (int j = start; j < end; j++) {
                    double[][] sum = null;   // 对每一个输入map的卷积进行求和

                    if (lastLayer.getType() != Layer.LayerType.hidden) {

                        for (int i = 0; i < lastMapNum; i++) {

                            double[][] lastMap = lastLayer.getMap(i);
                            double[][] kernel = layer.getKernel(i, j);

                            if (sum == null)
                                sum = convnValid(lastMap, kernel);
                            else
                                sum = matrixOp(convnValid(lastMap, kernel), sum,
                                        null, null, plus);
                        }
                        // 上一层是 全链接层
                    } else {

                        double[] lastout = lastLayer.getAout();
                        double[][] lastMap = reSize(lastout,(int) Math.sqrt(lastout.length),(int) Math.sqrt(lastout.length));
                        double[][] kernel = layer.getKernel(0, j);

                        sum = matrixOp(convnValid(lastMap, kernel), sum,
                                null, null, plus);
                    }

                    final double bias = layer.getBias(j);

//                    sum = matrixOp(sum, new Operator() {
//                        @Override
//                        public double process(double value) {
//                            return sigmod(value + bias);
//                        }
//                    });

                    sum = matrixOp(sum,sigmod);

//                    double[][] biasmat = fill(bias,sum.length,sum[0].length);
//                    sum = matrixOp(sum,biasmat,null,null,plus);
//                    sum = matrixOp(sum,actor);

//                    System.out.print("conv 输出的map size：");
//                    printSize(sum);
//                    System.out.print("conv 输入的map size：");
//                    printSize( lastLayer.getMap(0));

                    layer.setMapValue(j, sum);    // 输出值
                }
            }
        }.start();
    }

    /**
     * 前向传播：
     * 计算采样层的输出值，这里的采样层是进行均值处理
     */
    private void setSampOutput(final Layer layer, final Layer lastLayer) {

        int lastMapNum = lastLayer.getOutMapNum();

        new ConcurenceRunner.TaskManager(lastMapNum) {

            @Override
            public void process(int start, int end) {

                for (int i = start; i < end; i++) {

                    double[][] lastMap = lastLayer.getMap(i);
                    Size scaleSize = layer.getScaleSize();

                    // 按照scaleSize 区域进行均值处理
                    double[][] sampleMatrix = scaleMatrix(lastMap, scaleSize);


//                    System.out.print("sample 输出的map size：");
//                    printSize(sampleMatrix);
//                    System.out.print("sample 输入的map size：");
//                    printSize(lastMap);

                    layer.setMapValue(i, sampleMatrix);
                }
            }
        }.start();
    }


    /** ============================  全链接隐藏层的 设置 =================================================================*/


     /** 1。 forword (layer,lastlayer ) 逐个样本计算*/
    /**
     *  前向计算隐藏层的数据
     *
     *  @param layer:
     *              Layer  当前层
     *  @param lastlayer:
     *                  Layer 前一层
     * */
    private void setHiddenOutput(final Layer layer,final Layer lastlayer)  {

        double[][] wMat = layer.getWmat();       // 取出本层的权重矩阵    nin * nout
        double[] bias = layer.getBarr();         // 取出本层的偏置       nout

        double[][] curfeatval = new double[1][];

        /** 如果前面一层是全链接隐藏层，或者全链接输入层 */
        if(lastlayer.getType() == Layer.LayerType.hidden || lastlayer.getType() == Layer.LayerType.normalInput) {

            double[] lastLayerout = lastlayer.getAout();

            curfeatval[0] = lastLayerout;

        } else if (lastlayer.getType() == Layer.LayerType.conv || lastlayer.getType() == Layer.LayerType.samp) {

            int lastMapNum = lastlayer.getOutMapNum();
            Size lastMapSize = lastlayer.getMapSize();

            // 取出前一层在当前 RecordInBatch 的输出的maps
            double[][][] maps = lastlayer.getMaps()[lastlayer.getRecordInBatch()];   // batchsize * mapnum * row * col

            // 将上一层的输出map展平
            double[] revalmat = new double[lastMapNum * lastMapSize.x * lastMapSize.y];


            for (int j = 0 ; j < maps[0].length;j++)
                for (int r = 0; r < lastMapSize.x;r++)
                    for (int c = 0 ; c < lastMapSize.y; c++)
                        revalmat[j*lastMapSize.x*lastMapSize.y + r*lastMapSize.y + c] = maps[j][r][c];
            // 展平后的map 转化成矩阵 ，方便计算

            curfeatval[0] = revalmat;
        }

        double[][] inMat = addVec(dot(curfeatval, wMat), bias, 1);

        double[] aout = matrixOp(inMat,Activation.sigmod)[0];

        layer.setAout(aout);
    }




    /** 2. backword(layer, nextlayer )  负责计算本层的 error  逐个样本计算 */

    /**
     * @param layer Layer : 当前层
     * @param layer nextLayer: 下一层
     *
     * */
    private void setHiddenErrors(final Layer layer,final Layer nextLayer) {

        // 如果下一层是 卷积层或者sample层
        if(nextLayer.getType() == Layer.LayerType.conv ) {

            final int nextMapNum = nextLayer.getOutMapNum();


            double[][] sum = null;   // 对每一个卷积进行求和
            for (int j = 0; j < nextMapNum; j++) {
                double[][] nextError = nextLayer.getError(j);
                double[][] kernel = nextLayer.getKernel(0, j);

                // 对卷积核 进行180 度旋转， 然后进行full 模式下的卷积(下一层是卷积层)
                if (sum == null)
                    sum = convnFull(nextError, rot180(kernel));
                else
                    sum = matrixOp(convnFull(nextError, rot180(kernel)), sum, null, null, plus);

            }

            layer.setErr(reval(sum));

        } else if (nextLayer.getType() == Layer.LayerType.softmax || nextLayer.getType() == Layer.LayerType.hidden) {



        }

    }


    /** 3. gradient（layer,lastlayer）  负责计算本层的梯度  ： 批次计算  **/


    /** 4. 跟新梯度（）*/


    /** ================================ 全链接 softmax 的设置 =============================================================*/

    /** 1。 forword (layer,lastlayer )  计算本层的输出值 逐个样本计算*/



    /** 2. backword(layer, nextlayer )  负责计算本层的 error 逐个样本计算*/



    /** 3. gradient（layer,lastlayer）  负责计算本层的梯度  ： 批次计算 **/


    /** 4. 跟新梯度（）*/


    /** ===================================================================================================================*/


    /** ================================================================================================================= */


    private static AtomicBoolean stopTrain;

    static class Listenter extends Thread {

        Listenter() {

            setDaemon(true);
            stopTrain = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            System.out.println("Input & to stop train.");

            while (true) {
                try {
                    int a = System.in.read();

                    if (a == '&') {
                        stopTrain.compareAndSet(false, true);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Lisenter stop");
        }
    }


    /**
     * 构造者模式构造各层， 要求倒数第二层必须为采样层而不能为卷积层
     */
    public static class LayerBuilder {

        private List<Layer> mLayers;

        public LayerBuilder() {
            mLayers = new ArrayList<Layer>();
        }

        public LayerBuilder(Layer layer) {
            this();
            mLayers.add(layer);
        }

        public LayerBuilder addLayer(Layer layer) {
            mLayers.add(layer);
            return this;
        }
    }

    /**
     * 序列化保存model
     */
    public void saveModel(String fileName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(fileName));

            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反序列化 导入model
     */
    public static CNN loadModel(String fileName) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                    fileName));
            CNN cnn = (CNN) in.readObject();
            in.close();
            return cnn;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
