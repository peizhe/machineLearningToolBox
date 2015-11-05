矩阵分块乘法的实现：

问题描述：
 M*N 的矩阵分成 s*t  N*P矩阵分成  t*v


则 AB = { sum_t {Ast*Bt*v}}


输入数据的格式：

<M><i><j><m_ij>   M 为矩阵名称， i,j为M的元素的下标   m_ij 为 矩阵M的元素

输入样例：

A,0,1,1.0
A,0,2,2.0
A,0,3,3.0
A,0,4,4.0
A,1,0,5.0
A,1,1,6.0
A,1,2,7.0
A,1,3,8.0
A,1,4,9.0
B,0,1,1.0
B,0,2,2.0
B,1,0,3.0
B,1,1,4.0
B,1,2,5.0
B,2,0,6.0
B,2,1,7.0
B,2,2,8.0
B,3,0,9.0
B,3,1,10.0
B,3,2,11.0
B,4,0,12.0
B,4,1,13.0
B,4,2,14.0

输出结果：

<i><j><m_ij>

0,0,90.0
0,1,100.0
0,2,110.0
1,0,240.0
1,1,275.0
1,2,310.0



伪代码：

矩阵AB 的分块惩罚分为两步MR：

第一步 MR：

map(key,value):
    // value is ("A",i,j,a_ij) or ("B",j,k,b_jk)
    if value[0] == "A":
        i = value[1]
        j = value[2]
        a_ij = value[3]

        for k_per_v = 0 to p/v - 1:
            emit((i/s,j/t,k_per_v),("A",i%s,j%t,a_ij))         // 子矩阵索引 ，为每个 B子矩阵 分发一个A子句镇

     else
        j = value[1]
        k = value[2]
        b_jk = value[3]
        for i_per_s = 0 to m/s - 1:
            emit((i_per_s,j/t,k/v),("B",j%t,k%v,b_jk))

reduce(key,value):
    // key is (i_per_s,j_per_t,k_per_v)
    // values is a list of ("A",i_mod_s,j_mod_t,a_ij)  and ("B",j_mod_t,k_mod_v,b_jk)
    list_A = [(i_mod_s,j_mod_t,a_ij) for (M,i_mod_s,j_mod_t,a_ij) in values if M == "A"]
    list_B = [(j_mod_t,k_mod_v,b_jk) for (M,j_mod_t,k_mod_v,b_jk) in values if M == "B"]

    hash = {}

    for a in listA:
        for b in listB:
            if a[1] == b[0] : a.j_mod_t == b.j_mod_t
                hash[(a[0],b[1])] += a[2]*b[2]

    for {(i_mod_s,k_mod_v):v} in hash:
        emit((key[0]*s + i_mod_s,key[2]*v + k_mod_v),v)

第2步 MR：

map(key,value):
    emit(key,value)

reduce(key,value):
    result = 0
    for value in values:
        result += value
    emit(key ,result)

M*N的矩阵A分割成s*t, N*P的矩阵B分割成t*v，


Input

<M><i><j><m_ij>，M为矩阵，i， j为矩阵M的元素下标，m_ij为矩阵M行列下标为i，j的非0元素

A,0,1,1.0
A,0,2,2.0
A,0,3,3.0
A,0,4,4.0
A,1,0,5.0
A,1,1,6.0
A,1,2,7.0
A,1,3,8.0
A,1,4,9.0
B,0,1,1.0
B,0,2,2.0
B,1,0,3.0
B,1,1,4.0
B,1,2,5.0
B,2,0,6.0
B,2,1,7.0
B,2,2,8.0
B,3,0,9.0
B,3,1,10.0
B,3,2,11.0
B,4,0,12.0
B,4,1,13.0
B,4,2,14.0


Output

<i><j><m_ij>，结果矩阵行列下标i， j，以及对应的值m_ij

上述样例数据的数出结果为:

派生到我的代码片

    0,0,90.0
    0,1,100.0
    0,2,110.0
    1,0,240.0
    1,1,275.0
    1,2,310.0

Pseudocode

矩阵AB的分块乘法计算分两步map-reduce进行

第一步

    map(key, value):
        // value is ("A", i, j, a_ij) or ("B", j, k, b_jk)
        if value[0] == "A":
            i = value[1]
            j = value[2]
            a_ij = value[3]
            for k_per_v = 0 to p/v - 1:
                emit((i/s, j/t, k_per_v), ("A", i%s, j%t, a_ij))
        else:
            j = value[1]
            k = value[2]
            b_jk = value[3]
            for i_per_s = 0 to m/s - 1:
                emit((i_per_s, j/t, k/v), ("B", j%t, k%v, b_jk))

    reduce(key, values):
        // key is (i_per_s, j_per_t, k_per_v)
        // values is a list of ("A", i_mod_s, j_mod_t, a_ij) and ("B", j_mod_t, k_mod_v, b_jk)
        list_A = [(i_mod_s, j_mod_t, a_ij) for (M, i_mod_s, j_mod_t, a_ij) in values if M == "A"]
        list_B = [(j_mod_t, k_mod_v, b_jk) for (M, j_mod_t, k_mod_v, b_jk) in values if M == "B"]
        hash = {}
        for a in list_A:                                        # 子矩阵内相乘
            for b in list_B:
                if a[1] == b[0]: // a.j_mod_t == b.j_mod_t
                    hash[(a[0], b[1])] += a[2]*b[2]
        for {(i_mod_s, k_mod_v): v} in hash:
            emit((key[0]*s + i_mod_s, key[2]*v + k_mod_v), v)


第二步：

    map(key, value):
        emit(key, value)

    reduce(key, values):    # 子矩阵外部相乘
        result = 0
        for value in values:
            result += value
        emit(key, result)


分析：

上述第一步map-reduce过程中：

1、reduce job的数目 = 所有发送给reduce任务的unique keys 的数目，即(m*n*p) / (s*t*v)
2、矩阵A发送给每个reduce元素的个数为st
3、矩阵B发送给每个reduce元素的个数为tv

计算开销：

1、The communication cost of the first Map tasks is
        O（mn + np）

2、The communication cost of the first Reduce tasks is
        O( mnp/v + mnp/s)
3、The communication cost of the second Map tasks is
        O(mnp/t)
4、The communication cost of the second Reduce tasks is
        O(mnp/t)



















