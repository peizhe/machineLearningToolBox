
BFS(G, s)
  for each vertex u ∈ V [G] - {s}
       do color[u] ← WHITE
          d[u] ← ∞
          π[u] ← NIL
  //除了源顶点s之外，第1-4行置每个顶点为白色，置每个顶点u的d[u]为无穷大，
  //置每个顶点的父母为NIL。
  color[s] ← GRAY
  //第8行，将源顶点s置为灰色，这是因为在过程开始时，源顶点已被发现。
  d[s] ← 0       //将d[s]初始化为0。
  π[s] ← NIL     //将源顶点的父顶点置为NIL。
  Q ← Ø
  ENQUEUE(Q, s)                  //入队
  //第12、13行，初始化队列Q，使其仅含源顶点s。
  while Q ≠ Ø
      do u ← DEQUEUE(Q)    //出队
  //第16行，确定队列头部Q头部的灰色顶点u，并将其从Q中去掉。
         for each v ∈ Adj[u]        //for循环考察u的邻接表中的每个顶点v
             do if color[v] = WHITE
                   then color[v] ← GRAY     //置为灰色
                        d[v] ← d[u] + 1     //距离被置为d[u]+1
                        π[v] ← u            //u记为该顶点的父母
                        ENQUEUE(Q, v)        //插入队列中
         color[u] ← BLACK      //u 置为黑色