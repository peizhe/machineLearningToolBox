package com.ireader.ml.classifier.tree.singletree.dto;

//import com.ireader.ml.classifier.tree.singletree.dto.Rule;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.ireader.ml.common.struct.DataPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;


/**
 * Created by zxsted on 15-9-22.
 *
 * http://blog.163.com/lintingte@126/blog/static/482595432011101931728265/
 * http://blog.csdn.net/zhao19861029/article/details/8473245
 * http://www.cnblogs.com/wangchenyang/archive/2011/08/23/2150530.html
 *
 *
 * 树的功能类
 *
 */
public class Tree  {

    private Document document = null;   // 用于持久化


    private  double pre_val;            // 如果是分支节点， 那么存储的是判别值， 如果是叶子节点 ， 这里保存的叶子输出值

    protected String feat_id;           // 本节点的名字， 也就是特征id ： 即使是字符串也是整数转成的这一点需要注意
    protected Tree left = null;                // 左子节点
    protected Tree right = null;               // 右子节点



    public Tree(String feat_id, double threshold,Tree left,Tree right ) {
        this.feat_id = feat_id;
        this.left = left;
        this.right = right;
        this.pre_val = threshold;
    }

    public Tree(){}


    /**
     *   根据一个规则列表生成树结构
     * */
    public Tree(List<Rule> rules) {

        for (Rule rule : rules) {

            List<String> conditions = rule.conditions;

            Tree node = this;    // 节点的指针,从根节点开始遍历

            for (String condition : conditions) {

                String[] fields = condition.split(",");
                String fid = fields[0];
                String value = fields[1];
                String child = fields[2];

                if(node.getFeat_id() == null) {

                    node.feat_id = fid;
                    node.setPre_val(Double.parseDouble(value.trim()));

                    if (child.trim().equalsIgnoreCase("left")) {
                        node.left = new Tree();
                        node = node.left;
                    } else if(child.trim().equalsIgnoreCase("right")) {
                        node.right = new Tree();
                        node = node.left;
                    }

                } if(node.getFeat_id().equalsIgnoreCase(fid.trim())) {

                    /**因为是二叉树 ， 此时分割阈值已经确定 所以直接 根据 child 的类型向下遍历即可*/

                    if (child.trim().equalsIgnoreCase("left")) {

                        if (node.left == null)
                            node.left = new Tree();

                        node = node.left;
                    } else if (child.trim().equalsIgnoreCase("right")) {

                        if (node.right == null)
                            node.right = new Tree();

                        node = node.right;
                    }
                }
            }

//            if (!rule.label.equalsIgnoreCase("")) {
//                node.setPre_val(Double.parseDouble(rule.label.trim()));
//            }

            if (!Double.isNaN(rule.preval)) {
                node.setPre_val(rule.preval.doubleValue());
            }
        }

    }



    /**
     *  预测函数
     * */
    public double eval(DataPoint dp) {

        double ret_val = 0.0;

        // 取出特征索引
        String fidstr = this.getFeat_id();
        int fid = Integer.parseInt(fidstr);

        // 取出 节点 阈值
        double threshold = this.getPre_val();

        // 如果是叶子节点直接输出 预测值
        if(this.left == null && this.right == null) {
            ret_val =  this.getPre_val();
        }

        // 如果 是字节点那么递归调用子树
        if (dp.getFeatures().getValue().get(fid) <= threshold && this.left != null) {
            ret_val = this.left.eval(dp);
        } else if (dp.getFeatures().getValue().get(fid)  > threshold && this.right != null){
            ret_val =  this.right.eval(dp);
        }
        return ret_val;

    }




    /**
     * 初始化 xml文档构架对象， 如果 持有的document对象为空那么 为其生成一个
     * */
    public void init() {

        if (this.document == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                this.document = builder.newDocument();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 给树对象传入已实例化的文档的对象
     * */
    public void setDocument(Document doc) {
        this.document = doc;
    }


    /**
     *  根据 xml 的一个节点创建树
     *
     *  @param n : param 一个 xml文档节点。
     * */
    public Tree create2(Node n) {

        Tree treeNode = null;

        if(n.getChildNodes().item(1).getNodeName().compareToIgnoreCase("feature") == 0) {

            NodeList nl = n.getChildNodes();
            String fid = nl.item(1).getFirstChild().getNodeValue().toString().trim();
//            System.out.println(fid);
            double threshold = Double.parseDouble(nl.item(3).getFirstChild().getNodeValue().toString().trim());
            treeNode = new Tree();
            treeNode.setFeat_id(fid);
            treeNode.setPre_val(threshold);
            treeNode.setLeft(create2(nl.item(5)));
            treeNode.setRight(create2(nl.item(7)));

        } else {    // 否则是一个叶子节点
            double threshold = Double.parseDouble(n.getChildNodes().item(1).getFirstChild().getNodeValue().toString().trim());
            treeNode = new Tree();
            treeNode.setPre_val(threshold);
        }

        return treeNode;

    }




    /**
     *  将 tNode 代表的子树 转化成 Element 对象， 用于持久化
     *  @param  tNode ： Tree 一个树节点
     * */
    public Element getTreeDocNode( Tree tNode) {
        init();

        Element tree = document.createElement("node");

        // 如果是叶子节点 , 输出预测值
        if(tNode.getLeft() == null && tNode.getRight() == null)
        {
            Element output = document.createElement("output");
            output.appendChild(document.createTextNode(tNode.getPre_val() + ""));
            tree.appendChild(output);
        } else    // 输出分支节点
        {

            Element feature = document.createElement("feature");
            feature.appendChild(document.createTextNode(tNode.getFeat_id()));

            Element threshod = document.createElement("threshold");
            threshod.appendChild(document.createTextNode(tNode.getPre_val()+""));

            tree.appendChild(feature);
            tree.appendChild(threshod);


            // 左子节点
//            Element leftNode = document.createElement("left");
            Element leftNode = getTreeDocNode(tNode.getLeft());
//            leftNode.appendChild(left);
            leftNode.setAttribute("pos","left");
            tree.appendChild(leftNode);

            // 右子节点
//            Element rightNode = document.createElement("right");
            Element rightNode = getTreeDocNode(tNode.getRight());
//            rightNode.appendChild(right);
            rightNode.setAttribute("pos","right");
            tree.appendChild(rightNode);

        }
        return tree;
    }


    /**
     *  测试用函数
     *
     *  将当前的树保存到指定的文件中
     *
     *  @param filename ：  持久化文件名
     * */
    public void pesit(String filename) throws TransformerException, FileNotFoundException {

        init();

        Element root = getTreeDocNode(this);

        Element treeNode = this.document.createElement("node");
        treeNode.appendChild(root);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        DOMSource source = new DOMSource(treeNode);

        transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source,result);
        System.out.println("树结构文件生成完毕！");
    }


    /**
     *  (测试用函数)
     *  将 dom 文件转换为字符串的形式输出
     * */
    public String docToString() {

        Element node = getTreeDocNode( this);
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



    /** ========================= 这个是 用于显示树结构的的输出函数 方便测试  ========================================= */

    public String toString()
    {
        return toString("");     // 调用下面的 输出函数
    }


    public String toString(String indent)
    {
        String strOutput = indent + "<node>" + "\n";
        strOutput += getString(indent + "\t");
        strOutput += indent + "</node>"+"\n";
        return strOutput;
    }

    public String getString(String indent)
    {
        String strOutput = "";
        //如果是叶子节点，则输出预测值
        if(this.left == null && this.right == null)
        {
//			strOutput += indent + "<output>" + avgLabel + " </output>" + "\n";
            strOutput += indent + "<output>" + this.pre_val + "</output>" + "\n";

        }else    //输出 分支节点
        {
            strOutput += indent + "<feature>" + this.feat_id + "</feature>" + "\n";
            strOutput += indent + "<threshold>" + this.pre_val + "</threshold>" + "\n";
            //输出左子节点
            strOutput += indent + "<node pos = \"left\">" + "\n";
            strOutput += left.getString(indent + "\t");
            strOutput += indent + "</node>" + "\n";
            //输出右子节点
            strOutput += indent + "<node pos =\"right\">" + "\n";
            strOutput += right.getString(indent + "\t");
            strOutput += indent + "</node>" + "\n";
        }
        return strOutput;
    }





    /** ========================  getter and setter =============================================================== */


    public String getFeat_id() {
        return this.feat_id;
    }

    public void setFeat_id(String feat_id) {
        this.feat_id = feat_id;
    }

    public double getPre_val() {
        return pre_val;
    }


    public void setPre_val(double pre_val) {
        this.pre_val = pre_val;
    }

    public Tree getLeft() {
        return left;
    }

    public void setLeft(Tree left) {
        this.left = left;
    }

    public Tree getRight() {
        return right;
    }

    public void setRight(Tree right) {
        this.right = right;
    }


    /** ==================== test main ================================================================== */




    public static void main(String[] args) {

        Tree tree = new Tree();

        Tree left = new Tree("left",5,null,null);
        Tree right = new Tree("right",6,new Tree("left",4,null,null),new Tree("right",9,null,null));
        Tree node = new Tree("test",4,left,right);

        System.out.println(node.toString());

    }
}




















