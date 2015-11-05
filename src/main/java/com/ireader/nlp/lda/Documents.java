package com.ireader.nlp.lda;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zxsted on 15-10-19.
 *
 * 文档集对象， 存储用于训练的所有文档
 *
 * 作用：  将文档转化为单词的 id，并提供互相转化的函数
 *
 */
public class Documents {

    ArrayList<Document> docs;               // 文档列表
    Map<String,Integer> termToIndexMap;     // 单词到 ID 的映射
    ArrayList<String>   indexToTermMap;     // 单词id转化为单词
    Map<String,Integer> termCountMap;       // 词频统计


    public Documents() {
        docs = new ArrayList<Document>();
        termToIndexMap = new HashMap<String,Integer>();
        indexToTermMap = new ArrayList<String> ();
        termCountMap = new HashMap<String,Integer>();
    }

    /**
     * 从指定路径加载文档集， 初始化Documents 对象
     *
     * @param docsPath
     */
    public void readDocs(String docsPath) {
        for (File docFile : new File(docsPath).listFiles()) {
            Document doc = new Document(docFile.getAbsolutePath(),termToIndexMap,indexToTermMap,termCountMap);
            docs.add(doc);
        }
    }

    /** 一个文档对象 ，存储一个文档的所有的单词 */
    public static class Document {

        private String docName;
        int[] docWords;        // 存储文档单词对应的词ID
        private Pattern MY_PATTERN = Pattern.compile(".*[a-zA-z]+.*]");

        public Document(String docName,Map<String,Integer> termToIndexMap,
                        ArrayList<String> indexToTermMap,Map<String,Integer> termCountMap){
            this.docName = docName;

            ArrayList<String> docLines = new ArrayList<String>();   // 暂存文档的各个句子
            ArrayList<String> words = new ArrayList<String>();
            readLines(docName,docLines);

            for (String line : docLines) {
                for(String word : line.split("\t")) {
                    words.add(word);
                }
            }

            // 移除噪音数据
            for (int i = 0 ; i < words.size(); i++) {
                if (isNoiseWord(words.get(i))) {
                    words.remove(i);
                    i--;
                }
            }

            // 将单词转化为index
            this.docWords = new int[words.size()];
            for(int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if(!termToIndexMap.containsKey(word)) {
                    int newIndex = termToIndexMap.size();
                    termCountMap.put(word,newIndex);
                    indexToTermMap.add(word);
                    termCountMap.put(word,new Integer(1));
                    docWords[i] = newIndex;
                } else {
                    docWords[i] = termToIndexMap.get(word);
                    termCountMap.put(word,termCountMap.get(word) + 1);
                }
            }
            words.clear();   // 清空暂存空间
        }

        private void readLines(String docName,List<String> docLines) {

            BufferedReader br = null;
            FileReader fin = null;
            String line = null;

            try {
                fin = new FileReader(docName);
                br = new BufferedReader(fin);

                while ((line = br.readLine()) != null) {
                    docLines.add(line.trim());
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                    try {
                        if(fin != null) fin.close();
                        if(br != null) br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }


        /**
         *  验证是否是 噪音单词
         * */
        public boolean isNoiseWord(String string) {

            string = string.toLowerCase().trim();

            Matcher m = MY_PATTERN.matcher(string);

            if(string.matches(".*www\\..*") || string.matches(".*\\.com.*") ||
                    string.matches(".*http:.*") )
                return true;
            if (!m.matches()) {
                return true;
            } else
                return false;

        }
    }

}
