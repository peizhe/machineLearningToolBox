package com.ireader.graph.mr;

import org.apache.hadoop.io.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-11-2.
 *
 * ndoe class 存储了图节点的信息：
 *
 * node id，相邻节点，以及当前被访问的状态，距离等等
 */
public class Node {


    /**
     *  节点的状态
     */
    public  static enum Color {

        WHITE,      // univistied
        GRAY,       // visited ,unprocess
        BLACK       // process
    };

    private String id;        // 节点的id
    private int distance;     // 节点距源节点的距离

    private List<String> edges = new ArrayList<String>();   // 节点的邻接边
    private Color color = Color.WHITE;    // 节点初始化为未访问状态

    private String parent;     // 节点的父节点

    public Node() {
        distance = Integer.MAX_VALUE;
        color = Color.WHITE;
        parent = null;
    }

    /**
     *
     *  @param  nodeInfo : 节点信息组成的字符串
     *  样例：
     *   id \t adj | distance | state | parent
     *  1<tab>2,3|0|GRAY|source
     *  2<tab>1,3,4,5|Integer.MAX_VALUE|WHITE|null
     *  3<tab>1,4,2|Integer.MAX_VALUE|WHITE|null
     *  4<tab>2,3|Integer.MAX_VALUE|WHITE|null
     *  5<tab>2|Integer.MAX_VALUE|WHITE|null
     *
     * */
    public Node(String nodeInfo) {

        String[] inputVal = nodeInfo.split("\t");
        String key = "";
        String val = "";

        try {
            key = inputVal[0];      // node id
            val = inputVal[1];
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        String[] tokens = val.split("\\|");
        this.id = key;
        for (String s : tokens[0].split(",")) {
            if (s.length() > 0) edges.add(s);
        }

        if (tokens[1].equalsIgnoreCase("Integer.MAX_VALUE")) {
            this.distance = Integer.MAX_VALUE;
        } else {
            this.distance = Integer.parseInt(tokens[1]);
        }

        this.color = Color.valueOf(tokens[2]);
        this.parent = tokens[3];

    }

    public Text getNodeInfo() {
        StringBuilder sb = new StringBuilder();
        for (String v: edges) {
            sb.append(v).append(",");
        }

        sb.append("|");

        if (this.distance < Integer.MAX_VALUE) {
            sb.append(this.distance).append("|");
        } else {
            sb.append("Integer.MAX_VALUE").append("|");
        }

        sb.append(color.toString()).append("|");
        sb.append(getParent());

        return  new Text(sb.toString());
    }


    /** ================== getter and setter ============================================================================= */

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public List<String> getEdges() {
        return edges;
    }

    public void setEdges(List<String> edges) {
        this.edges = edges;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }







}
