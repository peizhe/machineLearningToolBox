package com.ireader.graph.local;

import java.util.Set;

/**
 * Created by zxsted on 15-10-31.
 */
public interface Graph<T> {

    /**
     * 顶点数
     * @return
     */
    public int numberOfVertices();

    /**
     * 边数
     * @return
     */
    public int numberOfEdges();

    /**
     * 测试图是否为空
     * @return
     */
    public boolean isEmpty();

    /**
     * 得到v1 v1的权值
     * @param v1
     * @param v2
     * @return
     */
    public int getWeight(T v1, T v2);

    /**
     * 设置v1 v2 的权值
     * @param v1
     * @param v2
     * @param w
     * @return
     */
    public int setWeight(T v1, T v2, int w);

    /**
     * 得到v的邻节点
     * @param v
     * @return
     */
    public Set<T> getNeighbors(T v);

    /**
     * 添加边
     * @param v1
     * @param v2
     * @param w
     * @return
     */
    public boolean addEdge(T v1, T v2, int w);

    /**
     * 添加顶点
     * @param v
     * @return
     */
    public boolean addVertex(T v);

    /**
     * 删除v1 与 v2之间的边
     * @param v1
     * @param v2
     * @return
     */
    public boolean removeEdge(T v1, T v2);

    /**
     * 删除顶点v
     * @param v
     * @return
     */
    public boolean removeVertex(T v);

    /**
     * 清空
     */
    public void clear();

    /**
     * 返回顶点集
     * @return
     */
    public Set<T> vertexSet();

    /**
     * 返回是否包含顶点v
     * @param v
     * @return
     */
    public boolean containsVertex(T v);

    /**
     * 返回是否包含v1 到 v2 的边
     * @param v1
     * @param v2
     * @return
     */
    public boolean containsEdge(T v1, T v2);
    /**
     * 把图节点全部设为非访问
     */
    public void allUnVisted();
    /**
     * 获取节点状态
     * @param v
     * @return
     */
    public VertexState getState(T v);
    /**
     * 设置节点状态,并返回先前的值
     * @param v
     * @param state
     * @return
     */
    public VertexState setState(T v,VertexState state);
    /**
     * 求顶点的入度
     * @param v
     * @return
     */
    public int getIndegree(T v);
}
