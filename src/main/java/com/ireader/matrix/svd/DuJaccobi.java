package com.ireader.matrix.svd;


import static com.ireader.ml.Matrix.*;

/**
 * Created by zxsted on 15-10-28.
 *
 *
 * 双边jaccobi 旋转
 * 对于正定的对称矩阵，奇异值等于特征值，奇异向量等于特征向量
 * 用于求解对称矩阵的特征值和特征向量
 *
 */
public class DuJaccobi {


    private static double threshold = 1E-8;    //
    private static int iteration = 30;         // 迭代次数的上限

    public static int sign(double number) {

        return (number >= 0)?1:-1;
    }

    /**
     *  获取 i，j 对应的旋转矩阵
     *
     *  @param mat:double[][]  待旋转的矩阵
     * */
    public static double[][] getRotateMatForDuSide (double[][] mat,int i,int j ) {

        int m = mat.length;

        double ele =  mat[i][j];      // 要被消去的点

        if (Math.abs(ele) < threshold) return null;

        double ele1 = mat[i][i];
        double ele2 = mat[j][j];

        double tao = (ele1 - ele2) / (2 * ele);

        /** 计算旋转角度 */
        double tan = sign(tao) / (Math.abs(tao) + Math.sqrt(1 + Math.pow(tao,2)));
        double cos = 1 / Math.sqrt(1 + Math.pow(tan,2));
        double sin = cos * tan;

        double[][] G = eyeMat(m);

        G[i][i] = cos;
        G[i][j] = -1 * sin;
        G[j][i] = sin;
        G[j][j] = cos;

        return G;
    }


    /**
     *  执行一次双边Jaccobi 旋转
     *
     * @param mat :
     *             double[][]  待旋转的矩阵
     * @param ratoteMat:
     *                 double[][] 旋转矩阵
     * */
    public static double[][] duJaccobiOneRate(double[][] mat,double[][] ratoteMat) {

        return dot(dot(T(ratoteMat),mat),ratoteMat);
    }

    /**
     * 执行一次 Jaccobi 计算的累乘
     *
     * @param cumMat
     * @param ratoteMat
     * @return
     */
    public static double[][] cumprod(double[][] cumMat,double[][] ratoteMat) {
        return dot(cumMat,ratoteMat);
    }


    /**
     *  执行完整的 Jaccobi 循环，
     *    将 得到的特征值保存在 Evec 中， 特征向量保存在JMat中
     *
     *   @param mat ：
     *              double[][] 待求解的矩阵
     *   @param Evec:
     *              double[]  特征值保存空间
     *   @Param JMat：
     *              double[][] 保存特征向量
     * */
    public static  void duJaccobiRatote(double[][] mat, double[] Evec,double[][] JMat ) {

        int size = mat.length;

        int iter = iteration;

        double[][] cummat = eyeMat(size);

        while ( iter-- > 0) {
            boolean pass = false;

            for (int i = 0 ; i < size; i++) {

                // 注意 j 的索引 是从 i + 1 开始的
                for (int j = i+1; j < size; j++) {
                    double[][] G = getRotateMatForDuSide(mat, i, j);

                    if (null == G) {
                        pass = true;
                        continue;
                    }

                    // 执行一次 Jaccobi 旋转
                    mat = duJaccobiOneRate(mat, G);

                    //  累次计算特征向量
                    cummat = cumprod(cummat, G);
                }
            }
            // 如果非主对角元素全部变为 0 时 ，退出
            if (pass) break;

            System.out.println("迭代次数：" +(iteration - iter) );
        }

        // get egin and vector
        for (int i = 0 ; i < size; i++) {
            Evec[i] = (mat[i][i] < threshold)? 0:mat[i][i];
            for (int j = 0 ; j < size; j++) {
                JMat[i][j] = cummat[i][j];
            }
        }

    }


    /**
     *  one case single side jaccobi rotate
     *
     *  单边Jacobi之于双边Jacobi的一个不同就是每次旋转只影响到矩阵A的两列，因经很多列对的正交旋转变换是可以并行执行的。

     * 基本思想是将A按列分块，每一块分配到一个计算节点上，块内任意两列之间进行单边Jacobi正交变换，所有的计算节点可以同时进行。
     * 问题就是矩阵块内部列与列之间进行正交变换是不够的，我们需要所有矩阵块上的任意两列之间都进行正交变换，
     * 这就需要计算节点之间交换矩阵的列。采用奇偶序列的方法，
     * 具体可参考文章 http://cdmd.cnki.com.cn/Article/CDMD-10285-1012286387.htm
     *
     * */
    public static boolean oneSideRotate (double[][] mat,double[][] Vmat,int i ,int j) {

        int rownum = mat.length;
        int colnum = mat[0].length;

        assert i < j : "i >= j";

        double[][] Tmat = T(mat);

        double[] ci = Tmat[i];
        double[] cj = Tmat[j];

        double ele = dot(ci,cj);

        // two col is orthogonal
        if (Math.abs(ele) < threshold) return true;

        double ele1 = dot(ci,ci);
        double ele2 = dot(cj,cj);


        /** =只要每次旋转前都把范数大的列放在前面，就可以保证求出的奇异值是递减排序的= */
        if (ele1 < ele2) {

            for (int row =0 ; row < rownum; row++) {
                mat[row][i] = cj[row];
                mat[row][j] = ci[row];
            }

            for (int row = 0; row < colnum; row++) {
                double tmp = Vmat[row][i];
                Vmat[row][i] = Vmat[row][j];
                Vmat[row][j] = tmp;
            }
        }

        double tao = (ele1 - ele2) / ( 2 * ele);
        double tan = sign(tao) / (Math.abs(tao) + Math.sqrt(1 + Math.pow(tao,2)));
        double cos = 1 / Math.sqrt(1 + Math.pow(tan,2));
        double sin = cos * tan;

        for (int row = 0; row < rownum ; row++) {

            double var1 = mat[row][i] * cos + mat[row][j] * sin;
            double var2 = mat[row][j] * cos - mat[row][i] * sin;
            mat[row][i] = var1;
            mat[row][j] = var2;
        }

        for (int col = 0 ; col <colnum; col++ ) {

            double var1 = Vmat[col][i] * cos + Vmat[col][j] * sin;
            double var2 = Vmat[col][j] * cos - Vmat[col][i] * sin;

            Vmat[col][i] = var1;
            Vmat[col][j] = var2;
        }

        return false;
    }


    public static void hestens_Jaccobi(double[][] mat,double[][] Vmat) {

        int rownum = mat.length;
        int colnum = mat[0].length;

        int iter = iteration;

        while (iter-- >0) {

            boolean pass = false;                 // 指示矩阵的所有列否正交

            for (int i = 0; i < colnum; i++) {
                // 注意 j 的索引 是从 i + 1 开始的
                for (int j = i+1 ; j < colnum; j++) {
                    pass = oneSideRotate(mat, Vmat,i,j);
                }
            }

            if (pass)
                break;
        }

        System.out.println("迭代次数：" + (iteration - iter));
    }

    /**
     *  SVN
     *
     * mat = U*S*V
     *
     *  返回矩阵的 非零特征值个数
     * */
    public static int svn(double[][] mat,double[][] Smat,double[][] Umat,double[][] Vmat ) {

        int rownum = mat.length;
        int colnum = mat[0].length;

        assert rownum <= colnum: "行数小于列数";

        hestens_Jaccobi(mat,Vmat);

        // 存放 奇异值
        double[] E = new double[colnum];

        // 记录非0奇异值个数
        int non_zero = 0;

        for (int i = 0 ; i < colnum; i++) {
            double[] col = T(mat)[i];
            double norm = Math.sqrt(dot(col,col));

            if (norm > threshold) {
                non_zero++;
            }
            E[i] = norm;
        }

        /**
         *  U矩阵的后(rows-none_zero)列以及V的后(columns-none_zero)列就不计算了，采用默认值0。
         *  对于奇异值分解A=U*Sigma*V^T，我们只需要U的前r列，V^T的前r行（即V的前r列），就可以恢复A了。r是A的秩
         * */
        for (int row = 0 ; row < colnum; row++) {
            Smat[row][row] = E[row];

            for (int col = 0 ; col < non_zero; col++) {
                Umat[row][col] = mat[row][col] / E[col];
            }
        }

        return non_zero;

    }


    /**
     *  SVD
     * */
    public static void SVD(double[][] Amat,double[][] Umat,double[][] Vmat ,double[] EVec) {

        int rownum = Amat.length;
        int colnum = Amat[0].length;

        assert rownum <= colnum;
        assert Umat.length == rownum;
        assert Umat[0].length == rownum;
        assert Vmat.length == colnum;
        assert Vmat[0].length == colnum;
        assert EVec.length == colnum;

        /** A trans product A */
        double[][]  sigmMat = dot(T(Amat),Amat);

        double[][] J = new double[colnum][colnum];
        double[] S = new double[colnum];

        duJaccobiRatote(sigmMat,S,J);

        for (int i = 0 ; i < S.length; i++) {
            S[i] = Math.sqrt(S[i]);
        }

        //TODO : sort S and J

        //TODO: Umat generate


    }


    /**
     *  单边的 JACCOBI 旋转的 SVD 并行算法
     *
     * 单边Jacobi之于双边Jacobi的一个不同就是每次旋转只影响到矩阵A的两列，因经很多列对的正交旋转变换是可以并行执行的。
     *
     * 基本思想是将A按列分块，每一块分配到一个计算节点上，块内任意两列之间进行单边Jacobi正交变换，
     * 所有的计算节点可以同时进行。问题就是矩阵块内部列与列之间进行正交变换是不够的，我们需要所有矩
     * 阵块上的任意两列之间都进行正交变换，这就需要计算节点之间交换矩阵的列。采用奇偶序列的方法，具
     * 体可参考文章 http://cdmd.cnki.com.cn/Article/CDMD-10285-1012286387.htm。
     *
     * while () {
     *     //奇数次迭代后矩阵按列范数从大到小排列；偶数次迭代后矩阵按列范数从小到大排列
     *
     *    //  每个计算节点上相邻两列进行单边Jacobi变换
     *
     *    // 从i = 1 开始循环 ；循环次数为 矩阵的列数 ； 原矩阵有几列就需要做几轮的交换
     *    for ( int i = 1; i <= totalColum; i++) {
     *
     *      int send=0,recv=0;  //send记录是否要把本矩阵块的最后一列发送给下一个计算结点；recv记录是否要从上一个计算结点接收一列数据
     *
     *
     *      // 注意下面的判断逻辑
     *      int mod1=i%2;       //余数为0时是奇序，否则为偶序
     *      int mod2=(myRank*COL)%2;    //判断本块的第1列(首列)是否为原矩阵的第奇数列，为0则是，为1则不是
     *      if(mod1^mod2){      //融合了奇序和偶序的情况
     *          j=0;        //首列可以和第二列进行正交变换
     *      } else {
     *          j=1;        //首列需要和上一个计算结点的最后一列进行正交变换
     *          if(myRank>0){        //不是第1个计算节点
     *              recv=1;     //需要从上一个计算节点接收最后一列
     *          }
     *      }
     *
     *      // 注意这里的步长是 2
     *      for(++j;j<COL;j+=2){     //第j列与j-1列进行单边Jacobi正交变换
     *          orthogonal(A,ROW,COL,j-1,j,&pass,V,totalColumn);
     *          exchangeColumn(A,ROW,COL,j-1,j);        //正交变换之后交换两列
     *          exchangeColumn(V,totalColumn,COL,j-1,j);
     *      }
     *      if(j==COL){     //最后一列剩下了，无法配对，它需要发送给下一个计算节点
     *          if(myRank<procSize-1){   //如果不是最后1个计算节点
     *              send=1;         //需要把最后一列发给下一个计算节点
     *          }
     *      }
     *      if(send) {
     *          //把最后一列发给下一个计算节点
     *          MPI_Send(lastColumnA,ROW,MPI_DOUBLE,myRank+1,59,MPI_COMM_WORLD);
     *          MPI_Send(lastColumnV,totalColumn,MPI_DOUBLE,myRank+1,60,MPI_COMM_WORLD);
     *
     *      }
     *
     *      if (recv) {
     *          //从上一个计算节点接收最后一列
     *          //本行首列与上一个计算节点末列进行正交变换
     *          //把preColumn留下
     *          //把firstColumn发送给上一个计算结点
     *      }
     *      if(send) {
     *          //把最后一列发给下一个计算节点后，下一个计算节点做完了正交变换，又把一列发送回来了，现在需要接收
     *          //把接收到的那一列赋给本块的最后一列
     *
     *      }
     *
     *      //各个计算节点都完成一次迭代后，汇总一上是不是所有的都pass了
     *
     *
     *    }
     *
     * }
     *
     * */


    /** ========================= test main ====================================================== */

    public static void main(String args[]) {

//        double[][] Amt = randomMatrix(10,10,true);
        double[][] Amt = fill(3,10,10);
//        Amt = matrixOp(Amt,eyeMat(10),null,null,minus);
//        Amt = eyeMat(10);
        printMatrix(Amt);

        Amt = new double[][]{
                {1,1,1,1,0,0},
                {1,1,1,1,0,0},
                {1,1,1,1,0,0},
                {1,1,1,1,0,0},
                {0,0,0,0,0,0},
                {0,0,0,0,0,0}

        };

        double[][] U = new double[10][10];
        double[][] V = eyeMat(10);   // 对应 S
        double[][] E = new double[10][10];

        int rank = svn(Amt,E,U,V);

        ptestname("执行svd矩阵分解");

        System.out.println("矩阵的秩是：" + rank);

        printMatrix(U);
        printMatrix(V);
        printMatrix(E);

    }


}
