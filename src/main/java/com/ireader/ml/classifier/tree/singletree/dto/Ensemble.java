package com.ireader.ml.classifier.tree.singletree.dto;


import com.ireader.ml.common.struct.DataPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-9-22.
 *
 * 树的组合结构： 保存了树list 和 权重list
 */
public class Ensemble {

    private List<Tree> trees = null;        // 保存所有的树结构
    private List<Double> weights =null;     // 保存每个树的权重

    private Document document = null;



    public Ensemble() {
        this.trees = new ArrayList<Tree>();
        this.weights =  new ArrayList<Double>();
    }

    public Ensemble(Ensemble e) {
        this.trees = new ArrayList<Tree>();
        this.weights = new ArrayList<Double>();

        this.trees.addAll(e.trees);
        this.weights.addAll(e.weights);
    }


    // TODO： 添加 pre函数

    /**
     *  初始化森林的文档对象， 并依次传入各个树对象中
     * */
    public void init() throws ParserConfigurationException {

        // 初始化 xml 文档解析对象
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        this.document = dBuilder.newDocument();

        for(int i = 0 ; i < this.trees.size(); i++) {
            trees.get(i).setDocument(this.document);
        }
    }

    public double predict(DataPoint dp) {

        double preval = 0.0;

        for (int i = 0 ; i < trees.size(); i++) {
            preval += weights.get(i) * trees.get(i).eval(dp);
        }

        return preval;
    }


    /**
     *  将树的组合模型以XML 的形式输出 ： 用于command 中调试
     * */
    public String  toString() {
        String strRep = "<ensemble> + “\n";
        for (int i = 0 ; i < trees.size(); i++)
        {
            strRep += "\t<tree id =\" " + (i+1) + " \"  weight=\" " + weights.get(i) + "\"> \n";
            strRep += trees.get(i).toString("\t\t");
            strRep +=  "\t </tree>" + "\n";
        }
        strRep += "</ensemble>" + "\n";
        return strRep;
    }


    /**
     *  将将森林 ensemble  转化成 xml doc对象
     *
     * */
    public Element persist() throws ParserConfigurationException {

        init();

        Element  ensemble =  document.createElement("ensemble");

        for(int i = 0 ;i < trees.size(); i++ ) {

            Element treeNode = document.createElement("tree");
            treeNode.setAttribute("id", (i + 1) + "");
            treeNode.setAttribute("weight",(i+1) + "");

            Element root = trees.get(i).getTreeDocNode(trees.get(i));
            root.setAttribute("pos","root");

            treeNode.appendChild(root);

            ensemble.appendChild(treeNode);
        }

        return ensemble;
    }

    /**
     *  将doc对象持久化到磁盘文件
     * */
    public void save(String filename) throws TransformerException, FileNotFoundException, ParserConfigurationException {


        Element ensemble = persist();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        DOMSource source = new DOMSource(ensemble);

        transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source,result);

        System.out.println("森林的组合文件生成完毕");
    }


    /**
     *  测试函数 ： 将doc 对象生成 字符串
     * */
    public String docToString(Document document) {

        String result = null;

        result = docToString(document.getDocumentElement());

        return result;
    }


    /**
     *  将doc中的一个element 转换为字符串
     * */
    public String docToString(Element node) {

        String result = null;

        if (document != null) {

            StringWriter strWtr = new StringWriter();
            StreamResult strResult = new StreamResult(strWtr);

            TransformerFactory tfac = TransformerFactory.newInstance();

            try{
                Transformer t = tfac.newTransformer();
                t.setOutputProperty(OutputKeys.ENCODING,"utf-8");
                t.setOutputProperty(OutputKeys.INDENT,"yes");
                t.setOutputProperty(OutputKeys.METHOD,"xml");

                t.transform(new DOMSource(node),strResult);

            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

            result = strResult.getWriter().toString();

            try{
                strWtr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }






    /**
     *  向组合model 中添加一个树和权重
     * */
    public void add(Tree tree,Double weight) {
        this.trees.add(tree);
        this.weights.add(weight);
    }

    /*
    * 计算整个model的输出值
    * */
    public double eval(DataPoint dp ) {
        double s = 0;
        for (int i = 0 ; i < trees.size(); i++)
            s += trees.get(i).eval(dp) * this.weights.get(i);
        return s;
    }

    public Tree getTree(int k) {
       return  this.trees.get(k);
    }

    public double getWeight(int k) {
        return this.weights.get(k);
    }

    // 获取模型中所有树的 偏差
    public double variance() {
        double var = 0.0;
        for(int i = 0 ; i < this.trees.size(); i++) {
//            var += this.trees.get(i).variance();
        }

        return var;
    }



    /**
     * 从xml 字符串中解析出树
     * */
    public void load(String xmlRep) {

        try{
            trees  = new ArrayList<Tree>();
            weights = new ArrayList<Double>();

            // 初始化 xml 文档解析对象模型
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            byte[] xmlDATA = xmlRep.getBytes();
            ByteArrayInputStream in = new ByteArrayInputStream(xmlDATA);
            Document doc = dBuilder.parse(in);   // 将文档数据解析成 Document对象

            NodeList nl = doc.getElementsByTagName("tree");
            for (int i = 0 ; i < nl.getLength();i++) {
                Node n = nl.item(i);

                if(n instanceof  Element) {

                    Tree regressionTree = new Tree();

                    // 这里使用的 是create2 因为 xml 将#text 也作为一个字节点
                    Tree tree = regressionTree.create2(n.getChildNodes().item(1));

                    double weight = Double.parseDouble(n.getAttributes().getNamedItem("weight").getNodeValue().toString());

                    trees.add(tree);
                    weights.add(weight);
                }

            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {

        Ensemble ensemble = new Ensemble();

//        Tree left_1 = new Tree("lft_1",4,null,null);
//        Tree right_1 = new Tree("rft_1",3,null,null);

        Tree left_1 = new Tree(null,4,null,null);
        Tree right_1 = new Tree(null,3,null,null);

        Tree tree1 = new Tree("ft1",3,left_1,right_1);

        ensemble.add(tree1,0.3);

//        Tree left_2 = new Tree("lft_2",4,null,null);
//        Tree right_2 = new Tree("rft_2",3,null,null);

        Tree left_2 = new Tree(null,4,null,null);
        Tree right_2 = new Tree(null,3,null,null);

        Tree tree2 = new Tree("ft2",3,left_2,right_2);

        ensemble.add(tree2,0.4);

//        Tree left_3 = new Tree("lft_3",4,null,null);
//        Tree right_3 = new Tree("rft_3",3,null,null);

        Tree left_3 = new Tree(null,4,null,null);
        Tree right_3 = new Tree(null,3,null,null);

        Tree tree3 = new Tree("ft3",3,left_3,right_3);

        ensemble.add(tree3,0.5);

//        Tree left_4 = new Tree("lft_4",4,null,null);
//        Tree right_4 = new Tree("rft_4",3,null,null);

        Tree left_4 = new Tree(null,4,null,null);
        Tree right_4 = new Tree(null,3,null,null);

        Tree tree4 = new Tree("ft4",3,left_4,right_4);

        ensemble.add(tree4,0.37);

        try {

//            System.out.println(ensemble.trees.get(i));

            ensemble.init();
            ensemble.save("/home/zxsted/data/ensembletest.xml");
            Element ens = ensemble.persist();


            // 将森林转化文字符串
            String str = ensemble.docToString(ens);

            System.out.println(str);

            // 从文件中加载 树结构
            Ensemble ensemblereload =  new Ensemble();
            ensemblereload.load(str);

            for(int i = 0 ; i < ensemblereload.trees.size(); i++) {

                // 下面是从树结构中生成的 而不是从document 对象中获取的
                System.out.println(ensemblereload.getTree(i).getFeat_id() + ":" +ensemblereload.getWeight(i) );
//                System.out.println(ensemblereload.getTree(i).toString());
                // 与上面语句的区别是 多了xml文件头
                System.out.println(ensemblereload.getTree(i).docToString());
            }

        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


    }


}
