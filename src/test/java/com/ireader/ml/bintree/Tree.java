package com.ireader.ml.bintree;

import org.glassfish.grizzly.streams.BufferedInput;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zxsted on 15-10-14.
 *
 * 树的测试代码
 *
 */
public class Tree<D extends  Comparable>  {

    static final int MAXLEN = 20;       // 最大长度

    static class Node<T extends Comparable> {    // 节点结构
        T data;

        Node left;
        Node right;
    }

    private Node<D> root = null;

    public  Tree(D rdata) throws IOException {
        this.root =  init(rdata);
    }

    /** 初始化二叉树 */
    public  Node init(D rdata) throws IOException {
        Node node;

        if ((node = new Node()) != null)    // 申请根内存
        {
            System.out.println("input root data");
//            String datastr = new BufferedReader(new InputStreamReader(System.in)).readLine();
            node.data = 0;
            node.left = null;
            node.right = null;

            if(node != null) {
                return  node;
            } else {
                return null;
            }
        }

        return null;
    }

    /** 添加节点 */
    public  Node<D> addNone( D data) {


        Node<D> parNode = findParent(this.root,data);

        Node childNonde = new Node();
        childNonde.data = data;

        if(data.compareTo(parNode.data) < 0) {
            parNode.left = childNonde;
        } else if (data.compareTo(parNode.data) < 0) {
            parNode.right = childNonde;
        } else {
            childNonde = parNode;
        }

        return childNonde;
    }

    /** 寻找待插入的父节点 */
    public Node<D> findParent(Node<D> node , D data)  {

        D ndata = node.data;

        if(ndata.compareTo(data) < 0 && node.left != null )
            findParent(node.left,data);
        else if (ndata.compareTo(data) < 0 && node.left == null )
            return node;
        else if (ndata.compareTo(data) > 0 && node.right != null)
            findParent(node.right ,data);
        else if (ndata.compareTo(data) > 0 && node.right == null)
            return node;
        else if (ndata.compareTo(data) == 0)
            return node;

        return null;
    }




    /** 判断是否是叶子节点 */
    public boolean isleaf(Node<D> curNode) {

        if (curNode.left == null &&  curNode.right == null) {
            return true;
        } else {
            return false;
        }
    }


    /**寻找数据所在的叶子节点*/
    public Node<D> findLeaf( Node<D> curNode,D data) {

        if (isleaf(curNode)) return curNode;
        else if(curNode.data.compareTo(data) < 0 && curNode.left != null) {
           return  findLeaf(curNode.left,data);
        } else if  (curNode.data.compareTo(data) >= 0 && curNode.right != null) {
            return findLeaf(curNode.right,data);
        }
        return null;
    }







}
