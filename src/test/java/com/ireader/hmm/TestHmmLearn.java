package com.ireader.hmm;

import com.ireader.hmm.local.HmmInference;
import com.ireader.hmm.local.HmmLearn;
import com.sun.tools.javac.util.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * Created by zxsted on 15-10-29.
 */
public class TestHmmLearn {

    private static String corpusPath;
    private static String modelPath;

    @BeforeClass
    public static void setup() throws IOException {
        corpusPath = TestHmmLearn.class.getResource("/").getPath() +
                "resources/corpus/";
        modelPath = TestHmmLearn.class.getResource("/").getPath() +
                "/resources/model/hmm/";
    }


    /**
     *  先通过BaumWelch算法去学习模型参数,并把学习到的参数写入文件
     * */
    @Test
    public void printParam() throws IOException {

        HmmLearn hmmLearn = new HmmLearn();
        hmmLearn.initParam(4, corpusPath + "msr_test");
        hmmLearn.baumWelch();

        double[] PI = hmmLearn.getStateProb();
        double[][] A = hmmLearn.getStateTrans();
        double[][] B = hmmLearn.getEmission();

        int stateCount = PI.length;

        List<String> stateSet = new ArrayList<String>();

        for (int i = 0 ; i < stateCount; i++) {
            stateSet.add(getLabelName(i+1));
        }

        writeLines(modelPath + "state",stateSet);

        // -----------------------------------------------------
        List<String> observeSet = new LinkedList<String>();
        Map<String,Integer> observeIndex = hmmLearn.getObserveIndexMap();
        for (Map.Entry<String,Integer> entry: observeIndex.entrySet()) {
            observeSet.add(entry.getKey());
        }

        writeLines(modelPath + "observe",observeSet);

        // -----------------------------------------------------
        List<String> stateProb = new ArrayList<String>();
        for (double pi : PI) {
            stateProb.add(String.valueOf(pi));
        }

        writeLines(modelPath + "pi",stateProb);

        // -----------------------------------------------------

        BufferedWriter bw = new BufferedWriter(new FileWriter(
                modelPath + "A"));

        for (int i = 0 ; i < A.length; i++) {
            for (int j = 0 ; j < A[i].length; j++) {
                bw.write(A[i][j] + "\t");
            }
            bw.newLine();
        }

        bw.close();

        // -----------------------------------------------------
        bw = new BufferedWriter(new FileWriter(
                modelPath + "B"));

        for (int i = 0 ; i < B.length; i++) {
            for (int j = 0 ; j < B[i].length;j++) {
                bw.write(B[i][j] +"\t");
            }
            bw.newLine();
        }
        bw.close();

    }

    /**
     *  再用学习好的参数去做inference
     * */
    public void testInference() {

        HmmInference hmmInference = new HmmInference();

        hmmInference.initStateSet(modelPath + "state");
        hmmInference.initObserveSet(modelPath + "observe");
        hmmInference.initStateProb(modelPath + "PI");
        hmmInference.initStateProb(modelPath + "A");
        hmmInference.initConfusion(modelPath + "B");

        String sentence = "他们的成长与优越的家庭背景没有任何正相关的关系";

        List<String> obs_seq = str2List(sentence);
        HmmInference.Pair<Double,LinkedList<String>> states = hmmInference.viterbi(obs_seq);

        System.out.println("最可能的状态序列：" + states.getStateList()+"其概率为：" + states.getVal());

        double ratio = hmmInference.estimate(obs_seq);

        System.out.println("句子1出现的概率：" + ratio);

        sentence = "为报倾城随太守亲射虎看孙郎会挽雕弓如满月西北望";// 要保证两个句子长度相等，比较其出现概率才有意义

        obs_seq = str2List(sentence);
        double ratio2 = hmmInference.estimate(obs_seq);

        System.out.println("句子1出现的概率：" + ratio2);

        equals(ratio > ratio2);

    }



    /** =================== 辅助函数 ======================================== */

    public void writeLines(String filename,List<String> lines) {

        PrintWriter pw = null;
        FileOutputStream fout = null;

        try {
            pw = new PrintWriter(new FileOutputStream(filename));

            for (String line : lines) {
                pw.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                if (fout != null) fout.close();
                if (pw != null) pw.close();;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 按照Excel的规则，给一个列号，给出列名称。比如1对应A，26对应Z，27对应AA，53对应BA
     *
     * @param a
     * @return
     */
    private static String getLabelName(int a) {

        StringBuilder sb = new StringBuilder();
        char[] arr = new char[26];

        arr[0] = 'A';
        for (int i = 1 ; i < 26; i++) {
            arr[i] = (char) (arr[i - 1] + 1);
        }

        Deque<String> deque = new ArrayDeque<String>();

        while(a > 0) {
            char c = arr[(a - 1) % 26];
            deque.push(String.valueOf(c));
            a = (a - 1) / 26;
        }

        while (deque.size() > 0) {
            sb.append(deque.pop());
        }

        return sb.toString();
    }

    private List<String> str2List(String sentence) {

        List<String> obs_seq = new ArrayList<String>();
        int i = 0 ;
        while (i < sentence.length()) {
            obs_seq.add(sentence.substring(i,i+1));
            i++;
        }

        return obs_seq;
    }

}
