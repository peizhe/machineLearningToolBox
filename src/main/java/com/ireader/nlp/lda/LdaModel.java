package com.ireader.nlp.lda;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zxsted on 15-10-19.
 *
 * lda 的 gibbis 采样函数
 */
public class LdaModel {

    int [][] doc;           // m,ndoc  存储 转化为id 的doc ndoc是 文档i的 单词个数
    int V,K,M;              // V: 单词表的长度， K： topic 的个数 ， M 文档的个数
    int [][] z;             // 为每个文档 中的单词指定topic
    float alpha;            // doc-topic 的 dilichlet 的超参数 注意 ： 这里dilichlet  参数使用的是对称 dilichlet 分布
    float beta;             // topic - word dilichlet 的超参数 注意 ： alpha 是数组 长度为k , beta 是矩阵：size : k*v :

    int[][] nmk;            // 每个文档中各个topic 的个数：   size  : m*k
    int[][] nkt;            // 每个主题中各个单词出现的次数：  size : k *V

    int[] nmkSum;           // 每个文档的单词总数
    int[] nktSum;           // 每个主题的单词总数

    double[][] phi;         // topic-word  的分布
    double[][] theta;       // doc-topic 的分布

    int iterations;         // 最大循环次数
    int saveStep;           // 两次保存的间隔步数
    int beginSaveIters;     // 开始保存的循环

    public LdaModel(Params params) {
        this.alpha = params.alpha;
        this.beta  = params.beta;
        iterations = params.iteration;
        K = params.topicNum;
        saveStep = params.saveStep;
        beginSaveIters = params.beginSaveIters;
    }

    public void initializeModel(Documents docSet) {

        /** V: 单词表的长度， K： topic 的个数 ， M 文档的个数 */
        M = docSet.docs.size();
        V = docSet.termCountMap.size();

        nmk = new int[M][K];
        nkt = new int[K][V];

        nmkSum = new int[M];
        nktSum = new int[K];

        phi = new double[K][V];         // topic-word  的分布
        theta = new double[M][K];       // doc-topic 的分布

        /** 初始化 各个文档 中单词的索引 */
        doc = new int[M][];
        for (int m = 0 ; m < M; m++) {
            //
            int N = docSet.docs.get(m).docWords.length;
            doc[m] = new int[N];
            for (int n = 0 ; n < N; n++) {
                doc[m][n] = docSet.docs.get(m).docWords[n];
            }
        }

        /** 随机初始化， 每个单词的topic */
        z = new int[M][];

        for (int m = 0 ; m < M; m++) {

            int N = docSet.docs.get(m).docWords.length;  // 文档中总词数
            z[m] = new int[N];

            for (int n = 0; n < N; n++) {

                int initTopic = (int) (Math.random() * K);
                z[m][n] = initTopic;

                // 文档m中赋与主题k的单词个数
                nmk[m][initTopic]++;
                // 主题K 中给单词v 的次数
                nkt[initTopic][doc[m][n]] ++;

                // 每个主题的单词个数
                nktSum[initTopic]++;
            }
            // 每个文档的 单词个数
            nmkSum[m] = N;
        }
    }

    /**
     *  对当前单词 mn 进行主题采样
     * */
    private int sampleTopicZ(int m,int n) {

        int oldTopic = z[m][n];

        /** 剔除当前单词的主题 */
        nmk[m][oldTopic]--;
        nkt[oldTopic][doc[m][n]]--;
        nmkSum[m]--;
        nktSum[oldTopic]--;

        /** 计算每个主题的 p(z_i = k | z_-i,w) */
        double[] p = new double[K];

        for (int k = 0; k < K; k++) {
            p[k] =( (nkt[k][doc[m][n]] + beta) / (nktSum[k] + V * beta) ) *
                    (nmk[m][k] + alpha) / (nmkSum[m] + K*alpha);
        }

        /** 使用轮盘赌法 进行采样,累加 p[k] */
        for (int k = 1; k < K; k++) {
            p[k] += p[k - 1];
        }

        double u = Math.random() * p[K - 1];  // 因为 p[] 没有 正则化

        int newTopic;
        for (newTopic = 0; newTopic < K; newTopic++) {
            if (u < p[newTopic]) {
                break;
            }
        }

        /** 采样出新的 topic ， 那么增加 当前单词的计数 */
        nmk[m][newTopic]++;
        nkt[newTopic][doc[m][n]] ++;
        nktSum[newTopic]++;

        return newTopic;
    }



    /** 用于 循环结束后 根据隐性变量分配结果，采样出结果 */
    private void updateEstimateParameters() {

        for (int k = 0; k < V; k++) {
            for (int t = 0; t < V; t++) {
                phi[k][t] = (nkt[k][t] + beta) / (nktSum[k] + V * beta);
            }
        }

        for (int m = 0 ; m < M; m++) {
            for (int k = 0; k < K; k++) {
                theta[m][k] = (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
            }
        }
    }


    public void infermodel(Documents docSet) throws Exception {

        if (iterations < saveStep + beginSaveIters) {
            throw new Exception("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
        }

        for ( int i = 0 ; i < iterations;i++) {

            System.out.println("Iteration " + i);
            if ((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep ) == 0)) {
                // 保存模型
                System.out.println("保存第" + i + "次循环的 model");

                //
            }

            // 使用 Gibbs Sampling  来更新 z[][]
            for (int m = 0 ; m < M; m++) {
                int N = docSet.docs.get(m).docWords.length;

                for (int n = 0; n < N; n++) {
                    int newTopic = sampleTopicZ(m, n);
                    z[m][n] = newTopic;
                }
            }
        }
    }



    public void saveIteratedModel(int iters,Documents docSet) throws IOException {

        String resPath = "/Home/data/";
        String modelName = "lda_" + iters;

        ArrayList<String> lines = new ArrayList<String>();

        lines.add("alpha="+alpha);
        lines.add("beta="+beta);
        lines.add("topicNum="+K);
        lines.add("docNum="+M);
        lines.add("termNum="+V);
        lines.add("iterations="+iterations);
        lines.add("saveStep="+saveStep);
        lines.add("beginSaveIters="+beginSaveIters);

        writeLines(resPath+modelName + ".params",lines);

        // lda.phi K*V
        BufferedWriter  bw = new BufferedWriter(new FileWriter(resPath+ modelName+".phi"));

        for (int i = 0 ; i < K; i++) {
            for (int j = 0 ; j < V; j++) {
                bw.write(phi[i][j]+"\t");
            }
            bw.write("\n");
        }
        bw.close();

        // lda.theta M*K

        bw = new BufferedWriter(new FileWriter(resPath + modelName + ".theta"));

        for (int i = 0 ; i < M; i++) {
            for (int j = 0 ; j < K; j++) {
                bw.write(theta[i][j] + "\t");
            }
            bw.write("\n");
        }
        bw.close();

        // lda.topic assign
        bw = new BufferedWriter(new FileWriter(resPath + modelName + ".tassign"));

        for (int m = 0 ; m < M; m++) {
            for (int n = 0; n < doc[m].length;n++) {
                bw.write(doc[m][n] +":" + z[m][n]+"\t");
            }
            bw.write("\n");
        }

        bw.close();

        // lda.twords phi[][] K*V
        bw = new BufferedWriter(new FileWriter(resPath + modelName+modelName + ".twords"));
        int topNum = 20;  // Find the top 20 topic words in each topic
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0 ; j < V; j++) {
                tWordsIndexArray.add(new Integer(j));
            }

            Collections.sort(tWordsIndexArray,new LdaModel.TwordsComparable(phi[i]));
            bw.write("topic "+ i+ "\t:\t");
            for (int t = 0; t < topNum; t++) {
                bw.write(docSet.indexToTermMap.get(tWordsIndexArray.get(t)) + " " +
                        phi[i][tWordsIndexArray.get(t)] + "\t");
            }
            bw.write("\n");
        }
        bw.close();
    }

    void writeLines(String fname,ArrayList<String> lines) {

        PrintWriter pw = null;
        FileOutputStream fout = null;

        try {
            pw = new PrintWriter(new FileOutputStream(fname));

            for (String line : lines) {
                pw.println(line);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }finally{
           if (pw != null) pw.close();
        }
    }





    public class TwordsComparable implements Comparator<Integer> {

        public double[] sortProb;   // Store probability of each word in topic k

        public TwordsComparable(double[] sortProb) {
            this.sortProb = sortProb;
        }

        @Override
        public int compare(Integer o1, Integer o2) {

            if (sortProb[o1] > sortProb[o2]) return -1;
            else if (sortProb[o1] < sortProb[o2]) return 1;
            else return 0;

        }
    }


    public static void main(String[] args) {

        String path = "";

        String path_1 = "";

        int index = path_1.lastIndexOf("/");

        String docName = path_1.substring(index+ 1);

        System.out.println(docName);
    }
}

class Params{
    float alpha;
    float beta;
    int iteration;
    int topicNum;
    int saveStep;
    int beginSaveIters;
}
