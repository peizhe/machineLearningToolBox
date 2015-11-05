package com.ireader.ml;

import java.io.*;
import java.util.*;

/**
 * Created by zxsted on 15-9-20.
 */
public class Matrix {

    /** ============== TODO list ================================================================== */

    /**
     *  交换两列
     *
     * @param mat
     * @param i
     * @param j
     */
    public static void exchangeCol(double[][] mat,int i,int j) {

        double[] temp = new double[mat.length];

        for (int ix = 0 ; ix < mat.length; ix++ ) {
            temp[ix] = mat[ix][i];
        }

        for (int ix = 0 ; ix < mat.length; ix++ ) {
            mat[ix][i] = mat[ix][j];
        }

        for (int ix = 0; ix < mat.length; ix++) {
            mat[ix][j] = temp[ix];
        }
    }


    /**
     * 交换两行
     *
     * @param mat
     * @param i
     * @param j
     */
    public static void exchangeRow(double[][] mat,int i,int j) {

        double[] temp = new double[mat[0].length];

        for (int ix = 0 ; ix < mat[0].length; ix++) {
            temp[ix] = mat[i][ix];
        }

        for (int ix = 0 ; ix < mat[0].length; ix++) {
            mat[i][ix] = mat[j][ix];
        }

        mat[j] = temp;
    }

    /**
     * 设置行
     *
     * @param mat
     * @param ridx
     * @param row
     */
    public static void setRow(double[][] mat,int ridx,double[] row) {

        if (row.length != mat[0].length)
            throw new RuntimeException("向量长度与矩阵的列数不同!");

        for (int i = 0 ; i < mat[0].length;i++) {
            mat[ridx][i] = row[i];
        }
    }

    /**
     * 设置列
     *
     * @param mat
     * @param cidx
     * @param vec
     */
    public static void setCol(double[][] mat,int cidx,double[] vec) {

        if (vec.length != mat[0].length)
            throw  new RuntimeException("向量长度与矩阵的行数不同！");

        for (int i = 0 ; i < vec.length; i++) {
            mat[i][cidx] = vec[i];
        }

    }

    /**左乘向量
     *
     * @param vec
     * @param mat
     * @return
     */
    public static double[] dot(double[] vec,double[][] mat) {

        if (vec.length != mat.length)
            throw new RuntimeException("向量的长度与矩阵的行数不同！");

        double[][] vecMat = new double[0][];

        vecMat[0] = vec;

        double[][] retMat = dot(vecMat,mat);

        return retMat[0];
    }

    /**右乘向量
     *
     * @param mat
     * @param vec
     * @return
     */
    public static double[] dot(double[][] mat,double[] vec) {

        if (vec.length != mat[0].length )
            throw new RuntimeException("矩阵的列数与向量的长度不同！");

        double[][] vecMat = new double[1][];

        vecMat[0] = vec;

        double[][] retMat = dot(mat,T(vecMat));

        return T(retMat)[0];

    }


    /** ============================================================================================ */

    /**从指定文件中加载 数据
     *
     * @param filename
     * @param xMat
     * @param yArr
     * @param start
     */
    public static void loadData(String filename ,double[][] xMat,double[] yArr,int start) {

        BufferedReader br = null;
        FileInputStream fin= null;

        String line = null;

        int count = 0;

        int length = xMat.length;

        try {
            fin = new FileInputStream(filename);
            br = new BufferedReader(new InputStreamReader(fin));

            while((line  = br.readLine()) != null ) {
                if ( count >=start && count < start+length) {
                    String[] fields = line.split(" ");
                    xMat[count - start]= new double[fields.length - 1];
                    for(int i = 1 ;i < fields.length; i++ )
                        xMat[count - start][i-1] = Double.parseDouble(fields[i]);
                    yArr[count - start] = Double.parseDouble(fields[0]);
                }
                count++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) fin.close();
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *   根据指定的 起始索引位置 idx 和指定的长度 ，从 xMat 中提取 一个批次
     *
     * @param xMat          数据矩阵
     * @param idx           提取的起始索引位置
     * @param batchsize     提取的长度
     * @return
     */
    public static double[][] getxBatch(double[][] xMat,  int batchsize ,int idx) {

        int m = xMat.length;
        int batchNum = xMat.length / batchsize;

        double[][] xbatch = slice(xMat,idx *batchsize,(idx + 1) * batchsize,0 );

        return xbatch;
    }


    /**
     *   根据指定的 起始索引位置 idx 和指定的长度 ，从 yArr 中提取 一个批次
     *
     * @param yArr          数据矩阵
     * @param idx           提取的起始索引位置
     * @param batchsize     提取的长度
     * @return
     */
    public static double[] getyBatch(double[] yArr ,  int batchsize ,int idx) {

        double[] ybatch = new double[batchsize];

        for (int i = 0 ; i < batchsize; i++) {
            ybatch[i] = yArr[idx*batchsize+i];
        }

        return ybatch;
    }


    /** ===================== 随机数工具 ============================================================ */

    private static Random r = new Random(2);



    public static double uniform(double min,double max) {
        return r.nextDouble() / (max - min) + min;
    }


    /**
     *  从 0 ～ n 中随机选择一个整数
     *  概率阈值是 p
     *
     *  用途： 在RBM 中进行采样时， 使用。 如：
     *      sample[i] = binomial(1, mean[i]);
     * */
    public static int  binomial(int n,double p) {

        if (p < 0 || p > 1) return 0;

        int c = 0;
        double rval;

        for (int i = 0; i < n; i++) {
            rval = r.nextDouble() ;
            if (rval < p) c++;
        }

        return c;
    }

    public static enum RandomType {
        Uniform,Gaussian
    }


    /**
     *  随机初始化权重，使各个权重非负，且和为1
     * @param len
     * @param precision
     * */
    public static double[]  RandomNormArr(int len,double precision) {

        double[] arr = new double[len];

        int[] position = new int[len];

        for (int i = 0 ; i < len ; i++) {
            position[i] = (int) (Math.random() * precision);
        }

        Arrays.sort(position);

        int pre = 0;

        for (int i = 0 ; i < len; i++) {
            arr[i] = 1.0 * (position[i] - pre) / precision;
            pre = position[i];
        }

        arr[len] = 1.0 * (precision - pre) / precision;

        return  arr;
    }

    /**
     *  均等的初始化权重， 使得各个权重非负， 且和为1
     * */
    public double[] EquallyNormArr(int len) {
        double[] arr = new double[len];
        for (int i = 0 ; i < len; i++) {
            arr[i] = 1.0 / len;
        }
        return arr;
    }


    /** 产生一个指定范围的随机数  */
    public static double rangeRand(double low,double high) {
        return r.nextDouble() * (high - low) + low;
    }

    /** 产生一个高斯分布值 */
    public static double gaussRand(double miu,double sigma) {

        return r.nextGaussian() *sigma + miu;
    }



    /**
     *  产生一个指定scale范围 的矩阵
     * */
    public static double[][] uniformMat(int rownum,int colnum,double scale) {

        double[][] retMat = new double[rownum][colnum];

        for (int i = 0 ; i < rownum; i++)
            for (int j = 0 ; j < colnum; j ++)
                retMat[i][j] = rangeRand(-scale,scale);

        return retMat;
    }

    /**
     *  产生一个指定的gauss 分布的矩阵
     * */
    public static double[][] gaussianMat(int rownum,int colnum,double miu,double sigma) {

        double[][] retMat = new double[rownum][colnum];

        for (int i = 0 ; i < rownum; i++)
            for (int j = 0 ; j < colnum; j++)
                retMat[i][j] = gaussRand(miu, sigma);

        return retMat;
    }

    /**
     *  根据一个概率矩阵来采样  一个 整数 矩阵 范围是 0 ～ num
     *  （RBM 中使用）
     *
     *  @param num : int  最大的 整数
     *  @param aMat: 用于采样的嘎律矩阵
     * */
    public static double[][] binomial(int num,double[][] aMat) {

        final int count  = num;

        return  matrixOp(aMat, new Operator() {
            @Override
            public double process(double value) {
                return binomial(count,value);
            }
        });
    }




    /**
     * 随机初始化矩阵
     *
     * @param x
     * @param y
     * @param bound
     * @param b
     * @return
     * */
    public static double[][] randomMatrix(int x,int y,double bound,boolean b) {
        double[][] matrix = new double[x][y];

        int tag = 1;

        for (int i = 0; i < x; i++) {
            for (int j = 0 ; j < y;j++) {
                // 随机值在 [-0.05,0.05] 之间， 让权重初始化值比较小比价，避免过拟合
                matrix[i][j] = (r.nextDouble() - bound) / 10;
            }
        }

        return matrix;
    }

    public static double[][] randomMatrix(int x,int y) {
        double[][] matrix = new double[x][y];

        int tag = 1;

        for (int i = 0; i < x; i++) {
            for (int j = 0 ; j < y;j++) {
                // 随机值在 [-0.05,0.05] 之间， 让权重初始化值比较小比价，避免过拟合
                matrix[i][j] = (r.nextDouble() -0.5) * Math.sqrt(6/(x+y));
            }
        }

        return matrix;
    }



    public static double[][] randomMatrix(int x,int y,boolean b) {
        return randomMatrix(x,y,0.05,b);
    }


    /**
     * 随机初始化一维向量
     * */
    public static double[] randomArray(int len) {
        double[] data = new double[len];

        for(int i = 0 ; i < len; i++) {
            data[i] = r.nextDouble() / 10  - 0.05;
        }
        return data;
    }

    /**
     *  初始化0向量
     * */
    public static double[] zeroArray(int len) {
        double[] data = new double[len];

        for(int i = 0 ; i < len; i++) {
            data[i] = 0;
        }
        return data;
    }

    /**
     *  随机排列的抽样， 随机抽取batchsize 个[0,size) 的数
     *
     * */
    public static int[] randomPerm(int size,int batchSize) {

        Set<Integer> set = new HashSet<Integer>();

        while (set.size() < batchSize) {
            set.add(r.nextInt(size));
        }

        int[] randPerm = new int[batchSize];
        int i = 0;
        for(Integer value: set)
            randPerm[i++] = value;
        return randPerm;
    }

    public static double[][] zeroMat(int rownum,int colnum) {

        double[][] retMat = new double[rownum][colnum];

        for (int i = 0 ; i < rownum; i++)
            for (int j = 0 ; j < colnum; j++)
                retMat[i][j] = 0;

        return retMat;
    }


    public static double[][] eyeMat(int labelnum) {
        double[][] retMat = new double[labelnum][labelnum];
        for(int row = 0 ; row < retMat.length; row++)
            for(int col = 0 ; col < retMat[0].length;col++)
                retMat[row][col]  = row == col?1:0;

        return retMat;
    }

    public static double[][] oneMat(int rownum,int colnum) {
        double[][] retMat = new double[rownum][colnum];
        for(int row = 0 ; row < retMat.length; row++)
            for(int col = 0 ; col < retMat[0].length;col++)
                retMat[row][col]  = 1.0;

        return retMat;
    }

    public static double[][] diag(double[] diag) {
        double[][] retMat = new double[diag.length][diag.length];
        for(int row = 0 ; row < retMat.length; row++)
            for(int col = 0 ; col < retMat[0].length;col++)
                retMat[row][col]  = row == col?diag[row]:0;

        return retMat;
    }

    public static double[][] fill(double val,int rownum,int colnum) {

        double[][] retMat = new double[rownum][colnum];

        for (int i = 0 ; i < rownum; i++)
            for (int j = 0 ; j < colnum; j++)
                retMat[i][j] = val;

        return retMat;
    }

    /** ============================================================================================ */


    /**
     *  打印矩阵
     * */
    public static void printMatrix(double[][] matrix) {
        for(int i = 0 ; i < matrix.length; i++) {
            String line  = Arrays.toString(matrix[i]);
            line = line.replaceAll(", ","\t");
            System.out.println(line);
        }
        System.out.println();

    }

    /**
     *  打印数组
     *
     *  @param arr: 数组
     * */
    public static void printArray(double[] arr) {

        System.out.println(Arrays.toString(arr));
    }




    public static int[] size(double[][] matrix) {
        int[] retArr = new int[2];
        retArr[0] = matrix.length;
        retArr[1] = matrix[0].length;

        return retArr;
    }

    public static void printSize(double[][] matrix) {
        System.out.println("矩阵的size为："+ Arrays.toString(size(matrix)));
    }


    //

    /**
     *  卷积核或者采样层的scale 的大小， 长和宽可以不相等， 类型安全 定义后不可以修改
     *
     * */
    public static class Size implements Serializable {

        private static final long serialVersionUID = -209157832162004118L;

        public final int x;
        public final int y;

        public Size(int x,int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("Size(").append(" x= ")
                    .append(x).append(" y= ").append(y).append(")");
            return s.toString();
        }

        /**
         *  整除scalesize 得到一个新的size， 要求this.x,this.y 可以分别被 scaleSize.x scaleSize.y  整除
         *
         * */
        public Size divide(Size scaleSize) {
            int x = this.x / scaleSize.x;
            int y = this.y / scaleSize.y;

            if(x * scaleSize.x != this.x || y * scaleSize.y != this.y)
                throw new RuntimeException(this + " 不能整除 "  + scaleSize);
            return new Size(x,y);
        }

        /**
         *  减去 size 大小， 并x和y分别附加一个值append
         *
         * */
        public Size substract(Size size,int append) {
            int x = this.x - size.x + append;
            int y = this.y - size.y + append;
            return new Size(x,y);
        }

    }

    /** ============== 时间度量工具 ========================================================================== */

    public interface TestTask {
        public void process();
    }

    /**
     *  时间测试包装类
     * */
    public class TimedTest {

        private int repeat;
        private TestTask task;

        public TimedTest(TestTask t,int repeat) {
            this.repeat = repeat;
            task = t;
        }


        public void test() {
            long t = System.currentTimeMillis();
            for(int i = 0 ; i < repeat; i++) {
                task.process();
            }

            double costTime = (System.currentTimeMillis() - t) / 1000.0;

            System.out.println("Cost " + costTime + "s");
        }

    }




    /** ====================================================================================================== */
    /** ============================== 矩阵包装类 ============================================================= */
    /** ====================================================================================================== */


    /**
     *  矩阵对应元素相乘时，在每个元素上的操作
     * */
    public interface Operator extends Serializable {
        public double process(double value);
    }

    /**
     *  定义每个元素value 都进行的1-value 的操作
     * */
    public static final Operator one_value = new Operator(){

        @Override
        public double process(double value) {
            return 1 - value;
        }
    };

    /**
     *  sigmod 函数
     * */
    public static final Operator sigmod = new Operator() {

        @Override
        public double process(double value) {
            return 1 / (1 + Math.pow(Math.E,-value));
        }
    };

    public static final Operator exp = new Operator() {
        @Override
        public double process(double value) {
            return Math.pow(Math.E,value);
        }
    };


    /**
     *  两个元素的操作
     * */
    public interface OperatorOnTwo extends Serializable {
        public double process(double a,double b);
    }

    /**
     *  定义矩阵对应元素的加法操作
     * */
    public static final OperatorOnTwo plus = new OperatorOnTwo(){

        @Override
        public double process(double a,double b) {
            return a+b;
        }
    };

    /**
     *  定义矩阵 对应元素的乘法操作
     * */
    public static OperatorOnTwo multiply = new OperatorOnTwo() {

        @Override
        public double process(double a,double b){
            return a * b;
        }
    };


    /**
     *  定义矩阵对应元素的减法操作
     * */
    public static OperatorOnTwo minus = new OperatorOnTwo() {

        @Override
        public double process(double a, double b) {
            return a - b;
        }
    };


    /**
     * 对单个矩阵进行操作
     *
     * @param ma
     * @param operator
     * @return
     */
    public static double[][] matrixOp(final double[][] ma, Operator operator) {
        final int m = ma.length;
        int n = ma[0].length;

        double[][] retMat = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                retMat[i][j] = operator.process(ma[i][j]);
            }
        }
        return retMat;

    }

    /**
     * 两个维度相同的矩阵对应元素操作,得到的结果方法mb中，即mb[i][j] = (op_a
     * ma[i][j]) op (op_b mb[i][j])
     *
     * @param ma
     * @param mb
     * @param operatorB
     *            在第mb矩阵上的操作
     * @param operatorA
     *            在ma矩阵元素上的操作
     * @return
     *
     */
    public static double[][] matrixOp(final double[][] ma, final double[][] mb,
                                      final Operator operatorA, final Operator operatorB,
                                      OperatorOnTwo operator) {
        final int m = ma.length;
        int n = ma[0].length;

        double[][] retMat = new double[m][n];
        if (m != mb.length || n != mb[0].length)
            throw new RuntimeException("两个矩阵大小不一致 ma.length:" + ma.length
                    + "  mb.length:" + mb.length);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double a = ma[i][j];
                if (operatorA != null)
                    a = operatorA.process(a);
                double b = mb[i][j];
                if (operatorB != null)
                    b = operatorB.process(b);
                retMat[i][j] = operator.process(a, b);
            }
        }
        return retMat;
    }

    public static double[][] add(double[][] aMat,double[][] bMat) {
        return matrixOp(aMat,bMat,null,null,plus);
    }

    public static double[][] sub(double[][] aMat,double[][] bMat) {
        return matrixOp(aMat,bMat,null,null,minus);
    }

    public static double[][] mul(double[][] aMat,double[][] bMat) {
        return matrixOp(aMat,bMat,null,null,multiply);
    }

    public static double[][] mul(double[][] aMat,final double lr) {
        return matrixOp(aMat, new Operator() {
            @Override
            public double process(double value) {
                return lr * value;
            }
        });
    }


    /**
     *  softmax 计算函数
     *
     * @param oMat double[][] :  线性输出结果矩阵
     * */
    public static double[][] softmax(double[][] oMat )  {

        double[][] retMat = null;

        final double maxval = max(oMat);

        double[][] adjustMat = matrixOp(oMat, new Operator() {
            @Override
            public double process(double value) {
                return value - maxval;
            }
        });

        double[][] eMat = matrixOp(adjustMat, exp);

        double[] rowsum = dimsum(eMat, 0);

        retMat = edivid(eMat, rowsum, 0);

        return retMat;
    }






   /** ================================================================================================ */



    /**
     * 复制矩阵
     *
     * @param matrix
     * @return
     */
    public static double[][] cloneMatrix(final double[][] matrix) {

        final int m = matrix.length;
        int n = matrix[0].length;
        final double[][] outMatrix = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                outMatrix[i][j] = matrix[i][j];
            }
        }
        return outMatrix;
    }

    /**
     *  根据一个label数组 ，扩展出softmax的输出矩阵
     * */
    public static double[][] label4softmaxout(int labelnum,double[] labelArr) {

        double[][] retMat = new double[labelArr.length][labelnum];

        double[][] eye = eyeMat(labelnum);

        for (int i = 0 ; i < labelArr.length; i++) {
            for (int j = 0 ; j < labelnum; j++) {
                int label = (int) Math.floor(labelArr[i]);
                retMat[i][j] = eye[label][j];
            }
        }

        return retMat;
    }

    /**
     * 对矩阵进行180度旋转,是在matrix的副本上复制，不会对原来的矩阵进行修改
     *
     * @param matrix
     */
    public static double[][] rot180(double[][] matrix) {
        matrix = cloneMatrix(matrix);
        int m = matrix.length;
        int n = matrix[0].length;
        // 按列对称进行交换
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n / 2; j++) {
                double tmp = matrix[i][j];
                matrix[i][j] = matrix[i][n - 1 - j];
                matrix[i][n - 1 - j] = tmp;
            }
        }
        // 按行对称进行交换
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m / 2; i++) {
                double tmp = matrix[i][j];
                matrix[i][j] = matrix[m - 1 - i][j];
                matrix[m - 1 - i][j] = tmp;
            }
        }
        return matrix;
    }

    /**
     * 矩阵转置
     * */
    public static double[][] T(double[][] matrix){
        double[][] tmatrix = new double[matrix[0].length][matrix.length];

        for(int row = 0 ; row < matrix.length;row++)
            for(int col = 0 ; col < matrix[0].length;col++)
                tmatrix[col][row] = matrix[row][col];

        return tmatrix;
    }

    /**
     * 将权重矩阵转化为 一维数组
     *
     * */
    public static double[] reval(double[][] wMat) {

        double[] retArray = new double[wMat.length * wMat[0].length];

        for (int l = 0 ; l < wMat.length; l++) {
            for(int f = 0 ; f < wMat[0].length;f++) {
                retArray[l*wMat[0].length + f] = wMat[l][f];
            }
        }

        return retArray;
    }

    /**
     * 将一维数组转换为权重矩阵
     * */
    public static double[][] reSize(double[] wArr,int lnum,int fnum)  {

        double[][] retMat = new double[lnum][fnum];
        if(lnum*fnum != wArr.length)
            throw new RuntimeException("resize 的维度 与数组长度不相匹配 数组长度为："  +wArr.length + "resize 的维度：(" + lnum+","+fnum+")");

        for (int l = 0 ; l < lnum ; l++)
            for (int f = 0 ; f < fnum; f++)
                retMat[l][f] = wArr[l*fnum + f];
        return retMat;
    }

    /**
     * 将一列 matrix 展平
     * */
    public static double[] reval(List<double[][]> matList){

        int num = 0;
        for(int i = 0 ; i < matList.size();i++) {
            num += matList.get(i).length * matList.get(i)[0].length;
        }

        double[] retArr = new double[num];

        int subBegin = 0;
        for(int i = 0 ; i < matList.size(); i++) {
            if(i>0) subBegin += matList.get(i-1).length * matList.get(i-1)[0].length;
            double[] curArr = reval(matList.get(i));
            for(int j = 0; j < curArr.length;j++)
                retArr[subBegin+j] = curArr[j];
        }
        return retArr;
    }

    /**
     * 从一个一维数组 重构出 一个神经网络参数矩阵列表
     *
     *  如列表为：
     *  [3,4,5,4,6]
     *  则各个矩阵的size为：
     *  (4，3+1),(5，4+1),(4，5+1),(6，4+1)
     * */
    public static List<double[][]> resize(double[] wArr,List<Integer> dimList,boolean bias) {

        List<double[][]> retMatList = new ArrayList<double[][]>();
        int curBegin = 0;
        for(int i = 0 ; i < dimList.size() - 1; i++) {
            int fnum = dimList.get(i);
            int lnum = dimList.get(i+1);

            fnum = bias?fnum+1:fnum;   // 矩阵是否有偏置

            int length = fnum * lnum;  // 注意 +1 是偏置

            double[][] curWMat = null;

            for(int j = 0 ; j < length; j++) {
                double[] subArr = subArray(wArr,curBegin,length);
                curWMat = reSize(subArr,lnum,fnum);

            }
            retMatList.add(curWMat);

            curBegin += length;
        }

        return retMatList;
    }

    /**
     *  从一个数组中提取子数组
     * */
    public static double[] subArray(double[] wArr,int begin,int length) {

        double[] retArray = new double[length];
        for(int i = 0 ; i < length;i++){
            retArray[i] = wArr[begin+i];
        }

        return retArray;
    }



    public static double[][] vstack(double[][] leftMat,double[][] rightMat)  {

        int leftRowNum = leftMat.length;
        int leftColNum = leftMat[0].length;

        int rightRowNum = rightMat.length;
        int rightColNum = rightMat[0].length;

        if(leftRowNum != rightRowNum) throw new RuntimeException("stack 的两个矩阵 的行数不相等");

        int newcolnum = leftColNum + rightColNum;

        double[][] retMat = new double[leftRowNum][newcolnum];

        for (int i = 0 ; i < leftRowNum; i++) {

            for (int j = 0; j < leftColNum; j++) {
                retMat[i][j] = leftMat[i][j];
            }

            for (int j = 0 ; j < rightColNum; j++) {
                retMat[i][leftColNum +j] = rightMat[i][j];
            }
        }

        return retMat;
    }

    public static double[][] hstack(double[][] upMat,double[][] downMat)  {

        int upRowNum = upMat.length;
        int upColNum = downMat[0].length;

        int downRowNum = downMat.length;
        int downColNum = downMat[0].length;

        if(upColNum != downColNum) throw new RuntimeException("stack 的两个矩阵 的行数不相等");

        int newRowNum = upRowNum + downRowNum;

        double[][] retMat = new double[newRowNum][upColNum];

        for (int j = 0 ; j < upColNum; j++) {

            for (int i = 0; i < upRowNum; i++) {
                retMat[i][j] = upMat[i][j];
            }

            for (int i = 0 ; i < downRowNum; i++) {
                retMat[upRowNum+i][j] = downMat[i][j];
            }
        }

        return retMat;
    }


    /**
     * 矩阵切块
     *
     *    按照 行和列的范围进行切块
     * */
    public static double[][] slice(double[][] mat,int rstart,int rend,int cstart,int cend) {

        double[][] retMat = new double[rend - rstart ][ cend - cstart ];

        for (int i = rstart; i < rend; i++)
            for (int j = cstart ; j < cend; j++)
                retMat[i - rstart][j - cstart] = mat[i][j];

        return retMat;
    }

    public static double[][] slice(double[][] mat ,int start,int end,int direction) {

        double[][] retMat = null;

        if (direction == 0) retMat = slice(mat,start,end,0,mat[0].length);
        else if (direction == 1) retMat = slice(mat,0,mat.length,start,end);

        return retMat;
    }


    /**
     *  使用坐标列表（数组表示的）进行搜索
     * */
    public static double[][] iloc(double[][] mat,int[] rlist,int[] clist) {

        double[][] retMat = new double[rlist.length][clist.length];

        for (int i = 0 ; i < rlist.length; i++)
            for (int j = 0; j < clist.length; j++)
                retMat[i][j] = mat[rlist[i]][clist[j]];

        return retMat;
    }

    public static double[][] iloc(double[][] mat,int[] list,int direction) {

        double[][] retMat = null;

        if (direction == 0) {    // 选择 row

            int[] olist = new int[mat[0].length];
            for (int i = 0 ; i < mat[0].length; i++)
                olist[i] = i;
            retMat = iloc(mat,list,olist);

        } else if (direction == 1) {

            int[] olist = new int[mat.length];
            for (int i = 0 ; i < mat.length; i++)
                olist[i] = i;
            retMat = iloc(mat,olist,list);
        }

        return retMat;
    }


    /**=====================================================================================================*/


    /**
     *  Vector inner product
     * */
    public static double dot(double[] vec1, double[] vec2)  {

        double retVal = 0.0;

        if (vec1.length != vec2.length)
            throw new RuntimeException("进行内积计算的两个向量长度不一致， vec1 length:"+ vec1.length + " vec2 length :" + vec2.length);

        for(int i = 0 ; i < vec1.length ; i++) {
            retVal += vec1[i] * vec2[i];
        }

        return retVal;
    }

    /**
     * Matrix inner product
     * */
    public static double[][] dot(double[][] mat1, double[][] mat2)  {


        double[][] retMat = new double[mat1.length][mat2[0].length];

        if(mat1[0].length != mat2.length)
            throw new RuntimeException("进行内积计算的两个矩阵的 列 行不相等， mat1 的列个数为" + mat1[0].length +
                    " mat2 的行数为：" + mat2.length);

        double[][] tMat2 = T(mat2);  // colnum * rownum
        for(int row = 0 ; row < mat1.length;row++)
            for(int col = 0 ; col < mat2[0].length;col++)
                retMat[row][col] = dot(mat1[row], tMat2[col]);

        return retMat;
    }

    /**
     * 为矩阵添加 1 bias
     *
     * xMat: size m,n
     * */
    public static double[][] addBias(double[][] xMat) {
        double[][] retMat = new double[xMat.length][xMat[0].length+1];

        for(int row = 0 ; row < xMat.length;row++) {
            for(int col = 0 ; col < xMat[0].length;col++)
                retMat[row][col] = xMat[row][col];
            retMat[row][xMat.length] = 1.0;
        }

        return retMat;
    }

    /**
     * 去除偏置
     * xMat : size m,(n+1)
     * */
    public static double[][] removeBias(double[][] xMat) {

        double[][] retMat = new double[xMat.length][xMat[0].length-1];

        for(int row = 0 ; row < xMat.length; row++)
            for(int col = 0 ; col < xMat[0].length-1; col++)
                retMat[row][col] = xMat[row][col];

        return retMat;
    }

    /**
     *  求最大元素
     * */
    public static double max(double[][] mat) {

        int m = mat.length;
        int n = mat[0].length;

        double maxval = Double.MIN_VALUE;

        for (int i = 0 ; i<m; i++)
            for (int j = 0 ; j < n; j++)
                if (maxval < mat[i][j]) maxval = mat[i][j];

        return maxval;
    }


    /**
     * 对矩阵元素求和
     *
     * @param error
     * @return 注意这个求和很可能会溢出
     */

    public static double sum(double[][] error) {
        int m = error.length;
        int n = error[0].length;
        double sum = 0.0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum += error[i][j];
            }
        }
        return sum;
    }

    /**
     * 对errors[...][j]元素求和
     *
     * @param errors
     * @param j
     * @return
     */
    public static double[][] sum(double[][][][] errors, int j) {
        int m = errors[0][j].length;
        int n = errors[0][j][0].length;
        double[][] result = new double[m][n];
        for (int mi = 0; mi < m; mi++) {
            for (int nj = 0; nj < n; nj++) {
                double sum = 0;
                for (int i = 0; i < errors.length; i++)
                    sum += errors[i][j][mi][nj];
                result[mi][nj] = sum;
            }
        }
        return result;
    }




    /**
     * 矩阵按照纬度求和
     *
     *  dim = 0: 加 行向量 : 行方向操作 ， 列要与向量长度相同
     *  dim = 1: 加 列向量 : 列方向操作 ， 行要与向量长度相同
     * */
    public static double[] dimsum(double[][] matrix,int dim) {

        int m = matrix.length;
        int n = matrix[0].length;

        double[] retArr = null;

        if (dim == 1) {
            retArr = new double[n];

            for(int i = 0 ; i < m; i++)
                for (int j = 0 ; j < n; j++)
                    retArr[j] += matrix[i][j];

        } else if ( dim == 0) {

            retArr = new double[m];

            for(int i = 0 ; i < m; i++)
                for(int j = 0 ; j < n; j++) {
                    retArr[i] += matrix[i][j];
                }
        }

        return retArr;
    }

    /**
     *  矩阵加 向量
     *
     *  dim = 0: 加 行向量 : 行方向广播 ， 列要与向量长度相同
     *  dim = 1: 加 列向量 : 列方向广播 ， 行要与向量长度相同
     * */
    public static double[][] addVec(double[][] matrix,double[] vec, int dim) {

        int m = matrix.length;
        int n = matrix[0].length;

        double[][] retMat = new double[m][n];

        if(dim == 1) {
            if (n != vec.length)
                throw new RuntimeException("矩阵的行长度与向量的长度不同，无法广播！");

            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] = matrix[i][j] + vec[j];


        } else if (dim == 0) {
            if (m != vec.length)
                throw new RuntimeException("矩阵的列长度与向量的长度不同，无法广播！");
            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] +  vec[i];
        }

        return retMat;
    }


    /**
     *  矩阵减 向量
     *
     *  dim = 0: 减 行向量 : 行方向广播 ， 列要与向量长度相同
     *  dim = 1: 减 列向量 : 列方向广播 ， 行要与向量长度相同
     * */
    public static double[][] subVec(double[][] matrix,double[] vec, int dim)  {

        int m = matrix.length;
        int n = matrix[0].length;

        double[][] retMat = new double[m][n];

        if(dim == 1) {
            if (n != vec.length)
                throw new RuntimeException("矩阵的行长度与向量的长度不同，无法广播！");

            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] -  vec[j];


        } else if (dim == 0) {
            if (m != vec.length)
                throw new RuntimeException("矩阵的列长度与向量的长度不同，无法广播！");
            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] - vec[i];
        }

        return retMat;
    }

    /**
     *  矩阵 按元素乘 向量
     *
     *  dim = 0: 乘 行向量 : 行方向广播 ， 列要与向量长度相同
     *  dim = 1: 乘 列向量 : 列方向广播 ， 行要与向量长度相同
     * */
    public static double[][] emul(double[][] matrix,double[] vec ,int dim) {

        int m = matrix.length;
        int n = matrix[0].length;

        double[][] retMat = new double[m][n];

        if(dim == 1) {
            if (n != vec.length)
                throw new RuntimeException("矩阵的行长度与向量的长度不同，无法广播！");

            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] *  vec[j];


        } else if (dim == 0) {
            if (m != vec.length)
                throw new RuntimeException("矩阵的列长度与向量的长度不同，无法广播！");
            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] * vec[i];
        }

        return retMat;

    }

    /**
     *  矩阵 按元素除 向量
     *
     *  dim = 0: 除 行向量 : 行方向广播 ， 列要与向量长度相同
     *  dim = 1: 除 列向量 : 列方向广播 ， 行要与向量长度相同
     * */
    public static double[][] edivid(double[][] matrix,double[] vec ,int dim){

        int m = matrix.length;
        int n = matrix[0].length;

        double[][] retMat = new double[m][n];

        boolean zeroflag = false;

        for(int i = 0 ; i < vec.length; i++) {
            if(0.0 == vec[i]) zeroflag = true;
        }

        if (zeroflag) throw new RuntimeException("向量中含有零元素，无法除");

        if(dim == 1) {
            if (n != vec.length)
                throw new RuntimeException("矩阵的行长度与向量的长度不同，无法广播！");

            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] /  vec[j];


        } else if (dim == 0) {
            if (m != vec.length)
                throw new RuntimeException("矩阵的列长度与向量的长度不同，无法广播！");
            for ( int i = 0 ; i < m ; i++)
                for (int j = 0 ; j < n; j++)
                    retMat[i][j] =matrix[i][j] / vec[i];
        }

        return retMat;

    }


/**============================ 卷积神经网络工具 =========================================================================*/
    /**
     * 克罗内克积,对矩阵进行扩展
     *
     * @param matrix
     * @param scale
     * @return
     */
    public static double[][] kronecker(final double[][] matrix, final Size scale) {
        final int m = matrix.length;
        int n = matrix[0].length;
        final double[][] outMatrix = new double[m * scale.x][n * scale.y];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int ki = i * scale.x; ki < (i + 1) * scale.x; ki++) {
                    for (int kj = j * scale.y; kj < (j + 1) * scale.y; kj++) {
                        outMatrix[ki][kj] = matrix[i][j];
                    }
                }
            }
        }
        return outMatrix;
    }

    /**
     * 对矩阵进行均值缩小
     *
     * @param matrix
     * @param
     * @return
     */
    public static double[][] scaleMatrix(final double[][] matrix,
                                         final Size scale) {
        int m = matrix.length;
        int n = matrix[0].length;
        final int sm = m / scale.x;
        final int sn = n / scale.y;
        final double[][] outMatrix = new double[sm][sn];
        if (sm * scale.x != m || sn * scale.y != n)
            throw new RuntimeException("scale不能整除matrix");
        final int size = scale.x * scale.y;
        for (int i = 0; i < sm; i++) {
            for (int j = 0; j < sn; j++) {
                double sum = 0.0;
                for (int si = i * scale.x; si < (i + 1) * scale.x; si++) {
                    for (int sj = j * scale.y; sj < (j + 1) * scale.y; sj++) {
                        sum += matrix[si][sj];
                    }
                }
                outMatrix[i][j] = sum / size;
            }
        }
        return outMatrix;
    }


    /**
     * 计算full模式的卷积
     *
     * @param matrix
     * @param kernel
     * @return
     */
    public static double[][] convnFull(double[][] matrix,
                                       final double[][] kernel) {
        int m = matrix.length;
        int n = matrix[0].length;
        final int km = kernel.length;
        final int kn = kernel[0].length;
        // 扩展矩阵
        final double[][] extendMatrix = new double[m + 2 * (km - 1)][n + 2
                * (kn - 1)];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                extendMatrix[i + km - 1][j + kn - 1] = matrix[i][j];
        }
        return convnValid(extendMatrix, kernel);
    }




    /**
     * 计算valid模式的卷积
     *
     * @param matrix
     * @param kernel
     * @return
     */
    public static double[][] convnValid(final double[][] matrix,
                                        double[][] kernel) {
        //kernel = rot180(kernel);
        int m = matrix.length;
        int n = matrix[0].length;
        final int km = kernel.length;
        final int kn = kernel[0].length;
        // 需要做卷积的列数
        int kns = n - kn + 1;
        // 需要做卷积的行数
        final int kms = m - km + 1;
        // 结果矩阵
        final double[][] outMatrix = new double[kms][kns];

        for (int i = 0; i < kms; i++) {
            for (int j = 0; j < kns; j++) {
                double sum = 0.0;
                for (int ki = 0; ki < km; ki++) {
                    for (int kj = 0; kj < kn; kj++)
                        sum += matrix[i + ki][j + kj] * kernel[ki][kj];
                }
                outMatrix[i][j] = sum;

            }
        }
        return outMatrix;

    }

    /**
     * 三维矩阵的卷积,这里要求两个矩阵的一维相同
     *
     * @param matrix
     * @param kernel
     * @return
     */
    public static double[][] convnValid(final double[][][][] matrix,
                                        int mapNoX, double[][][][] kernel, int mapNoY) {
        int m = matrix.length;
        int n = matrix[0][mapNoX].length;
        int h = matrix[0][mapNoX][0].length;
        int km = kernel.length;
        int kn = kernel[0][mapNoY].length;
        int kh = kernel[0][mapNoY][0].length;
        int kms = m - km + 1;
        int kns = n - kn + 1;
        int khs = h - kh + 1;
        if (matrix.length != kernel.length)
            throw new RuntimeException("矩阵与卷积核在第一维上不同");
        // 结果矩阵
        final double[][][] outMatrix = new double[kms][kns][khs];
        for (int i = 0; i < kms; i++) {
            for (int j = 0; j < kns; j++)
                for (int k = 0; k < khs; k++) {
                    double sum = 0.0;
                    for (int ki = 0; ki < km; ki++) {
                        for (int kj = 0; kj < kn; kj++)
                            for (int kk = 0; kk < kh; kk++) {
                                sum += matrix[i + ki][mapNoX][j + kj][k + kk]
                                        * kernel[ki][mapNoY][kj][kk];
                            }
                    }
                    outMatrix[i][j][k] = sum;
                }
        }
        return outMatrix[0];
    }







    public static int binaryArray2int(double[] array) {
        int[] d = new int[array.length];

        for (int i = 0 ; i < d.length; i++) {
            if (array[i] >= 0.500000001)
                d[i] = 1;
            else
                d[i] = 0;
        }

        String s = Arrays.toString(d);
        String binary = s.substring(1,s.length()-1).replace(",","");
        int data = Integer.parseInt(binary,2);
        return data;
    }

    /**
     * 取最大的元素的下标
     *
     * @param out
     * @return
     */
    public static int getMaxIndex(double[] out) {
        double max = out[0];
        int index = 0;
        for (int i = 1; i < out.length; i++)
            if (out[i] > max) {
                max = out[i];
                index = i;
            }
        return index;
    }





    /** ============= test function ================================================== */
    /** ============================================================================== */

    /** 测试卷积 */
    private static void testConvn() {

        int count = 1;
        double[][] m = new double[5][5];

        for (int i = 0 ; i < m.length; i++)
            for (int j = 0 ; j < m[0].length; j++)
                m[i][j] = count++;

        double[][] k = new double[3][3];

        for (int i = 0 ; i < k.length; i++)
            for (int j = 0 ; j < k[0].length; j++)
                k[i][j] = 1;

        double[][] out ;

        System.out.println("==============================================================");
        System.out.println("测试 valid 卷积");
        Matrix.printMatrix(m);
//        out = convnFull(m,k);
        out = convnValid(m,k);
        Matrix.printMatrix(out);


        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 full 卷积");
        out = convnFull(m,rot180(k));
        Matrix.printMatrix(out);
    }

    /** 测试矩阵放缩 */
    private static void testScaleMatrix() {

        int count = 1;
        double[][] m = new double[16][16];

        for( int i = 0 ; i < m.length; i++)
            for (int j = 0 ; j < m[0].length; j++)
                m[i][j] = count++;

        double[][] out = scaleMatrix(m,new Size(2,2));

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 矩阵放缩");
        printMatrix(m);
        printMatrix(out);
    }

    /** 测试 克罗内科 卷积 */
    private static void testKronecker() {
        int count  = 1;
        double[][] m = new double[5][5];

        for (int i = 0; i < m.length; i++)
            for (int j = 1; j < m[0].length; j++)
                m[i][j] = count++;

        double[][] out = kronecker(m, new Size(2, 2));


        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 克罗内科 卷积");
        printMatrix(m);
        System.out.println();
        printMatrix(out);
    }

    private static void testMatrixProduct() {

        int count = 1;
        double[][] m = new double[5][5];
        for (int i = 0 ; i < m.length; i++)
            for (int j = 0 ; j < m[0].length; j++)
                m[i][j] = count++;

        double[][] k = new double[5][5];
        for (int i = 0 ; i < k.length; i++)
            for (int j = 0 ; j < k[0].length; j++)
                k[i][j] = j;

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 矩阵元素相乘");
        printMatrix(m);
        System.out.println();
        printMatrix(k);

        double[][] out = matrixOp(m, k, new Operator() {
            @Override
            public double process(double value) {
                return value - 1;
            }
        }, new Operator() {
            @Override
            public double process(double value) {
                return -1 * value;
            }
        },multiply);
        System.out.println();

        printMatrix(out);
    }

    /**  测试举着克隆 */
    private static void testCloneMatrix() {

        int count = 1;
        double[][] m = new double[5][5];

        for (int i = 0 ; i < m.length; i++)
            for (int j = 0 ; j < m[0].length;j++)
                m[i][j] = count++;

        double[][] out = cloneMatrix(m);

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 矩阵克隆");
        printMatrix(m);
        System.out.println();
        printMatrix(out);
    }

    /** 测试旋转 180  */
    public static void testRot180() {

        double[][] matrix = {
                {1,2,3,4},
                {5,6,7,8},
                {9,10,11,12}
        };

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 矩阵180度旋转");
        printMatrix(matrix);
        double[][] rot = rot180(matrix);
        System.out.println();
        printMatrix(rot);

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 矩阵转置");
        double[][] tr = T(matrix);
        System.out.println();
        printMatrix(tr);

    }

    public  static void ptestname(String type) {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("测试 " + type);
    }


    private static  void batchTest()  {

        int m = 4;
        int n = 4;
//
//        double[][] A = null;
//        double[][] B = null;
//        double[] arow = null;
//        double[] acol = null;
//
//        double[][] eye = null;
//
//        A = randomMatrix(m, n, true);
//        B = randomMatrix(m, n, true);
//        eye = eyeMat(m);
//        arow = randomArray(m);
//        acol = randomArray(n);
//
//        double[] zeroa = zeroArray(m);
//
//        int[] perm = randomPerm(m,n);
//
//        ptestname("初始化");
//
//        printMatrix(A);
//        System.out.println();
//        printMatrix(B);
//        System.out.println();
//        printMatrix(eye);
//
//        System.out.println();
//        System.out.println(Arrays.toString(arow));
//        System.out.println();
//        System.out.println(Arrays.toString(acol));
//
//        System.out.println();
//        System.out.println(Arrays.toString(zeroa));
//
//
//        ptestname("添加偏置");
//        double[][] abias = addBias(A);
//        printMatrix(abias);
//        ptestname("去除偏置");
//        double[][] RA = removeBias(abias);
//        printMatrix(RA);
//        ptestname("展平");
//        double[] flatArr = reval(A);
//        System.out.println(Arrays.toString(flatArr));
//        ptestname("数组转矩阵");
//        printMatrix(reSize(flatArr, 4, 4));
//        ptestname("矩阵sum");
//        System.out.println(sum(A));
//        ptestname("矩阵列sum");
//        System.out.println(Arrays.toString(dimsum(A, 1)));
//        ptestname("矩阵行sum");
//        System.out.println(Arrays.toString(dimsum(A, 0)));
//
//        double[] col = new double[m];
//        for(int i = 0 ; i < m ; i++)
//            col[i] = 1;
//        double[] row = new double[n];
//        for(int j = 0 ; j < n; j++)
//            row[j] = 1;
//
//        double[] factor = new double[m];
//        for(int j = 0 ; j < n; j++)
//            factor[j] = j;
//
//        ptestname("向量内积");
//        System.out.println(dot(col, col));
//
//        ptestname("矩阵内积");
//        printMatrix(A);
//        System.out.println();
//        printMatrix(dot(A, eye));
//        System.out.println();
//        printMatrix(dot(T(A), eye));
//
//        ptestname("矩阵加行");
//        printMatrix(addVec(A, factor, 1));
//        ptestname("矩阵加列");
//        printMatrix(addVec(A, factor, 0));
//
//        ptestname("矩阵减行");
//        printMatrix(subVec(A, factor, 1));
//        ptestname("矩阵减列");
//        printMatrix(subVec(A, factor, 0));

//        ptestname("softmax 输出构建");
//        Random rand = new Random(9);
//        double[] labels = new double[m];
//        for(int i = 0; i < m; i ++){
//            labels[i] = rand.nextInt(4);
//        }
//
//        System.out.println(Arrays.toString(labels));
//        double[][] softmaxMat = label4softmaxout(4,labels);
//        System.out.println();
//        printMatrix(softmaxMat);


//        double[][] eye = eyeMat(5);
//
//        double[] vec = new double[]{1.0,2.0,3.0,4.0,5.0};
//
////        eye = randomMatrix(5,5,true);
//
//        eye = oneMat(5);
//
//        double[] addMat =null;
//        ptestname("矩阵与向量相乘");
//        printMatrix(eye);
//
//        System.out.println("dim is :" + 0);
//        double[][] prodMat = emul(eye, vec, 0);
//        printMatrix(prodMat);
//        System.out.println();
//        System.out.println("dim is :" + 1);
//        printMatrix(emul(eye, vec, 1));
//
//        ptestname("矩阵与向量相除");
//        printMatrix(eye);
//        System.out.println("dim is :" + 0);
//        double[][] dividMat = edivid(eye, vec, 0);
//        printMatrix(dividMat);
//        System.out.println();
//        System.out.println("dim is :" + 1);
//        printMatrix(edivid(eye, vec, 1));
//
//        double[][] appMat = new double[1][];
//
//        appMat[0] = vec;
//
//        ptestname("水平stack");
//
//        double[][] hmat = T(appMat);
//        printMatrix(hmat);
//        printMatrix(vstack(eye, hmat));
//        System.out.println();
//        printMatrix(vstack( hmat,eye));
//
//        ptestname("垂直stack");
//        printMatrix(appMat);
//        printMatrix(hstack(eye, appMat));
//        System.out.println();
//        printMatrix(hstack(appMat, eye));

//        ptestname("矩阵切片");
//        double[][] A = randomMatrix(10,10,false);
//        printMatrix(A);
//
//        double[][] block = slice(A,3,7,2,5);
//        printMatrix(block);
//
//        double[][] rblock = slice(A,3,7,0);
//        printMatrix(rblock);
//
//        double[][] cblock = slice(A,3,7,1);
//        printMatrix(cblock);
//
//        ptestname("矩阵索引");
//        double[] prod = new double[10];
//        for (int i = 0 ; i < prod.length; i++)
//            prod[i] = i;
//
//        double[][] B = oneMat(10);
//        double[][] Bt = emul(B,prod,0);
//        printMatrix(Bt);
//
//        double[][] rowb = iloc(Bt,new int[]{1,2,3,4,5,},0);
//        printMatrix(rowb);
//        double[][] colb = iloc(Bt,new int[]{3,4,5},1);
//        printMatrix(colb);


//        /**
//         *  矩阵列表的 展平和重构
//         * */
//
//        ptestname("测试矩阵展平");
//        List<double[][]> testList = new ArrayList<double[][]>();
//        int sizes[] = new int[]{3,4,4,3,2};
//        List<Integer> sizeList = new ArrayList<Integer>();
//        // size : (4,3+1),(4,4+1)(3,4+1),(2,3+1)
//        sizeList.add(sizes[0]);
//        for (int i = 0; i < 4; i++) {
//            testList.add(fill(i+1,sizes[i+1],sizes[i]+1));
//            sizeList.add(sizes[i+1]);
//        }
//
//
//        for (int i = 0 ; i < 4; i++) {
//            System.out.println();
//            printMatrix(testList.get(i));
//        }
//
//        double[] revalArr = reval(testList);
//        System.out.println("展平后的长度为：" + revalArr.length +" 目标长度是" +((4*4+4*5+3*5+2*4)));
//
//        System.out.println(Arrays.toString(revalArr));
//
//        ptestname("根据 size 列表 重构矩阵");
//
//        List<double[][]> resizes = resize(revalArr,sizeList,true);
//
//        for (double[][] matrix : resizes) {
//            System.out.println();
//            printMatrix(matrix);
//        }


        double[][] five = oneMat(6,6);

        double[][] one = fill(1,1,1);


        double[][] conf = convnValid(five,one);

        double[][] mean = scaleMatrix(five, new Size(2,2));

        printMatrix(mean);
    }

    public static void main(String[] args) {

//        testConvn();
//        testScaleMatrix();
//        testKronecker();
//        testMatrixProduct();
//        testCloneMatrix();
//        testRot180();

        try {
            batchTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}




















