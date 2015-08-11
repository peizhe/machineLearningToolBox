package com.ireader.nlp.word2vec.util;

import java.util.*;

/**
 * Created by zxsted on 15-7-29.
 *
 * 根据词频构建一棵哈夫曼树
 */
public class HuffmanTree  {

    public static void make(Collection<? extends HuffmanNode> nodes) {

        TreeSet<HuffmanNode> tree = new TreeSet<HuffmanNode>(nodes);

        while (tree.size() > 1) {
            HuffmanNode left = tree.pollFirst();
            HuffmanNode right = tree.pollFirst();
            HuffmanNode parent = left.merge(right);   // 将两个节点融合成父节点
            tree.add(parent);
        }
    }


    /**
     * 获取根节点到叶节点的路径
     * */
    public static List<HuffmanNode> getPath(HuffmanNode leafNode) {

        List<HuffmanNode> nodes = new ArrayList<HuffmanNode>();

        for (HuffmanNode hn = leafNode; hn != null;hn.getParent()) {
            nodes.add(hn);
        }

        Collections.reverse(nodes);

        return nodes;
    }

}
