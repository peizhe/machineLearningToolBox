package com.ireader.hmm.local;

import java.io.*;
import java.util.*;

/**
 * Created by zxsted on 15-10-28.
 *
 *
 * HHM 的 评估与解码功能
 */
public class HmmInference {

    private List<String> stateSet = new LinkedList<String>(); // 状态值集合
    private List<String> observeSet = new LinkedList<String>(); // 观察值集合
    private double[]     stateProb;    // 初始状态转移矩阵
    private double[][]   stateTrans; // 状态转移矩阵
    private double[][]   emission ;  // 发射矩阵
    private double[]     minEmission; // 发射矩阵每一行的极小值 （用于零值平均）


    /***/
    public void initParam(String tagFile) {

        Map<String,Integer> stateIndexMap = new HashMap<String,Integer>(); // 状态之索引
        Map<String,Integer> observeIndexMap = new HashMap<String,Integer>(); // 观察值及其编号

        int[] stateCount;   // 状态值以及其计数
        int[][] stateTransCount; // 状态转移矩阵
        int[][] confusionCount;  // 混淆计数矩阵

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(
                    tagFile)));

            if (br.markSupported()) {
                br.mark(1024*1024*100);
            }

            String line = null;

            int stateTotal = 0;
            int observeTotal = 0;

            //first scan file, init stateIndexMap observeIndexMap

            while ((line = br.readLine()) != null) {
                // one line contains : one observation val, one state val, split with space
                String[] arr = line.split("\\s+");

                if (arr.length >= 2) {
                    String observe = arr[0];
                    String state = arr[1];
                    if (!observeIndexMap.containsKey(observe)) {
                        observeIndexMap.put(observe,observeTotal++);
                    }
                    if (!stateIndexMap.containsKey(state)) {
                        stateIndexMap.put(state,stateTotal++);
                    }
                }

            }

            if (br.markSupported()) {
                br.reset();
            } else {
                br.close();
                br = new BufferedReader(new FileReader(new File(tagFile)));
            }

            // second scan file  ,init val of  statCount confusionCount stateCount
            for (int i = 0 ; i < stateTotal; i++) {
                stateSet.add("");
            }

            for (int i = 0 ; i < observeTotal; i++) {
                observeSet.add("");
            }

            stateTransCount = new int[stateTotal][];

            for (int i = 0 ; i < stateIndexMap.size(); i++) {
                stateTransCount[i] = new int[stateTotal];
            }

            confusionCount = new int[stateTotal][];
            for (int i = 0 ; i < stateTotal; i++) {
                confusionCount[i] = new int[observeTotal];
            }

            stateCount = new int[stateTotal];
            String preState = null;

            while ((line = br.readLine()) != null) {
                String[] arr = line.split("\\s+");
                if (arr.length >= 2) {
                    String observe = arr[0];
                    String state = arr[1];
                    int row = stateIndexMap.get(state);
                    int col = 0;
                    int oldCount = 0;

                    col = observeIndexMap.get(observe);
                    oldCount = confusionCount[row][col];
                    confusionCount[row][col] = oldCount + 1;

                    stateCount[row] = stateCount[row] + 1;

                    if (preState == null) {
                        preState = state;
                    } else {
                        row = stateIndexMap.get(preState);
                        col = stateIndexMap.get(state);
                        oldCount = stateTransCount[row][col];
                        stateTransCount[row][col] = oldCount + 1;
                        preState = state;
                    }
                } else {
                    preState = null;
                }
            }

            br.close();


            // 给HMM基本参数赋值
            for (Map.Entry<String,Integer> entry : stateIndexMap.entrySet()) {
                String state = entry.getKey();
                int index = entry.getValue();
                stateSet.set(index,state);
            }

            for (Map.Entry<String, Integer> entry : observeIndexMap.entrySet()) {

                String observe = entry.getKey();
                int index = entry.getValue();
                observeSet.set(index,observe);
            }

            stateProb = calProbByCount(Smooth.GoodTuring(stateCount));

            stateTrans = new double[stateTransCount.length][];

            for (int i = 0 ; i < stateTransCount.length;i++) {
                // 计算状态转移概率时不作平滑，因为有些状态之间转移的概率就应该是0，如果平滑就会变成非0
                stateTrans[i] = calProbByCount(stateTransCount[i]);
            }

            emission = new double[confusionCount.length][];

            minEmission = new double[confusionCount.length];

            for (int i = 0 ; i < confusionCount.length; i++) {
                emission[i] = calProbByCount(Smooth.GoodTuring(confusionCount[i]));

                double min = Double.MAX_VALUE;
                for (double ele : emission[i]) {
                    if (ele < min) {
                        min = ele;
                    }
                }
                minEmission[i] = min;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    /**
     *  use forward algorith slove estimate problem,return a observe squence probability
     *
     * */
    public double estimate(List<String> obs_seq) {
        double rect = 0.0;

        int LEN = obs_seq.size();
        double[][] Q = new double[LEN][];

        // 状态的初始概率，乘上隐藏状态到观察状态的条件概率。
        Q[0] = new double[stateSet.size()];

        for(int j = 0 ; j < stateSet.size(); j++) {
            if (observeSet.contains(obs_seq.get(0))) {
                Q[0][j] = stateProb[j] * emission[j][observeSet.indexOf(obs_seq.get(0))];
            } else {
                Q[0][j] = stateProb[j] * minEmission[j];

                System.out.println("Observe value : " + obs_seq.get(0) + "has not apearance!");
            }
        }

        //  index begin of i : 首先从前一时刻的每个状态，转移到当前状态的概率求和，然后乘上隐藏状态到观察状态的条件概率
        for (int i = 1; i < LEN; i++) {
            Q[i] = new double[stateSet.size()];

            for (int j = 0 ; j < stateSet.size(); j++) {
                double sum = 0.0;
                for (int k = 0 ; k < stateSet.size();k++) {
                    sum += Q[i-1][k] * stateTrans[k][j];
                }
                if (observeSet.contains(obs_seq.get(i))) {
                    Q[i][j] = sum
                            * emission[j][observeSet.indexOf(obs_seq.get(i))];
                } else {
                    Q[i][j] = sum * minEmission[j];
                    System.err.println("obsertion val " + obs_seq.get(0) + "has not appearance!");;
                }
            }
        }

        for (int i = 0 ; i < stateSet.size();i++)
            rect += Q[LEN - 1][i];

        return rect;
    }



    /**
     * 采用viterbi进行解码：给定HMM的所有参数，给一个观察序列，评估最可能的状态序列是什么。
     * */
    public Pair<Double,LinkedList<String>> viterbi(List<String> observe) {
        LinkedList<String> sta = new LinkedList<String>();

        int LEN = observe.size();
        int M = stateSet.size();

        double[][] Q = new double[LEN][];
        int[][] Path = new int[LEN][];

        Q[0] = new double[M];
        Path[0] = new int[M];


        for (int j = 0 ; j < M; j++) {
            if (observeSet.contains(observe.get(0))) { // // 观察值在训练样本中未出现过，则概率设为0
                Q[0][j] = stateProb[j]
                        * emission[j][observeSet.indexOf(observe.get(0))];

            } else {
                Q[0][j] = stateProb[j] * minEmission[j] / 2;
                System.err.println("观察值'" + observe.get(0) + "'在已标记样本中未出现过");
            }
            Path[0][j] = -1;
        }

        for (int i = 1 ; i < LEN ; i++) {
            Q[i] = new double[M];
            Path[i] = new int[M];

            for (int j = 0 ; j < M; j++) {
                double max = 0.0;
                int index = 0;

                for (int k = 0 ; k < M; k++) {
                    if (Q[i - 1][k] * stateTrans[k][j] > max) {
                        max = Q[i - 1][k] * stateTrans[k][j];
                        index = k;  //max val is form k of last layer
                    }
                }

                if (observeSet.contains(observe.get(i))) {
                    Q[i][j] = max
                            * emission[j][observeSet.indexOf(observe.get(i))];
                } else {
                    Q[i][j] = max * minEmission[j] / 2;

                    System.err.println("观察值'" + observe.get(0) + "'在已标记样本中未出现过");
                }

                Path[i][j] = index;
            }
        }

        // 找到最后一个时刻呈现哪种状态的概率最大
        double max = 0;
        int index = 0;

        for (int i = 0 ; i < M; i++) {
            if (Q[LEN - 1][i] > max) {
                max = Q[LEN - 1][i];
                index = i;
            }
        }


        sta.add(stateSet.get(index));
        // 动态规划，逆推回去各个时刻出现什么状态概率最大
        for (int i = LEN - 1; i > 0;i++) {
            index = Path[i][index];
            sta.add(stateSet.get(index));
        }

        // 把状态序列再顺过来
        Collections.reverse(sta);

        return new Pair().of(max,sta);

    }





    /** ========================================================================================================== */

    public void baumWelch() {

    }

    public static class Pair<T,U> {

        private T val ;
        private U stateList ;

        public Pair(){}

        public Pair(T val,U stateList) {
            val = val;
            stateList = stateList;
        }
        public  Pair of(T val,U stateList) {

            return new Pair<T,U>(val, stateList);
        }

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }

        public U getStateList() {
            return stateList;
        }

        public void setStateList(U stateList) {
            this.stateList = stateList;
        }
    }

    public static class Smooth {
        public static double[] GoodTuring(int[] value) {
            double[] retArr = new double[value.length];
            for (int i = 0 ; i < value.length;i++)
                retArr[i] = value[i];
            return retArr;
        }
    }


    /** ========================================================================================================== */

    public void initStateSet(String infile) {
        readLine(infile,stateSet);
    }

    public void initObserveSet(String infile) {
        readLine(infile,observeSet);
    }

    public void initStateProb(String infile) {
        assert stateSet != null;

        int stateCount = stateSet.size();
        assert  stateCount > 0;

        stateProb = new double[stateCount];
        List<String> lines = new LinkedList<String>();
        readLine(infile,lines);
        assert  lines.size() >= stateCount;

        for (int i = 0; i < stateCount; i++) {
            stateProb[i] = Double.parseDouble(lines.get(i));
        }
    }

    public void initStateTrans(String infile) {
        List<String> lines = new LinkedList<String>();
        readLine(infile,lines);
        stateTrans = new double[lines.size()][];

        for (int i = 0 ; i < lines.size();i++) {
            String[] counts = lines.get(i).split("\\s+");
            stateTrans[i] = new double[counts.length];

            for (int j = 0 ; j < stateTrans[i].length;j++) {
                stateTrans[i][j] = Double.parseDouble(counts[j]);
            }
        }
    }


    public void initConfusion(String infile) {
        List<String> lines = new LinkedList<String>();
        readLine(infile,lines);
        emission = new double[lines.size()][];
        minEmission = new double[lines.size()];

        for (int i = 0 ; i < lines.size(); i++) {
            String[] counts = lines.get(i).split("\\s+");
            emission[i] = new double[counts.length];
            double min = Double.MAX_VALUE;

            for (int j = 0 ; j < emission[i].length;j++) {
                double ele = Double.parseDouble(counts[j]);
                emission[i][j] = ele;

                if (ele < min) {
                    min = ele;
                }
            }

            minEmission[i] = min;
        }
    }


    /** ========================================================================================================== */

    public List<String> getStateSet() {
        return stateSet;
    }

    public void setStateSet(List<String> stateSet) {
        this.stateSet = stateSet;
    }

    public List<String> getObserveSet() {
        return observeSet;
    }

    public void setObserveSet(List<String> observeSet) {
        this.observeSet = observeSet;
    }

    public double[] getStateProb() {
        return stateProb;
    }

    public void setStateProb(double[] stateProb) {
        this.stateProb = stateProb;
    }

    public double[][] getStateTrans() {
        return stateTrans;
    }

    public void setStateTrans(double[][] stateTrans) {
        this.stateTrans = stateTrans;
    }

    public double[][] getEmission() {
        return emission;
    }

    public double[] getMinEmission() {
        return minEmission;
    }

    public void setMinEmission(double[] minEmission) {
        this.minEmission = minEmission;
    }

    public void setEmission(double[][] emission) {
        this.emission = emission;
        minEmission = new double[emission.length];

        for (int i = 0; i < emission.length;i++) {
            double min = Double.MIN_VALUE;
            for (double ele:emission[i]) {
                if (ele < min) min = ele;
            }

            minEmission[i] = min;
        }
    }

    /** ========================================================================================================== */



    /**
     *  通过一组计数计算概率
     *
     * */
    public double[] calProbByCount(double[] countArr) {
        double sum = 0.0;

        for (double count : countArr) {
            sum += count;
        }

        double[] prob = new double[countArr.length];

        for (int i = 0 ; i < countArr.length; i++) {
            prob[i] = countArr[i] / sum;
        }
        return prob;
    }

    /**
     *
     * */
    private double[] calProbByCount(int[] countArr) {
        double sum = 0.0;
        for (double count : countArr) {
            sum += count;
        }
        double[] prob = new double[countArr.length];
        for (int i = 0; i < countArr.length; i++) {
            prob[i] = countArr[i] / sum;
        }
        return prob;
    }





    public void readLine( String filename,List<String> list) {

        BufferedReader br = null;
        FileInputStream fin = null;

        try{
            String line = null;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

            while((line = br.readLine().trim()) != null) {
                list.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
             try {
                 if (fin != null) fin.close();
                 if (br != null)  br.close();;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
