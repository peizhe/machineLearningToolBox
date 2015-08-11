package com.ireader.nlp.word2vec.vec;

import com.ireader.nlp.word2vec.util.*;
import org.apache.commons.lang.ObjectUtils;
import org.tukaani.xz.simple.ARMThumb;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by zxsted on 15-7-29.
 *
 *
 * Word2Vec 的并行实现
 */
public class Word2Vec {

    private Logger logger = Logger.getLogger("Word2Vec");

    private int windowSize; // 文字窗口的大小
    private int vectorSize; // 词向量的元素的个数

    public static enum Method{
        CBow,Skip_Gram;
    }


    private Method trainMethod; // 神经网络学习方法

    private double sample;

    private double alpha;         // 学习速率， 并行时，由线程更新
    private double alphaThresold;
    private double initialAlpha;     // 初始学习律
    private int freqThresold = 5;
    private final byte[] alphaLock = new byte[0]; // alpha 同步锁
    private final byte[] treeLock = new byte[0];  // tree 同步锁
    private final byte[] vecLock = new byte[0];   // 词向量同步锁

    private double[] expTable;                    // 预计算指数表
    private static final int EXP_TABLE_SIZE = 1000;
    private static final int MAX_EXP = 6;

    private Map<String, WordNeuron> neuronMap;

    private int totalWordCount;         // 语料中词的总数
    private int currentWordCount;       // 当前已经阅的词数， 并行是由线程更新
    private int numOfThread;            // 线程的个数

    // 单词或短语的计数器
    private Counter<String> wordCounter = new Counter<String>();

    private File tempCorpus = null;
    private BufferedWriter tempCorpusWriter;

    // 静态内部类
    public static class Factory {
        private int vectorSize = 200;
        private int windowSize = 5;
        private int freqThresold = 5;

        private Method trainMethod = Method.Skip_Gram;

        private double sample = 1e-3;

        private double alpha = 0.025,alphaThreshold = 0.0001;
        private int numOfThread = 1;

        public Factory setVectorSize(int size) {
            vectorSize = size;
            return this;
        }

        public Factory setWindow(int size) {
            windowSize = size;
            return this;
        }

        public Factory setFreqThreshold(int thresold) {
            freqThresold = thresold;
            return this;
        }

        public Factory setMethod(Method method) {
            trainMethod = method;
            return this;
        }

        public Factory setSample(double rate) {
            sample = rate;
            return this;
        }

        public Factory setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        public Factory setAlphaThresold(double alpha) {
            this.alphaThreshold = alpha;
            return this;
        }

        public Factory setNumOfThread(int numOfThread) {
            this.numOfThread = numOfThread;
            return this;
        }

        public Word2Vec build() {
            return new Word2Vec(this);
        }

    }



    private Word2Vec(Factory factory) {
        vectorSize = factory.vectorSize;
        windowSize = factory.windowSize;
        freqThresold = factory.freqThresold;
        trainMethod = factory.trainMethod;
        sample = factory.sample;
        alpha = factory.alpha;
        initialAlpha = alpha;
        alphaThresold = factory.alphaThreshold;
        numOfThread = factory.numOfThread;


        totalWordCount = 0;

        expTable = new double[EXP_TABLE_SIZE];
        computeExp();         // 预计算exp 表
    }

    /**
     * 预计算 并保存sigmoid 函数结果， 加快后续计算速度
     * f(x) = x / (x + 1)   x = exp(w*f)    这种形式是因为是sigmoid
     * */
    private void computeExp() {

        for(int i = 0 ; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = Math.exp(((1 / (double)  EXP_TABLE_SIZE * 2 -1)*  MAX_EXP));
            expTable[i] = expTable[i] / (expTable[i] + 1);
        }
    }


    /**
     *  读取一段文本， 统计词频和相邻词语出现的频率，
     *  文本将输出到一个临时文件中， 以方便后续的计算
     * */
    public void readToken(Tokenizer tokenizer) throws IOException {

        if(tokenizer == null || tokenizer.size() < 1) {
            return;
        }

        currentWordCount += tokenizer.size();

        // 读取文本中的词，并统计词频
        while(tokenizer.hasMoreTokens()) {
            wordCounter.add(tokenizer.nextToken());
        }

        // 将文本输出到文件中， 以供后续训练使用
        try{
            if(tempCorpus == null) {
                File tempDir = new File("temp");
                if (!tempDir.exists() && ! tempDir.isDirectory()) {
                    boolean tempCreated = tempDir.mkdir();
                    if(! tempCreated){
                        logger.severe("unable to create temp file in " + tempDir.getAbsolutePath());
                    }
                }

                tempCorpus = File.createTempFile("tempCorpus", ".txt", tempDir);

                if(tempCorpus.exists()) {
                    logger.info("create temp file successful in  " + tempCorpus.getAbsolutePath());
                }
                tempCorpusWriter = new BufferedWriter(new FileWriter(tempCorpus));
            }

            tempCorpusWriter.write(tokenizer.toString(" "));
            tempCorpusWriter.newLine();
        } catch(IOException e) {
            e.printStackTrace();
            tempCorpusWriter.close();
        }
    }


    // 建立单词到 单词神经元的 映射关系
    private void buildVocabulary() {

        neuronMap = new HashMap<String,WordNeuron>();

        for(String wordText:wordCounter.keySet()) {
            int freq = wordCounter.get(wordText);
            if (freq < freqThresold) {
                continue;
            }
            neuronMap.put(wordText,
                    new WordNeuron(wordText,wordCounter.get(wordText),vectorSize));
        }

        logger.info("read" + neuronMap.size() + " word totally." );
    }


    // skipGram
    private void skipGram(int index, List<WordNeuron> sentence,int b, double alpha ) {
        WordNeuron word = sentence.get(index);    // 当前索引word

        int a,c = 0;    // b 是收缩词数 ， 下面保证在index 的一个半径为windowsize 的窗口中取词
        for (a = b; a < windowSize * 2 + 1 - b ; a++) {
            if(a  == windowSize) {
                continue;
            }
            c  = index - windowSize + a;    // 窗空起始值
            if (c <0 || c >=  sentence.size()) {
                continue;
            }

            double[] neu1e = new double[vectorSize];   // 误差项

            // 层次softmax
            List<HuffmanNode> pathNeurons = word.getPathNeurons();   // 获取一个叶节点的 路径
            WordNeuron we = sentence.get(c);    // 窗口中的其他word

            // 对路径上每个节点
            for(int neuronIndex = 0; neuronIndex < pathNeurons.size() -1; neuronIndex++)
            {
                HuffmanNeuron out = (HuffmanNeuron) pathNeurons.get(neuronIndex);
                double f = 0;

                // 从隐藏层到输出层
                for (int j =0 ; j < vectorSize; j++) {
                    f += we.vector[j] * out.vector[j];
                }

                if(f <= -MAX_EXP || f >= MAX_EXP) {
                    continue;
                } else {
                    f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
                    f = expTable[(int) f];
                }

                // 'g' 是梯度乘以学习速率
                HuffmanNeuron outNext = (HuffmanNeuron) pathNeurons.get(neuronIndex);

                double g = (1 - outNext.code - f) * alpha;

                for(c = 0; c < vectorSize;c++) {
                    neu1e[c] += g * out.vector[c];
                }
                // 学习 hidden->outout 的权重
                for(c = 0 ; c < vectorSize; c++) {
                    out.vector[c] += g * we.vector[c];
                }
            }

            // 学习 input -> hidden
            for (int j = 0; j < vectorSize; j++) {
                we.vector[j] += neu1e[j];
            }

        }
    }


    private void cbowGram(int index,List<WordNeuron> sentence,int b, double alpha) {

        WordNeuron word = sentence.get(index);
        int a,c = 0;

        double[] neu1e = new double[vectorSize]; // 误差项
        double[] neu1 = new double[vectorSize];  // 误差项

        WordNeuron last_word;

        for (a = b; a < windowSize * 2 + 1 - b ; a++)
            if (a != windowSize) {
                c = index - windowSize + a;
                if(c < 0 || c >= sentence.size())
                    continue;

                last_word = sentence.get(c);
                if (last_word == null)
                    continue;
                for(c = 0; c < vectorSize;c++)
                    neu1[c] += last_word.vector[c];
            }

        //  层次softmax
        List<HuffmanNode> pathNeurons = word.getPathNeurons();
        for (int neuronIndex = 0; neuronIndex < pathNeurons.size() - 1; neuronIndex++) {
            HuffmanNeuron out = (HuffmanNeuron) pathNeurons.get(neuronIndex);

            double f = 0;
            // 隐藏层到输出层
            for(c = 0; c < vectorSize;c++)
                f += neu1[c] + out.vector[c];
            if(f <= -MAX_EXP)
                continue;
            else if(f >= MAX_EXP)
                continue;
            else
                f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];

            // g 是 梯度乘以 学习速率

            HuffmanNeuron outNext = (HuffmanNeuron) pathNeurons.get(neuronIndex);

            double g = (1 - out.code - f) * alpha;

            for(c = 0; c < vectorSize; c++) {
                neu1e[c] += g * out.vector[c];
            }

            //  学习 隐藏层到输出层的 权重
            for (c = 0 ; c < vectorSize; c ++) {
                out.vector[c] += g * neu1[c];
            }

        }

        // 计算隐藏层节点
        for(a = b; a < windowSize * 2 + 1 - b ;a ++) {
            if(a != windowSize){
                c = index - windowSize + a;
                if (c >= sentence.size())
                    continue;
                last_word = sentence.get(c);
                if(last_word == null)
                    continue;
                for(c = 0 ; c < vectorSize;c++)
                    last_word.vector[c] += neu1e[c];

            }
        }
    }

    private long nextRandom = 5;

    public class Trainer implements  Runnable {

        private BlockingQueue<LinkedList<String>> corpusQueue;    // 文集队列
        private LinkedList<String> corpusToBeTrained;         // 要被训练的 数据集
        int trainingWordCount;                                // 训练的词数
        double tempALpha ;                                    // 当前学习速率

        public Trainer(LinkedList<String> corpus) {
            corpusToBeTrained = corpus;
            trainingWordCount = 0;
        }

        public Trainer(BlockingQueue<LinkedList<String>> corpusQueue) {
            this.corpusQueue = corpusQueue;
        }

        // 计算当前的alpha
        private void computeAlpha() {
            synchronized (alphaLock) {    // 这里对学习速率加锁 ，同步更新学习速率
                currentWordCount += trainingWordCount;

                // 随着训练的 过程的进行逐渐减少 学习速率
                alpha = initialAlpha * (1 - currentWordCount / (double) (totalWordCount + 1));        // 学习速率跟新的 公式  alpha = oldapah * (1 - processedWod / (allNumber +));
                if (alpha < initialAlpha * 0.0001) {
                    alpha = initialAlpha * 0.0001;
                }

                System.out.println("alpha:" + alpha + "\tProgress: "
                        + (int) (currentWordCount / (double) (totalWordCount + 1) * 100)
                        + "%\t");
            }
        }


        private void training() {

            for(String line : corpusToBeTrained) {    // 一个line 是一篇文章的 文件名
                List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                Tokenizer tokenizer = new Tokenizer(line ," ");
                trainingWordCount += tokenizer.size();

                while(tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    WordNeuron entry = neuronMap.get(token);

                    if (entry == null) {
                        continue;
                    }

                    //   The subsampling randomly discards frequent words while keeping the ranking same
                    if (sample > 0) {
                        double ran = (Math.sqrt(entry.getFrequency() / (sample * totalWordCount)) + 1) *
                                (sample * totalWordCount) / entry.getFrequency();
                        nextRandom = nextRandom * 25214903917L + 11;

                        if (ran < (nextRandom & 0xFFFF) /  (double) 65536) {
                            continue;
                        }
                        sentence.add(entry);

                    }
                }

                for(int index = 0 ; index < sentence.size(); index++) {
                    nextRandom = nextRandom * 25214903917L + 11;
                    switch(trainMethod) {
                        case CBow:
                            cbowGram(index,sentence,(int) nextRandom % windowSize,tempALpha);
                            break;
                        case Skip_Gram:
                            skipGram(index,sentence,(int) nextRandom % windowSize,tempALpha);
                            break;
                    }


                }
            }

        }


        @Override
        public void run() {
            boolean hasCorpusToBeTrained = true;

            try{
                while(hasCorpusToBeTrained) {
                    corpusToBeTrained = corpusQueue.poll(2, TimeUnit.SECONDS);

                    if (null != corpusToBeTrained) {
                        tempALpha = alpha;
                        trainingWordCount = 0;
                        training();
                        computeAlpha();   // 更新学习速率
                    } else {
                        // 超过2s还没获得数据，认为主线程已经停止投放语料，即将停止训练。

                        hasCorpusToBeTrained = false;
                    }
                }
            }catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    // 多线程训练 驱动程序
    public void training()  {
        if(tempCorpus == null) {
            throw new NullPointerException("训练语料为空，如果之前调用了 training()"+
                    "请调用readLine(String sentence) 重新输入语料");
        }


        buildVocabulary();

        HuffmanTree.make(neuronMap.values());


        // 重新遍历语料
        totalWordCount = currentWordCount;
        currentWordCount = 0;
        // 处理线程池定义
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThread);

        LineIterator li = null;

        try{
            BlockingQueue<LinkedList<String>> corpusQueue =new ArrayBlockingQueue<LinkedList<String>>(numOfThread);

            LinkedList<Future> futures = new LinkedList<Future>();  // 每个线程的返回结果，用于等待线程

            for(int thi = 0 ; thi < numOfThread; thi++) {
                futures.add(threadPool.submit(new Trainer(corpusQueue)));
            }

            tempCorpusWriter.close();
            li = new LineIterator(new FileReader(tempCorpus));
            LinkedList<String> corpus = new LinkedList<String>();   // 若干文本组成

            int trainBlockSize = 500; // 语料中句子个数

            while(li.hasNext()) {
                corpus.add(li.nextLine());
                if(corpus.size()  == trainBlockSize) {
                    // 放进任务队列
                    corpusQueue.put(corpus);

                    corpus = new LinkedList<String>();
                }

            }

            corpusQueue.put(corpus);
            logger.info("");

            // 等待线程处理完语料
            for (Future future: futures){
                future.get();
            }

            threadPool.shutdown();   // 关闭线程池
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch (IllegalAccessException ace) {
            ace.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally{
            li.close();

            if(!tempCorpus.delete()) {

            }
            tempCorpus = null;
        }
    }



}
