package com.ireader.graph.local;

import java.util.*;

/**
 * Created by zxsted on 15-11-1.
 *
 * 图的工具类
 */
public class GraphUtil {

    /**
     * 图的广度优先遍历
     *  非递归遍历
     *
     * @param graph
     * @param v
     * @param checkCycle
     * @param <T>
     * @return
     */
    public static <T> List<T> bfs(Graph<T> graph,T v,boolean checkCycle) {

        Queue<T> vertexQueue = new LinkedList<T>();  // 待访问的节点队列
        List<T> vistList = new ArrayList<T>();       // 已经访问的节点

        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("没有" + v + "对应的节点！");
        }

        graph.allUnVisted();  // 全部设置为为访问
        vertexQueue.add(v);

        T curr = null;
        Set<T> neighbors = null;

        while (! vertexQueue.isEmpty()) {

            // 取出待访问 节点的队头
            curr = vertexQueue.poll();

            // TODO： 这里可以添加节点处理逻辑

            // 设置为已经访问
            graph.setState(curr, VertexState.VISITED);
            // 加入已经访问列表
            vistList.add(curr);

            neighbors = graph.getNeighbors(curr);
            for (T neighbor:neighbors) {
                VertexState state = graph.getState(neighbor);
                if (state == VertexState.UNVISITED) { // 未访问， 就加入待访问列表
                    graph.setState(neighbor,VertexState.PASSED);  // 设置为已经访问过
                    vertexQueue.add(neighbor);
                } else {
                    if (state == VertexState.PASSED && checkCycle) {
                        throw  new IllegalStateException("存在环！");
                    }
                }
            }
        }

        // 返回访问列表
        return vistList;
    }


    /**
     * 递归遍历
     * @param graph
     * @param v
     * @param checkCycle
     * @param vList
     * @param <T>
     */
    public static<T> void dfsHandler(Graph<T> graph,T v, boolean checkCycle,List vList) {
        Set<T> neighbors = null;

        if (! graph.containsVertex(v)) {
            throw new IllegalStateException("不存在该顶点！");
        }

        // TODO : 这里可以添加节点处理逻辑

        graph.setState(v, VertexState.PASSED);

        // 取出该点的所有邻接点
        neighbors = graph.getNeighbors(v);
        VertexState state = null;

        for (T neighbor : neighbors) {
            state = graph.getState(neighbor);

            // 如果没有遍历， 就将其状态设置为已经访问，然后遍历
            if (state == VertexState.UNVISITED) {
                dfsHandler(graph,neighbor,checkCycle,vList);
            } else if (state == VertexState.PASSED && checkCycle) {  // 已经访问过，存在一个环
                throw new IllegalArgumentException("存在一个环");
            }
        }

        graph.setState(v,VertexState.VISITED); // 访问结束设为已访问

        // 将该节点加入访问列表
        vList.add(v);
    }


    public static<T> List<T> dfs(Graph<T> graph,T v,boolean checkCycle) {
        graph.allUnVisted();
        List<T> vList = new ArrayList<T>();
        dfsHandler(graph, v, checkCycle, vList);

        // 返回访问列表
        return vList;
    }


    /**
     *  图所有节点的遍历
     *
     * @param graph
     * @param <T>
     * @return
     */
    public static<T> List<T> visitGraph(Graph<T> graph) {

        List<T> vlist = new ArrayList<T>();

        graph.allUnVisted();
        for (T v: graph.vertexSet()) {
            if (graph.getState(v) == VertexState.UNVISITED) {
                dfsHandler(graph,v,false,vlist);
            }
        }

        return vlist;
    }


    /**
     *  检测图是否有环
     *
     * @param graph
     * @param <T>
     * @return
     */
    public static<T> boolean checkCycle(Graph<T> graph) {

        graph.allUnVisted();
        List<T> vList = new ArrayList<T>();

        for (T v: graph.vertexSet()) {
            try {
                dfsHandler(graph,v,true,vList);
            } catch (IllegalArgumentException e) {
                return true;
            }
        }

        return false;
    }

    /**
     * 图的拓扑排序
     *
     * 可以证明在被路径p(v,w)连接的任意顶点所在的图中，对于v和w来说，v在列表中必须出现在w之前。
     * 依据这个结论可以对图的每个节点进行一个dfs的遍历，最终的节点列表就是拓扑排序的一个结果(要对这个列表反转).
     *
     * @param graph
     * @param <T>
     * @return
     */
    public static<T> List<T> topologicalSort(Graph<T> graph) {

        List<T> vList = new ArrayList<T>();
        graph.allUnVisted();

        for (T v: graph.vertexSet()) {
            if (graph.getState(v) == VertexState.UNVISITED) {
                try {
                    dfsHandler(graph,v,true,vList);
                } catch (IllegalStateException e) {
                    throw new IllegalArgumentException("图有一个环！");
                }
            }
        }

        // 广度遍历后翻转排序
        Collections.reverse(vList);

        return vList;
    }

    /**
     *  图的拓扑排序2， 该方法会改变图的结构
     *
     *  方法一:
     * (1)在又向图中选一个没有前驱的点点输出。
     * (2)从图中删除该顶点和所有以它为尾的弧。
     * 重复以上步骤，直至全部顶点均一输出，或者当前图中不存在无前驱的顶点为止。
     * 拓扑排序要求图示无环图后一种情况	说明该图存在环。
     * 在实现中，我们可以用一个队列存入所有入度为0的顶点。然后依次删除这些顶点，
     * 和其对应的边，如果对应边删除后其终点的入度减为0者也将其存入队列中，如此循环下去，
     * 直到队列为空。最后比列表中的节点数是否等于图的顶点数，如果不等者图存在一个环。
     * @param graph
     * @param <T>
     * @return
     */
    public static<T> List<T> topologicalSort2(Graph<T> graph) {

        Queue<T> vQueue = new LinkedList<T>();
        List<T> vList = new ArrayList<T>();
        Set<T> vSet = graph.vertexSet();
        Set<T> neighbors = null;
        T vertex = null;

        // 将入度为0 的节点存入队列中
        for (T v: vSet) {
            if (graph.getIndegree(v) == 0) {
                vQueue.add(v);
                vList.add(v);
            }
        }


        while(!vQueue.isEmpty()) {
            vertex = vQueue.poll();
            neighbors = graph.getNeighbors(vertex);
            for (T neighbor : neighbors) {
                graph.removeEdge(vertex,neighbor);    // 删除边
                if (graph.getIndegree(neighbor) == 0) { // 若neighbor的入度变为0，也将其加入队列中
                    vQueue.add(neighbor);
                    vList.add(neighbor);
                }
            }
        }

        if (vList.size() != graph.numberOfVertices()) {
            throw new IllegalStateException("存在环");
        }

        return vList;
    }

    /** ============== 强联通组件 ============================================= */
    /**
     * 有向图中, u可达v不一定意味着v可达u. 相互可达则属于同一个强连通分量
     * 最关键通用部分：强连通分量一定是图的深搜树的一个子树。
     *
     * 算法步骤：
     *
     * 1）对图G进行深度搜索生成森林（树）
     * 2）生成图G的转置图G’
     * 3）用1）中生成的顶点到转置图中查找相关联的节点，返回的即为一个强连通分量
     * */

    private static<T> Graph<T> transpose(Graph<T> graph) {

        Graph<T> graphT = new EGraph<T>();
        Set<T> vSet = graph.vertexSet(); //  得到顶点数
        Set<T> neighbors = null;

        // 加入顶点
        for (T v: vSet) {
            graphT.addVertex(v);
        }

        // 转向
        for (T v: vSet) {
            neighbors = graph.getNeighbors(v);

            for (T neighbor : neighbors) {
                graphT.addEdge(neighbor,v,graph.getWeight(v,neighbor));
            }
        }
        return graphT;
    }

    public static<T> List<List<T>> getStrongComponents(Graph<T> graph) {
        List<T> vList = visitGraph(graph);
        Collections.reverse(vList);
        Graph<T> graphT = transpose(graph);
        List<List<T>> components = new ArrayList<List<T>>();
        List<T> componet = null;

        graphT.allUnVisted();
        for (T v:vList) {
            if (graphT.getState(v) == VertexState.UNVISITED) {
                componet = new ArrayList<T>();
                dfsHandler(graphT,v,false,componet);
                components.add(componet);
            }
        }

        return components;
    }


}
