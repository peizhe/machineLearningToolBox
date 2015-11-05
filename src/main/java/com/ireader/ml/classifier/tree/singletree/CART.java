package com.ireader.ml.classifier.tree.singletree;

import com.ireader.ml.classifier.tree.GBDT;
import com.ireader.ml.classifier.tree.singletree.dto.NodeStaticInfo;
import com.ireader.ml.classifier.tree.singletree.dto.Rule;
import com.ireader.ml.classifier.tree.singletree.dto.StatisticRecord;
import com.ireader.ml.classifier.tree.singletree.dto.Tree;
import com.ireader.ml.classifier.tree.singletree.mr.DecisionTree;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by zxsted on 15-10-25.
 *
 *  单棵树的生成算法
 */
public class CART {

    // 属性取值的值域范围，数据结构的第一维代表属性ID，
    // 第二维列表表示该属性的所有可能离散阈值。
    // 这里面不包含Label属性
    static List<double[]> attributeRange;

    // 记录当前决策树的模型，确定决策规则
    static List<Rule> model = new LinkedList<Rule>();

    static Configuration conf = null;

    private int regionNum;                     // 特征值的划分区间个数
    private int maxleafnodeNum ;               // 树的叶子节点个数总数
    private int maxTreeDepth;                  // 树的深度
    private double cha_threshold = 0.00001;    // 误差差值阈值

    String[] directList = new String[]{"left","right"};


    /**
     * CART 的训练函数
     *
     * @param attributeMetaInfoPath 存储数据属性元信息的文件的路径
     * @param dataSetPath           训练数据集文件所在的路径
     * @param rundir                模型储存文件
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public  Tree DTtrain(String attributeMetaInfoPath,String dataSetPath,String rundir,int regionNum,
                         int maxleafnodeNum,int maxTreeDepth,double cha_threshold,Configuration conf) throws IOException, InterruptedException, ClassNotFoundException {

        this.conf = conf;
        this.regionNum = regionNum;               // 特征值的划分区间个数
        this.maxleafnodeNum = maxleafnodeNum ;    // 树的叶子节点个数总数
        this.maxTreeDepth = maxTreeDepth;         // 树的深度
        this.cha_threshold = cha_threshold;       // 误差差值阈值

        FileSystem fs = FileSystem.get(conf);


        // 当前统计结果所在的路径
        String staticFilePath = rundir + "/static";

        // 当前层节点队列文件的路径
        String nodeRuleQueueFilePath = rundir + "/queue";


        // 加载属性元信息
        loadAttributeRange(attributeMetaInfoPath, this.regionNum);

        // 将统计信息存储到HDFS 中
        List<String> featRnages = new ArrayList<String>();

        for (double[] arr : this.attributeRange) {
            StringBuffer sb = new StringBuffer();
            for (double val : arr) {
                sb.append(val+",");
            }
            String line = sb.toString();
            line = line.substring(0,line.length()-1);
            featRnages.add(line);
        }

        GBDT.saveListToHDFS(featRnages,rundir+"/featRange",conf);
        conf.set("DT_FEAT",rundir+"/featRange");


        // 当前层的划分规则队列
        Queue<Rule> currentQueue = null;

        // 下一层的划分规则队列
        Queue<Rule> newQueue = new LinkedList<Rule>();

        // 是否包含Root 节点， 用于甄别是否是起始执行
        boolean hasRootNode = false;

        int iterateCount = 0;


        Map<Integer,NodeStaticInfo> nodeStaticInfos =
                new HashMap<Integer,NodeStaticInfo>();

        do {

            // 增加一轮迭代
            iterateCount++;

            // 准备当前轮迭代的环境变量
            String queueFilePath = nodeRuleQueueFilePath+"/queue-"+iterateCount;
            String newstaticFilePath = staticFilePath + "/static-" + iterateCount;

            // 将newQueue中保存的当前轮迭代所在的层的节点信息写入文件中
            outputNodeRuleQueueToFile(newQueue,queueFilePath);
            conf.set("DT_RULE",queueFilePath);

            // 将当前队列的指针指向上一轮迭代的输出
            currentQueue = newQueue;

            // 判断是否有根节点， 对于根节点需要单独处理
            if (!hasRootNode) {
                // 向 Queue 中插入一个空白的节点，作为根节点
                Rule rule = new Rule();
                currentQueue.add(rule);
                hasRootNode = true;
            }

            // 判断一下当前层数上是否有新的节点可以生长
            if (currentQueue.isEmpty()) {
                // 判断一下当前层上是否有新的节点可以生长
                // 退出while 循环结构
                break;
            }

            // 从输出结果中读取统计好的信息
            nodeStaticInfos.clear();    // 先将之前的 map 清空
            loadStaticInfo(nodeStaticInfos, newstaticFilePath);


            if (this.maxleafnodeNum!=0 && iterateCount >= this.maxTreeDepth ) {
                break;
            }

            if (this.maxleafnodeNum!=0 && currentQueue.size() > this.maxleafnodeNum) {
                break;
            }

            // 继续运行， 说明当前层还有节点可供生长
            runMapReduceJob(conf,dataSetPath,queueFilePath,newstaticFilePath,iterateCount);



            // 对当前的每个节点进行处理
            int i = 0;
            int Qlength = currentQueue.size();

            for (i = 0 ; i < Qlength; i++) {
                // 取出一个节点
                Rule rule = currentQueue.poll();

                // 节点统计信息应该包含1 到 |Q| 这|Q| 个规则
                // 节点编号从1开始， 所以是i+1
                assert (nodeStaticInfos.containsKey(new Integer(i+1)));

                NodeStaticInfo info = nodeStaticInfos.get(new Integer(i+1));
                double nodepreval = info.getPreValue();

                // 寻找最优的分割点
                splitInfo sinfo =  findBestSplit(rule,info);
                System.out.println("BEST_SPLIT_AID : " + sinfo.aid + "BEST_SPLIT_VALUE : " + sinfo.val);

                // 如果无法找到最佳分裂属性，即属性分裂不能够导致误差减小
                // 那么就停止构建新的子节点
                if (sinfo == null) {
                    Rule newRule = new Rule();
                    newRule.conditions = new ArrayList<String>(rule.conditions);
                    newRule.preval = nodepreval;
                    model.add(newRule);
                    continue;   // 继续处理下一个当前层的节点
                }
                // 分裂当前节点
                String[] directList = new String[]{"left","right"};
                for (String direct : this.directList) {

                    // 判断分裂的是否是叶子节点， 返回 的是叶子节点的预测值，
                    // 如果不是叶子节点，那么返回 预测值
                    Double leafval = satisfyLeafNodeCondition(rule,info,sinfo.aid,sinfo.val,direct);

                    // 增加规则
                    Rule newRule = new Rule();
                    newRule.conditions = new ArrayList<String>(rule.conditions);
                    newRule.conditions.add(sinfo.aid+"," + sinfo.val+ ","+ direct );

                    // 如果预测值不为 null，那么 是叶子节点
                    if (leafval != null) {
                        newRule.preval = leafval.doubleValue();
                        // 将叶子节点加入model中
                        model.add(newRule);

                        System.out.println("NEW RULE FOR LABEL:" + leafval + "/" + model.size());
                    } else {

                        newRule.preval = Double.NaN;
                        newQueue.add(newRule);
                    }
                }
            }

            System.out.println("NEW QUEUE SIZE:" + newQueue.size());

        } while(true);  // 不断的向深层扩展决策树，直到无法继续构建

        // 循环结束时， 队列不为空，那么将 所有的规则整合成叶子节点规则输出
        if(nodeStaticInfos.size()!=0) {

            /** ============= 将当前的节点转存到有序列表与ruleList 对齐 ========================================= */
            List<Rule> ruleList = new ArrayList<Rule>();
            for (int i = 0 ; i < currentQueue.size();i++) {
                    ruleList.add(currentQueue.poll());
            }

            // 将当前节点的平均值作为将当前节点的规则的label
            for ( NodeStaticInfo ninfo : nodeStaticInfos.values()) {
                List<StatisticRecord> currecordList=  ninfo.getRecords(new Integer(0));
                StatisticRecord record = currecordList.get(0);
                int nodeid = record.nid;

                double curPreValue = ninfo.getPreValue();
                Rule curRule = ruleList.get(nodeid-1);
                curRule.preval = curPreValue;

                model.add(curRule);
            }
        }

        Tree tree =  saveModel(model);

        return tree;
    }

    /**
     *  执行 mr 统计job
     * */
    private static boolean  runMapReduceJob(Configuration conf,String dataSePath, String nodeRuleQueueFilePath,
                                        String statisticFilePath,int itCount) throws IOException, ClassNotFoundException, InterruptedException {

        // 配置全局队列， 将规则文件加入cache
        // 在实现时， 可以放入HDFS中
        conf.set("nodeRuleQueueFile", nodeRuleQueueFilePath);
        System.err.println("NODE_RULE_URI:" + new Path(nodeRuleQueueFilePath).toUri());

        Job job = new Job(conf,"DTstatic:" +itCount );
        job.setJarByClass(CART.class);

        // 设置Mapper
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setMapperClass(DecisionTree.DecisionTreeMapper.class);

        // 设置combiner
        job.setCombinerClass(DecisionTree.DecisionTreeConsumer.class);

        // 设置reducer
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(DecisionTree.DecisionTreeReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(dataSePath));
        FileOutputFormat.setOutputPath(job, new Path(statisticFilePath));

        return job.waitForCompletion(true);
    }

    /**
     *  判断新增加的子节点是否是叶子节点
     * */
    private static Double satisfyLeafNodeCondition(Rule rule, NodeStaticInfo info,
                                                   int splitAid,double value,String direct) {


        // CASE1: 如果新节点不包含任何有效的新元组
        if (info.getRecords(splitAid,value,direct).size() == 0) {
            return value;
        }

        // CASE2: 新节点具有的所有的属性
        HashSet<Integer> usedAttributes = new HashSet<Integer>();

        for (String condition : rule.conditions) {
            String[] fields = condition.split(",");
            Integer aid = Integer.parseInt(fields[0]);
            usedAttributes.add(aid);
        }

        if (usedAttributes.size() == attributeRange.size()) {
            List<StatisticRecord> recordList = info.getRecords(splitAid,value,direct);
            return info.getPreValue(recordList);
        }

        //  不满足终止条件
        return null;
    }

    /**
     * 从输出结果文件中读取统计信息 存储到info中
     *
     *  @param info  Map<Integer,NodeStaticInfo> 用于存储各个节点的统计信息
     *  @param filePath 统计结果文件所在路径
     *
     *          统计结果文件是由若干行组成，每一行都符合如下的格式约定 "KEY \t COUNT"
     *          其中KEY中指定了统计的项目，具有如下的格式“nid#aid,属性值,label”。
     *          nid是节点临时编号（从1开始），aid是属性编号（从1开始），label是类别标签。 COUNT则指符合条件的元组个数。
     *
     */
    public static void loadStaticInfo(Map<Integer,NodeStaticInfo> info,String filePath) throws IOException {

        // 从HDFS 中加载数据
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] files = fs.listStatus(new Path(filePath));
        Path[] paths = new Path[files.length];

        for (int i = 0 ; i < 0; i++) {
            paths[i] = files[i].getPath();
        }

        // 遍历统计信息文件夹
        for (Path path : paths) {

            // 跳过 HDFS 上的系统统计文件， 这些文件一般以下划线开始
            if (path.getName().startsWith("_"))
                continue;

            FSDataInputStream inputStream = fs.open(path);
            Scanner scanner = new Scanner(inputStream);

            while(scanner.hasNext()) {

                String line = scanner.nextLine();

                if (line.length() == 0)
                    break;

                /** 解析当前 line 的各个field 封装成 StaticRecord 中 */
                String key = line.split("\t")[0];
                String value = line.split("\t")[1];
                String[] valiterms = value.split(",");
                String[] keyfields = key.split("#")[1].split(",");

                Integer nid =  Integer.parseInt(key.split("#")[0]);
                int aid = Integer.parseInt(keyfields[0]);
                double aval = Double.parseDouble(keyfields[1]);
                String direct = keyfields[2];

                double square = Double.parseDouble(valiterms[0]);
                double mean = Double.parseDouble(valiterms[1]);
                int count = Integer.parseInt(valiterms[2]);

                // 将读取到的记录插入已有的数据结构
                if (!info.containsKey(nid)) {
                    info.put(nid,new NodeStaticInfo());
                }

                /**
                 * nInfo 中封装了 一个map ：
                 *    key 是 aid
                 *    value： 是一个StaticRecord  的list
                 * */
                NodeStaticInfo nInfo = info.get(nid);

                // 如果aid之前没有出现过，则插入新的
                if (nInfo.getAttributeStaticRecords(aid) == null) {
                    nInfo.insertAttributeStaticRecords(aid,new LinkedList<StatisticRecord>());
                }

                List<StatisticRecord> recordList = nInfo.getAttributeStaticRecords(aid);
                StatisticRecord record = new StatisticRecord(nid,aid,aval,direct,square,mean,count);

                recordList.add(record);
            }

            scanner.close();
            inputStream.close();
        }
    }

    /**
     *  将队列中的字节点规则信息输出到文件中
     * */
    static void outputNodeRuleQueueToFile(Queue<Rule> queue,String filePath) throws IOException {
        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);

        FSDataOutputStream ostream = fs.create(path);
        PrintWriter printWriter = new PrintWriter(ostream);

        // 输出队列中的信息
        for (Rule rule : queue) {
            printWriter.println(rule.toString());
        }

        printWriter.close();
        ostream.close();
    }


    /**
     *  划分信息类
     *  封装属性： aid ： 特征id
     *            val：  该特征的特征值
     */
    static class splitInfo {

        public splitInfo(Integer aid,Double val) {
            this.aid = aid;
            this.val = val;
        }
        public Integer aid ;
        public Double val;
    }


    /**
     * 根据rule和统计信息，找到最佳分裂属性， 如果没有好的能带来信息增益的属性，则返回0
     * */
     splitInfo findBestSplit(Rule rule,NodeStaticInfo info) {

        Integer Dsize = new Integer(0);

        double maxCha = 0.0;
        Integer bestSplitAID = null;
        Double bestSplitVal = null;
        Integer ADSize = new Integer(0);   //

        // 遍历当前节点中每一个可能的候选属性
        for (Integer aid : info.getAvailabelAIDSet()) {

            System.out.println("---" + aid + "---");

            // 计算 属性 aid 的各个划分值的方差差值
            for (double value : attributeRange.get(aid.intValue())) {

                double curcha = calcMSE(info.getRecords(aid,value));
                assert (!Double.isInfinite(curcha) && !Double.isNaN(curcha));

                if (maxCha < curcha) {
                    maxCha = curcha;
                    bestSplitAID = aid;
                    bestSplitVal = value;
                }

            }
        } // for aid end


        if (maxCha < this.cha_threshold )
            return null;
        if(bestSplitAID == null && bestSplitVal == null)
            return null;

        return new splitInfo(bestSplitAID,bestSplitVal) ;
    }

    /**
     * 计算最方差的差值
     *
     * @param records :
     *                List<StatiticRecord> : 一个节点的 一个 aid 的 属性值 的一个列表
     *                   List 中包含 left right
     * */
    static double calcMSE(List<StatisticRecord> records) {

        double cha = 0.0;

        double all = 0.0;
        double square = 0.0;
        double mean = 0.0;

        double left = 0.0;
        double right = 0.0;


        int count = 0;
        for (StatisticRecord record : records) {

            double curmean = record.mean;
            double cursquare = record.squre;

            count += record.count;
            square += cursquare * record.count;
            mean += curmean * record.count;

            String direct = record.direction;

            // 该特征值进行数据集切分的[误差]
            // 计算左右节点的 方差 使用公式 var = E(x^2) - (E(x))^2
            if (direct.equalsIgnoreCase("left")) {
                left = cursquare - Math.pow(curmean,2);

            } else if (direct.equalsIgnoreCase("right")) {
                right = cursquare - Math.pow(curmean,2);
            }

        }

        square = square / count;
        mean = mean / count;

        // 数据集的[误差]
        // 使用公式 var = E(x^2) - (E(x))^2
        all = square - mean * mean;

        // 计算划分后与划分之前的 误差的差值
        cha = all - (left + right);

        /**
         *  注意这里实际上包含了 两个终止条件
         *
         *  当  left data size or right data size  is 0
         *  cha = 0
         *  当 best split - s = 0 时，
         *  cha = 0
         * */

        return cha;
    }



    /**
     *  从文件中加载各个属性的属性范围，并根据 指定的分段个数计算候选划分点
     *
     *  @param num :  各个属性的划分区间个数
     * */
    static void loadAttributeRange(String filepath,int num) throws IOException {

        attributeRange = new ArrayList<double[]>();

        Configuration conf = new Configuration();

        FileSystem fs  =FileSystem.get(conf);

        Path path = new Path(filepath);
        FSDataInputStream iStream = fs.open(path);
        Scanner scanner = new Scanner(iStream);

        Map<Integer,double[]> tempdict = new HashMap<Integer,double[]>();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            double[] range = new double[num-1];   // 存储 min max 之间的分割值

            int    aid = Integer.parseInt(line.split("\t")[0].trim());
            double max = Double.parseDouble(line.split("\t")[1].split(",")[0].trim());
            double min = Double.parseDouble(line.split("\t")[1].split(",")[1].trim());

            double step = (max - min) / num;

            for (int i=1 ; i < num; i++) {
                range[i] = min + step * i;
            }

            tempdict.put(aid,range);
        }

        scanner.close();
        iStream.close();

        for (int i = 0 ; i < tempdict.size() + 1; i++ ) {
            attributeRange.add(tempdict.get(i));
        }

    }


    /**
     * 根据据 model 的所有规则生成 树结构对象
     *
     * @param model
     * @return
     */
    static Tree saveModel(List<Rule> model) {

        return new Tree(model);
    }



    /** =================== getter and setter ============================================================== */

    public static List<double[]> getAttributeRange() {
        return attributeRange;
    }

    public CART setAttributeRange(List<double[]> attributeRange) {
        CART.attributeRange = attributeRange;
        return this;
    }

    public int getRegionNum() {
        return regionNum;
    }

    public CART setRegionNum(int regionNum) {
        this.regionNum = regionNum;
        return this;
    }

    public int getMaxleafnodeNum() {
        return maxleafnodeNum;
    }

    public CART setMaxleafnodeNum(int maxleafnodeNum) {
        this.maxleafnodeNum = maxleafnodeNum;
        return this;
    }

    public int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    public CART setMaxTreeDepth(int maxTreeDepth) {
        this.maxTreeDepth = maxTreeDepth;
        return this;
    }

    public double getCha_threshold() {
        return cha_threshold;
    }

    public CART setCha_threshold(double cha_threshold) {
        this.cha_threshold = cha_threshold;
        return this;
    }
}
