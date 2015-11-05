package com.ireader.graph.mr.bfs;


import com.ireader.graph.mr.Node;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.jobhistory.ReduceAttemptFinished;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by zxsted on 15-11-2.
 */
public class TraverseGraph {

    /**
     * Mapper class：
     * input：
     *   key :    value : nodeID<tab>list_of_adjacent_nodes|distance_from_the_source|color|parent>
     *
     * output：
     *   key： nodeId  value :  (updated) list_of_adjacent_nodes|distance_from_the_source|color|parent node
     * */
    public static class TravereMapper extends Mapper<Object,Text,Text,Text> {

        protected void map(Object key,Text value,Context context,Node inNode)
                                                throws IOException,InterruptedException{

            // gray ： 当前节点被访问，但是没有被处理过
            if (inNode.getColor() == Node.Color.GRAY) {
                // 遍历当前节点的所有邻接 节点
                for (String neighbor : inNode.getEdges()) {
                    Node adjacentNode = new Node();
                    adjacentNode.setId(neighbor);
                    adjacentNode.setDistance(inNode.getDistance() + 1);
                    adjacentNode.setColor(Node.Color.GRAY);
                    adjacentNode.setParent(inNode.getId());

                    // 将当前邻接节点 输出

                    context.write(new Text(adjacentNode.getId()) , adjacentNode.getNodeInfo());
                }

                // 当前节点已经处理完了 , 将其设置为黑色
                inNode.setColor(Node.Color.BLACK);
            }

            // 将当前节点也输出， reducer 进行融合
            context.write(new Text(inNode.getId()),inNode.getNodeInfo());

        }
    }

    /**
     *  Reducer class
     *
     *  融合节点节点，
     *  新的节点应该 有： 1. 整个图的关于该节点的所有邻接边，
     *                  2. 到源节点的最短的距离
     *                  3. 最深的颜色，即最高的状态
     *                  4. 最优的父节点
     * input:
     *  key :  nodeId  value : list_of_adjacent_nodes|distance_from_the_source|color|parent_node
     * */
    public static class TracerseReducer extends Reducer<Text,Text,Text,Text> {

        protected Node reduce(Text key, Iterable<Text> values, Context context,Node outNode)
                                                    throws IOException,InterruptedException {

            // 取出节点的 id ，作为输出 key
            outNode.setId(key.toString());

            for (Text value : values) {
                // 将 节点String 封装为Node
                Node inNode = new Node(key.toString() + "\t" + value.toString());

                // 将当前节点的所有 邻接点放置在新节点的邻接列表中
                if (inNode.getEdges().size() > 0) {
                    outNode.setEdges(inNode.getEdges());
                }

                // 设置最短的距离，以及相应的 parent
                if (inNode.getDistance() < outNode.getDistance()) {
                    outNode.setDistance(inNode.getDistance());
                    outNode.setParent(inNode.getParent());
                }

                // 最高的状态
                if (inNode.getColor().ordinal() > outNode.getColor().ordinal()) {
                    outNode.setColor(inNode.getColor());
                }
            }

            // 将该节点输出
            context.write(key,new Text(outNode.getNodeInfo()));

            // 返回输出节点， 子类进行进一步的处理
            return outNode;
        }
    }

}
