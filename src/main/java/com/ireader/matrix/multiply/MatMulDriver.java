package com.ireader.matrix.multiply;

import com.ireader.ml.Driver;
import com.ireader.conf.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by zxsted on 15-9-2.
 *
 */
public class MatMulDriver  extends Driver{




    private Configuration conf = null;

    // A is an m-by-n matrix; B is an n-by-p matrix.
    private int m;
    private int n;
    private int p;
    private int s;   // Number of rows in a block in A.                                        A 的一个block中的行数
    private int t;   // Number of columns in a block in A = number of rows in a block in B.    A 的一个block中的列数 或 B 的一个block 中的行数
    private int v;   // Number of columns in a block in B.                                     B 的一个block 中的列数
    private String  leftflag;

    private String inputPath;
    private String outputPath;

    public MatMulDriver(){
        this.conf = new Configuration();
    }

    /**==================== 参数导入函数 =======================================================*/


    public MatMulDriver setLeftflag(String _leftflag){
        this.leftflag = _leftflag;
        this.conf.set("leftflag",_leftflag);
        return this;
    }

    public MatMulDriver setM(int _m) {
        this.m = _m;
        this.conf.setInt("m", m);
        return this;
    }

    public MatMulDriver setN(int _n) {
        this.n = _n;
        this.conf.setInt("n", n);
        return this;
    }

    public MatMulDriver setP(int _p) {
        this.p = _p;
        this.conf.setInt("p",p);
        return this;
    }

    public MatMulDriver setS(int _s) {
        this.s = _s;
        this.conf.setInt("s",s);
        return this;
    }

    public MatMulDriver setT(int _t) {
        this.t = _t;
        this.conf.setInt("t",t);
        return this;
    }

    public MatMulDriver setV(int _v) {
        this.v = _v;
        this.conf.setInt("v",v);
        return this;
    }

    public MatMulDriver setInputPath(String inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public MatMulDriver setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }


    /**=======================================================================================*/


    @Override
    public boolean fit() throws IOException, InterruptedException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean transform() throws IOException, InterruptedException, ClassNotFoundException {

        /**
         * step one
         * */
        boolean success = runJob(this.conf,
                "Matrix and Matrix Multiplication first step",
                MatMulDriver.class,
                this.inputPath,
                "matrix/output/mattemp",
                MatrixMultiply.FirstStepMapper.class,
                null,
                MatrixMultiply.FirstStepReducer.class,
                SubIndexPartitioner.class,
                LongWritable.class,
                Text.class,
                Text.class,
                Text.class,
                50,
                false
                );


        /**
         *  step two
         * */
        success |= runJob(this.conf,
                "Matrix and Matrix Multiplication second step",
                MatMulDriver.class,
                "matrix/output/mattemp",
                this.outputPath,
                Mapper.class,
                null,
                MatrixMultiply.DoubleSumReducer.class,
                null,
                LongWritable.class,
                Text.class,
                Text.class,
                DoubleWritable.class,
                50,
                true);

        return success;
    }


    /**================== 调用样例 ========================================================*/

    public static void main(String[] args) {


        String configfile = null;

        if(args.length == 4){
            configfile = args[3];
        }

        Config config = null;

        try {
            if (configfile != null && !configfile.equals("")) {
                config = new Config(configfile);
            } else{
                config = new Config();    // load default model preperties content
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        MatMulDriver matmultiply  = new MatMulDriver().setM(config.getInt("MatMultiply_m", 2))
                .setN(config.getInt("MatMultiply_n", 5))
                .setP(config.getInt("MatMultiply_p", 3))
                .setS(config.getInt("MatMultiply_s", 5))
                .setT(config.getInt("MatMultiply_t", 5))
                .setV(config.getInt("MatMultiply_v", 3))
                .setInputPath(config.getString("MatMultiply_input", ""))
                .setOutputPath(config.getString("MatMultiply_output",""))
                .setLeftflag(config.getString("MatMultiply_leftflag","A"));
        try {

            boolean success = matmultiply.transform();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
