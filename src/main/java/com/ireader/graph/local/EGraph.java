package com.ireader.graph.local;

import java.util.*;

/**
 * Created by zxsted on 15-10-31.
 */
public class EGraph<T> implements   Graph<T>{

    private Map<T,Integer> verMap;           // 顶点对应的下标
    private List<VertexInfo<T>> vInfos;      // 顶点数组
    private Stack<Integer> vertextCache;     // 顶点缓存， 当删除一个顶点，并没有真正删除，将其下表存入vertexCache中
    private int edgeNum;                     // 边数

    private Set<T> graphVertexSet;           //

    public EGraph() {
        verMap = new HashMap<T,Integer>();
        vInfos = new ArrayList<VertexInfo<T>>();
        vertextCache = new Stack<Integer>();
        edgeNum = 0;
    }


    /**
     *  添加一个定点
     *
     * @param v
     * @return
     */
    public boolean addVertex(T v) {

        int index;

        // 已经存在的情况
        if (verMap.containsKey(v)) {
            return false;
        }

        /** 定点缓存不为空， 从中选择一个定点坐标不需要再创建一个对象*/
        if (!vertextCache.isEmpty()) {
            index = vertextCache.pop();
            // 取出可用的VertexInfo
            VertexInfo<T> vertexInfo = vInfos.get(index);
            // 将当前节点信息对象的 定点id更新为新加入的点的id
            vertexInfo.vertex = v;
            // 清理工作： 将以前的邻接信息清空
            vertexInfo.edgeList.clear();
            vertexInfo.inDegree = 0;
            vertexInfo.occupied = true;
        } else {
            index = vInfos.size();
            vInfos.add(new VertexInfo<T>(v));
        }

        verMap.put(v,index);

        return true;
    }

    /**
     *  返回v对应的定点在 vInfos 中的位置
     *
     * @param v
     * @return
     */
    private int getVertexInfoIndex(T v) {
        Integer index = verMap.get(v);
        return index == null ? -1:index;
    }


    /**
     * 返回v对应顶点所关联的邻结点
     *
     * @param v
     * @return
     */
    public Set<T> getNeighbors(T v) {

        // 取出当前节点的 index
        int index = getVertexInfoIndex(v);

        if (index == -1) {
            return null;
        }

        Set<T> edgeSet = new HashSet<T>();

        /** 取出该节点的 info */
        VertexInfo<T> vertexInfo = vInfos.get(index);
        Edge edge = null;

        /** 从 info 中取出 所有的边，然后取出边的dest ，添加如邻接 set */
        for (Iterator iterator = vertexInfo.edgeList.iterator(); iterator.hasNext();) {
            Edge e = (Edge) iterator.next();
            edgeSet.add(vInfos.get(e.dest).vertex);
        }

        return edgeSet;
    }


    /**
     * 添加从V1 到 V2 一条边
     * @param v1
     * @param v2
     * @param weight
     * @return
     */
    public boolean addEdge(T v1,T v2,int weight) {

        int post1 = getVertexInfoIndex(v1);
        int post2 = getVertexInfoIndex(v2);

        // 如果两个节点中至少一个不存在， 那么返回false
        if (post1 == -1 || post2 == -1) {
            throw new IllegalArgumentException("没有对应的节点！");
        }

        // 如果两个节点是同一个节点
        if (post1 == post2) {
            throw  new IllegalArgumentException("起始点和结束点不能相同！");
        }

        Edge edge = new Edge(post1 ,weight);
        VertexInfo<T> vertexInfo1 = vInfos.get(post1);

        if (!vertexInfo1.edgeList.contains(edge)) { //如果不存在v1 - v2 者加入
            vertexInfo1.edgeList.add(edge);
            // 更新 节点的度和图的边数
            vInfos.get(post2).inDegree++;
            edgeNum++;
        } else {
            return  false;
        }
        return true;

    }

    /**
     * 删除对应的边
     *
     * @param v1
     * @param v2
     * @return
     */
    public boolean removeEdge(T v1,T v2) {

        int post1 = getVertexInfoIndex(v1);
        int post2 = getVertexInfoIndex(v2);

        if (post1 == -1 || post2 == -1) {
            throw  new IllegalArgumentException("不存在这条边！");
        }

        Edge edge = null;

        for (Iterator iterator = vInfos.get(post1).edgeList.iterator();iterator.hasNext();) {
            edge = (Edge) iterator.next();

            if (edge.dest == post2) {
                iterator.remove();
                vInfos.get(post2).inDegree--;
                edgeNum--;
                return true;
            }
        }

        return false;
    }

    /**
     * 删除对应的顶点
     *
     * @param v
     * @return
     */
    public boolean removeVertex(T v) {
        Integer index = verMap.get(v);

        if (index == null) { // 不存在该顶点
            return false;
        }

        verMap.remove(v);

        // 删除该节点，并调整图结构
        removeFixup(index);

        return true;
    }

    private void removeFixup(int index) {

        Edge edge = null;

        // 1. 获取该索引对应的vertexInfo 对象， 将其 ooccupied 属性设置为false
        VertexInfo<T> vertexInfo = vInfos.get(index);

        vertexInfo.occupied = false;
        vertextCache.push(index);

        // 2. 删除以该顶点为终点的所有边
        int len = vInfos.size();
        VertexInfo<T> vertexInfo2 = null;

        for (int i = 0 ;i < len; i++) {
            vertexInfo2 = vInfos.get(i);
            if (vertexInfo2.occupied) { //v所对应的顶点已经为false
                for (Iterator iterator = vertexInfo2.edgeList.iterator();iterator.hasNext();) {
                    edge = (Edge) iterator.next();

                    if (edge.dest == index) {
                        iterator.remove();
                        edgeNum--;  // 边数减1
                        break;
                    }
                }
            }
        }

        // 3 删除所有从 V 出发的边
        edgeNum -= vertexInfo.edgeList.size();
        for (Iterator iterator = vertexInfo.edgeList.iterator();iterator.hasNext();) {
            edge = (Edge) iterator.next();
            vertexInfo2 = vInfos.get(edge.dest);
            vertexInfo.inDegree--;
            iterator.remove();
        }

    }


    /**
     * 判断图 是否为空
     * @return
     */
    public boolean isEmpty() {
        return verMap.size() == 0;
    }

    /**
     *  返回图中的边数
     * @return
     */
    public int numberOfEdges() {
        return edgeNum;
    }

    /**
     * 返回图中的定点
     * @return
     */
    public int numberOfVertices() {
        return verMap.size();
    }


    /**
     * 是否包含边
     *
     * @param v1
     * @param v2
     * @return
     */
    public boolean containsEdge(T v1,T v2) {

        int post1 = getVertexInfoIndex(v1);
        int post2 = getVertexInfoIndex(v2);

        if (post1 == -1 || post2 == -1) {
            throw new IllegalArgumentException("参数错误！");
        }

        VertexInfo<T> vertexInfo = vInfos.get(post1);

        Edge edge = null;

        for (Iterator iterator = vertexInfo.edgeList.iterator(); iterator.hasNext();) {
            edge = (Edge) iterator.next();

            if (edge.dest == post2) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否包含此顶点
     *
     * @param v
     * @return
     */
    public boolean containsVertex(T v) {
        return verMap.containsKey(v);
    }

    /**
     * 返回 v1 与 v2 的权值
     *
     * @param v1
     * @param v2
     * @return
     */
    public int getWeight(T v1,T v2) {

        Edge edge = getEdge(v1, v2);

        if (edge == null) {
            return -1;
        }

        return edge.weight;
    }

    private Edge getEdge(T v1,T v2) {

        int post1 = getVertexInfoIndex(v1);
        int post2 = getVertexInfoIndex(v2);

        if (post1 == -1 || post2 == -1) {
            throw new IllegalArgumentException("参数错误！");
        }

        VertexInfo<T> vertexInfo = vInfos.get(post1);

        Edge edge = null;
        for (Iterator iterator = vertexInfo.edgeList.iterator();iterator.hasNext();) {
            edge = (Edge) iterator.next();
            if (edge.dest == post2) {
                return edge;
            }
        }

        return null;
    }

    /**
     * 更新权值， 返回 -1 更新失败，更新成功返回原来的值
     *
     * @param v1
     * @param v2
     * @param weight
     * @return
     */
    public int setWeight(T v1,T v2,int weight) {

        Edge edge = getEdge(v1,v2);

        if (edge == null) {
            return -1;
        }

        int preWeight = edge.weight;
        edge.weight = weight;
        return preWeight;
    }

    public void clear() {

    }


    public Set<T> vertexSet() {

        if (graphVertexSet == null) {

            graphVertexSet = new Set<T>() {

                @Override
                public boolean add(T e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean addAll(Collection<? extends T> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    EGraph.this.clear();

                }

                @Override
                public boolean contains(Object o) {
                    return verMap.containsKey(o);
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                        Object object = (Object) iterator.next();
                        if(!verMap.containsKey(object)){
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean isEmpty() {
                    return verMap.size() == 0;
                }

                @Override
                public Iterator<T> iterator() {
                    // TODO Auto-generated method stub
                    return new IteratorImpl();
                }

                @Override
                public boolean remove(Object o) {
                    if(verMap.containsKey(o)){
                        removeVertex((T) o);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                        Object object = (Object) iterator.next();
                        if(!remove(object)){
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public int size() {
                    // TODO Auto-generated method stub
                    return verMap.size();
                }

                @Override
                public Object[] toArray() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    // TODO Auto-generated method stub
                    return null;
                }

            };
        }

        return graphVertexSet;
    }



    private class IteratorImpl implements Iterator<T> {

        private Iterator<T> iter;
        T lastValue = null;
        public IteratorImpl () {
            iter = verMap.keySet().iterator();
        }

        @Override
        public boolean hasNext() {

            return iter.hasNext();
        }

        @Override
        public T next() {
            lastValue = iter.next();
            return lastValue;

        }

        @Override
        public void remove() {
            if(lastValue == null){
                throw new IllegalStateException("还未调用next()方法，不可调用remove()方法");
            }
            int index = getVertexInfoIndex(lastValue);
            iter.remove();
            //清理工作
            removeFixup(index);
        }

    }


    /**
     *  获取状态
     *
     * @param v
     * @return
     */
    public VertexState getState(T v) {

        int post = getVertexInfoIndex(v);
        if (post == -1) {
            throw new IllegalArgumentException("参数错误！");
        }
        return vInfos.get(post).state;
    }

    /**
     *  全部节点设置为未访问
     */
    public void allUnVisted() {

        VertexInfo<T> vertexInfo = null;

        int len = vInfos.size();

        for (int i = 0 ; i < len; i++) {
            vertexInfo = vInfos.get(i);

            if (vertexInfo.state != VertexState.UNVISITED) {
                vertexInfo.state = VertexState.UNVISITED;
            }
        }
    }

    /**
     * 设置节点的状态
     *
     * @param v
     * @param state
     * @return
     */
    public VertexState setState(T v,VertexState state) {

        int post = getVertexInfoIndex(v);

        if (post == -1) {
            throw new IllegalArgumentException("参数错误！");
        }

        VertexInfo<T> vertex  = vInfos.get(post);
        VertexState preState = vertex.state;

        vertex.state = state;
        return preState;
    }


    /**
     * 获取入度
     *
     * @param v
     * @return
     */
    public int getIndegree(T v) {

        int index = getVertexInfoIndex(v);

        if (index == -1) {
            throw new IllegalArgumentException("参数错误！");
        }

        return vInfos.get(index).inDegree;
    }


}



/**
 *  定点信息类
 *
 *  每个定点 要关联与他 有链接的定点
 *
 * */
class VertexInfo<T> {

    public T vertex ;             // 顶点信息
    public List<Edge> edgeList;   // 关联节点
    public int inDegree;          // 定点入度
    public int edgeNum;           // 定点数


    public VertexState state;     // 定点状态
    public int dateValue;
    public T parent;              // 父节点

    public boolean occupied;      // 时候修改标识

    public VertexInfo(T vertex) {
        this.vertex = vertex;
        edgeList = new LinkedList<Edge>();
        inDegree = 0;
        occupied = true;
    }

}


/**
 *  图的边类
 *
 *  保存的数据属性有
 *   1. 边权重
 *   2. 目标顶点
 * */
class Edge {

    public int dest;      // 定点在数组中的位置
    public int weight;    // 权重


    public Edge(int dest, int weight) {
        this.dest = dest;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    /**  比较  */
    public boolean equals(Object obj) {

        if (!(obj instanceof Edge))
            return false;

        return ((Edge) obj).dest == this.dest;
    }
}
