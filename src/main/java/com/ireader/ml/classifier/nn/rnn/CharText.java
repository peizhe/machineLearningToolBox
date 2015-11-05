package com.ireader.ml.classifier.nn.rnn;

import com.ireader.ml.Matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zxsted on 15-11-1.
 */
public class CharText {

    private static final String LOCAL_DATA_PATH = "";

    private Map<String,Integer> charIndex = new HashMap<String, Integer>();
    private Map<Integer,String> indexChar = new HashMap<Integer, String>();
    private Map<String,double[]> charVector = new HashMap<String,double[]>();
    private List<String> sequence = new ArrayList<String>();

    public void init() {

        // 加载书籍 构建映射字典
        loadData();
        // 构建char的分布式向量表示
        buildDistributeRepresentations();
    }



    private void loadData() {

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(new File(LOCAL_DATA_PATH + "toy.txt")));

            String line = null;

            while ((line = br.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("")) {
                    sequence.add(line.toLowerCase());

                    for (char c: line.toLowerCase().toCharArray()) {
                        String key = String.valueOf(c);
                        if (!charIndex.containsKey(key)) {
                            charIndex.put(key,charIndex.size());
                            indexChar.put(charIndex.get(key),key);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void buildDistributeRepresentations() {
        for (String c : charIndex.keySet()) {
            double[] xt = Matrix.zeroArray(charIndex.size());
            xt[charIndex.get(c)]=1;
            charVector.put(c,xt);
        }
    }

    public Map<String,Integer> getCharIndex() {
        return charIndex;
    }

    public Map<String,double[]> getCharVector() {
        return charVector;
    }

    public List<String> getSequence() {
        return sequence;
    }

    public Map<Integer,String> getIndexChar() {
        return indexChar;
    }




    public static void main(String[] args) {
        CharText ct = new CharText();
        ct.init();
    }


}
