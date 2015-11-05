package com.ireader.ml.classifier.nn.cnn;

import static com.ireader.ml.Matrix.*;

import java.io.Serializable;
import  com.ireader.ml.Matrix.Size;

/**
 * Created by zxsted on 15-10-14.
 *
 * CNN 的一个纯数据结构， 不含所有 训练功能
 */
public class Layer implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5747622503947497069L;

    /* =================== 卷积层参数 ===================================================== */
    private LayerType type;   // 层的类型
    private int outMapNum;    // 输出map的个数
    private Size mapSize;     // map的size
    private Size kernelSize;  // 卷积核的大小
    private Size scaleSize;   // 采样大小
    private double[][][][] kernel;  // 卷积核， 只有卷积层和输出层有
    private double[] bias;          // 每个map 对应一个偏置，只有卷积层和输出层有


    private double[][][][] dkernel;  // 卷积核， 只有卷积层和输出层有
    private double[] dbias;          // 每个map 对应一个偏置，只有卷积层和输出层有

    private String actorType;      // 当前层的激活函数  ， 还没有 实现该功能

    // 保存各个batch的输出map , outmaps[0][0]表示第一条记录训练下第0个输出
    private double[][][][] outmaps;

    // 残差，
    private double[][][][] errors;

    /* ===================================================================================== */

    private static int recordInBatch = 0;  // 记录当前训练的是 batch的第几个记录

    private int classNum = -1;    // 类别个数

    /* =================== 全链接层参数 ===================================================== */

    private double[][] wmat ;     // 隐藏层的权重矩阵
    private double[]   barr;      // 隐藏层的偏置

    private double[][] aout;      // 隐藏层输出
    private double[][] err;       // 隐藏层的反向误差

    private double[][] dw;        // 隐藏层的权重矩阵
    private double[]   db;        // 隐藏层的偏置数组

    private Size wSize;           // 隐藏层权重矩阵的size
    private int hidoutNum;        // 隐藏层输出节点个数

    /* ===================================================================================== */


    public Layer() {

    }

    /**
     * 初始计算一个批次时 ， 初始化 batch内的序号
     */
    public static void prepareForNewBatch() {
        recordInBatch = 0;
    }

    /**
     *
     * 每计算一个新的 样本时， 对batch 内的序号进行递增。
     */
    public static void prepareForNewRecord() {
        recordInBatch++;
    }


    /** 取出当前 record 在batchsize 中的序号 */
    public static int getRecordInBatch () {
        return Layer.recordInBatch;
    }


    /**
     *
     *
     * @param mapSize
     * @return
     */
    public static Layer buildInputLayer(int outputMapNum,Size mapSize) {
        Layer layer = new Layer();
        layer.type = LayerType.input;
        layer.outMapNum = outputMapNum;// ÊäÈë²ãµÄmap¸öÊýÎª1£¬¼´Ò»ÕÅÍ¼
        layer.setMapSize(mapSize);//
        return layer;
    }



    /**
     *
     *
     * @return
     */
    public static Layer buildConvLayer(int outMapNum, Size kernelSize) {
        Layer layer = new Layer();
        layer.type = LayerType.conv;
        layer.outMapNum = outMapNum;
        layer.kernelSize = kernelSize;
        return layer;
    }

    /**
     *
     *
     * @param scaleSize
     * @return
     */
    public static Layer buildSampLayer(Size scaleSize) {
        Layer layer = new Layer();
        layer.type = LayerType.samp;
        layer.scaleSize = scaleSize;
        return layer;
    }

    /**
     *
     *
     * @return
     */
    public static Layer buildOutputLayer(int classNum) {
        Layer layer = new Layer();
        layer.classNum = classNum;
        layer.type = LayerType.output;
        layer.mapSize = new Size(1, 1);
        layer.outMapNum = classNum;

        return layer;
    }


    /**
     *
     *
     * @return
     */
    public Size getMapSize() {
        return mapSize;
    }

    /**
     *
     *
     * @param mapSize
     */
    public void setMapSize(Size mapSize) {
        this.mapSize = mapSize;
    }

    /**
     *
     *
     * @return
     */
    public LayerType getType() {
        return type;
    }

    /**
     *
     *
     * @return
     */

    public int getOutMapNum() {
        return outMapNum;
    }

    /**
     *
     *
     * @param outMapNum
     */
    public void setOutMapNum(int outMapNum) {
        this.outMapNum = outMapNum;
    }

    /**
     *
     *
     * @return
     */
    public Size getKernelSize() {
        return kernelSize;
    }

    /**
     *
     *
     * @return
     */
    public Size getScaleSize() {
        return scaleSize;
    }

    enum LayerType {

        input,          // 卷积输入层
        normalInput,    // 全链接输入层
        output,         // 卷积输出层
        conv,           // 卷积层
        samp,           // 采样层

        hidden,         // 全链接隐藏层
        softmax,        // softmax 输出层
        linear,         // 线性输出层 使用mse 损失函数
        sigmod,         // sigmoid 输出层 使用交叉熵损失函数
    }



    /**
     *
     *
     * @param frontMapNum
     */
    public void initKernel(int frontMapNum) {
//		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        this.kernel = new double[frontMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for (int i = 0; i < frontMapNum; i++)
            for (int j = 0; j < outMapNum; j++)
                kernel[i][j] = randomMatrix(kernelSize.x, kernelSize.y, true);
    }

    public void initDKernel(int frontMapNum) {
//		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        this.dkernel = new double[frontMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for (int i = 0; i < frontMapNum; i++)
            for (int j = 0; j < outMapNum; j++)
                dkernel[i][j] = randomMatrix(kernelSize.x, kernelSize.y, true);
    }



    /**
     * @param frontMapNum
     * @param size
     */
    public void initOutputKerkel(int frontMapNum, Size size) {
        kernelSize = size;
//		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        // 卷积核的个数是 前一层的特征 map 数 与 本层输出特征map 的个数的乘积
        this.kernel = new double[frontMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for (int i = 0; i < frontMapNum; i++)    // 遍历前一层的特征map
            for (int j = 0; j < outMapNum; j++)  // 遍历本层的输出map
                kernel[i][j] = randomMatrix(kernelSize.x, kernelSize.y,false);
    }
    public void initOutputDKerkel(int frontMapNum, Size size) {
        kernelSize = size;
//		int fan_out = getOutMapNum() * kernelSize.x * kernelSize.y;
//		int fan_in = frontMapNum * kernelSize.x * kernelSize.y;
//		double factor = 2 * Math.sqrt(6 / (fan_in + fan_out));
        // 卷积核的个数是 前一层的特征 map 数 与 本层输出特征map 的个数的乘积
        this.dkernel = new double[frontMapNum][outMapNum][kernelSize.x][kernelSize.y];
        for (int i = 0; i < frontMapNum; i++)    // 遍历前一层的特征map
            for (int j = 0; j < outMapNum; j++)  // 遍历本层的输出map
                dkernel[i][j] = randomMatrix(kernelSize.x, kernelSize.y,false);
    }

    /**
     *
     * @param frontMapNum
     */
    public void initBias(int frontMapNum) {
        this.bias = randomArray(outMapNum);
    }

    public void initDBias(int frontMapNum) {
        this.dbias = randomArray(outMapNum);
    }

    /**
     *
     *
     * @param batchSize
     */
    public void initOutmaps(int batchSize) {
        outmaps = new double[batchSize][outMapNum][mapSize.x][mapSize.y];
    }

    /**
     *
     *
     * @param mapNo
     *
     * @param mapX
     *
     * @param mapY
     *
     * @param value
     */
    public void setMapValue(int mapNo, int mapX, int mapY, double value) {
        outmaps[recordInBatch][mapNo][mapX][mapY] = value;
    }

    static int count = 0;

    /**
     *
     *
     * @param mapNo
     * @param outMatrix
     */
    public void setMapValue(int mapNo, double[][] outMatrix) {
        // Log.i(type.toString());
        // Util.printMatrix(outMatrix);
        outmaps[recordInBatch][mapNo] = outMatrix;
    }

    /**
     *
     *
     * @param index
     * @return
     */
    public double[][] getMap(int index) {
        return outmaps[recordInBatch][index];
    }

    /**
     *
     * @param i
     *
     * @param j
     *
     * @return
     */
    public double[][] getKernel(int i, int j) {
        return kernel[i][j];
    }


    public double[][] getDKernel(int i,int j) {
        return kernel[i][j];
    }

    /**
     *
     *
     * @param mapNo
     * @param mapX
     * @param mapY
     * @param value
     */
    public void setError(int mapNo, int mapX, int mapY, double value) {
        errors[recordInBatch][mapNo][mapX][mapY] = value;
    }

    /**
     *
     *
     * @param mapNo
     * @param matrix
     */
    public void setError(int mapNo, double[][] matrix) {
        // Log.i(type.toString());
        // Util.printMatrix(matrix);
        // recordInBatch 是在一个批次中的序数
        errors[recordInBatch][mapNo] = matrix;
    }

    /**
     *
     *
     * @param mapNo
     * @return
     */
    public double[][] getError(int mapNo) {
        return errors[recordInBatch][mapNo];
    }

    /**
     *
     *
     * @return
     */
    public double[][][][] getErrors() {
        return errors;
    }

    /**
     *
     * @param batchSize
     */
    public void initErros(int batchSize) {
        errors = new double[batchSize][outMapNum][mapSize.x][mapSize.y];
    }

    /**
     *
     * @param lastMapNo
     * @param mapNo
     * @param kernel
     */
    public void setKernel(int lastMapNo, int mapNo, double[][] kernel) {
        this.kernel[lastMapNo][mapNo] = kernel;
    }

    public void setDkernel(int lastMapNo, int mapNo, double[][] dkernel) {
        this.kernel[lastMapNo][mapNo] = dkernel;
    }

    /**
     *
     * @param mapNo
     * @return
     */
    public double getBias(int mapNo) {
        return bias[mapNo];
    }

    public double getDBias(int mapNo) {
        return dbias[mapNo];
    }

    /**
     *
     *
     * @param mapNo
     * @param value
     */
    public void setBias(int mapNo, double value) {
        bias[mapNo] = value;
    }

    public void setDbias(int mapNo,double value) {
        bias[mapNo] = value;
    }

    /**
     *
     *
     * @return
     */

    public double[][][][] getMaps() {
        return outmaps;
    }

    /**
     *
     *
     * @param recordId
     * @param mapNo
     * @return
     */
    public double[][] getError(int recordId, int mapNo) {
        return errors[recordId][mapNo];
    }

    /**
     *
     *
     * @param recordId
     * @param mapNo
     * @return
     */
    public double[][] getMap(int recordId, int mapNo) {
        return outmaps[recordId][mapNo];
    }

    /**
     *
     *
     * @return
     */
    public int getClassNum() {
        return classNum;
    }

    /**
     *
     *
     * @return
     */
    public double[][][][] getKernel() {
        return kernel;
    }

    public double [][][][] getDKernel() {
        return dkernel;
    }



    /**======================== hidden layer ===========================================================*/


    /**
     * @paran size
     *
     * */
    public static Layer buildHiddenLayer(Size hidSize) {

        Layer layer = new Layer();
        layer.type = LayerType.hidden;
        layer.wSize = hidSize;
        layer.hidoutNum = hidSize.y;

        return layer;
    }

    public  void initHiddenLayerWeight(Size hidSize) {

        this.wmat = randomMatrix(hidSize.x,hidSize.y);
//        this.bias = randomArray(hidSize.y);
    }

    public  void initHiddenLayerDWeight(Size hidSize) {

        this.dw = randomMatrix(hidSize.x,hidSize.y);
//        this.bias = randomArray(hidSize.y);
    }

    public  void initDBarr(int nout) {
        this.db = randomArray(nout);
    }





    public double[][] getWmat() {
        return wmat;
    }

    public void setWmat(double[][] wmat) {
        this.wmat = wmat;
    }

    public double[] getBarr() {
        return barr;
    }

    public void setBarr(double[] barr) {
        this.barr = barr;
    }

    public double[] getAout() {
        return aout[recordInBatch];
    }

    public double[] getAout(int recordId) {
        return aout[recordId];
    }

    public double[][] getAouts() {
        return aout;
    }

    public void setAout(double[] aout) {
        this.aout[recordInBatch] = aout;
    }

    public double[] getErr() {
        return err[recordInBatch];
    }

    public double[] getErr(int recordId){
        return err[recordId];
    }

    public double[][] getErrs(){
        return err;
    }

    public void initErrs(int batchSize) {
        err= new double[batchSize][];
    }

    public void setErr(double[] err) {
        this.err[recordInBatch] = err;
    }

    public double[][] getDw() {
        return dw;
    }

    public void setDw(double[][] dw) {
        this.dw = dw;
    }

    public double[] getDb() {
        return db;
    }

    public void setDb(double[] db) {
        this.db = db;
    }

    public Size getwSize() {
        return wSize;
    }

    public void setwSize(Size wSize) {
        this.wSize = wSize;
    }

    public int getHidoutNum() {
        return hidoutNum;
    }

    public void setHidoutNum(int hidoutNum) {
        this.hidoutNum = hidoutNum;
    }


    public  void initAout(int batchSize) {
        this.aout = new double[batchSize][this.wSize.y];
    }

    public void initBarr(int nout) {
        this.barr = randomArray(nout);
    }

    public void initErr(int batchSize) {
        this.err = new double[batchSize][this.wSize.y];
    }



    /*  全链接层**/
    public static Layer buildNormalInputLayer(int outputMapNum) {
        Layer layer = new Layer();
        layer.type = LayerType.normalInput;
        return layer;
    }



    /** softmax 层*/
    public static Layer buildSoftmaxLayer(Size softmaxSize) {
        Layer layer = new Layer();
        layer.type = LayerType.softmax;
        layer.wSize = softmaxSize;
        layer.wmat = randomMatrix(softmaxSize.x, softmaxSize.y);

        return layer;
    }

    public  void initSoftmaxLayerWeight(Size SoftMaxSize) {
        this.wmat = randomMatrix(SoftMaxSize.x, SoftMaxSize.y);
        this.bias = randomArray(SoftMaxSize.y);
    }




    /**
     * 还有sigmoid linear层没有实现
     *
     * */


}
