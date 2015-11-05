双数组Tire树

双数组Trie树(DoubleArrayTrie)是一种空间复杂度低的Trie树，应用于字符区间大的语言（如中文、日文等）分词领域。

    双数组Trie (Double-Array Trie)结构由日本人JUN-ICHI AOE于1989年提出的，是Trie结构的压缩形式，仅用两个
线性数组来表示Trie树，该结构有效结合了数字搜索树(Digital Search Tree)检索时间高效的特点和链式表示的Trie空间
结构紧凑的特点。双数组Trie的本质是一个确定有限状态自动机（DFA），每个节点代表自动机的一个状态，根据变量不同，进
行状态转移，当到达结束状态或无法转移时，完成一次查询操作。在双数组所有键中包含的字符之间的联系都是通过简单的数学
加法运算表示，不仅提高了检索速度，而且省去了链式结构中使用的大量指针，节省了存储空间。

                                                           ——《基于双数组Ｔｒｉｅ树算法的字典改进和实现》

双数组Trie树归根结底还是属于Trie树，所以免不了有一颗树的构造过程。不过这棵树并没有保存下来，而是边构造树边维护双数组，
双数组的信息足以表示整棵树。

1. 首先建立一个空的root节点：

Node{code=0, depth=0, left=0, right=6}

其中code指的是字符的编码，在Java中是双字节，depth是深度，left及right表示这个节点在字典中的索引范围。

2. 然后按照字典序插入所有的字串节点：


使用了两个数组base和check来维护Trie树，它们的下标以及值都代表着一个确定的状态。
base储存当前的状态以供状态转移使用，
check验证字串是否由同一个状态转移而来并且当check为负的时候代表字串结束。

假定有字符串状态s,当前字符串状态为t，假定t加了一个字符c就等于状态tc，加了一个字符x等于状态tx，那么有

base[t] + c = base[tc]

base[t] + x = base[tx]

check[tc] = check[tx]


在每个节点插入的过程中会修改这两个数组，具体说来：

1、初始化root节点base[0] = 1; check[0] = 0;

2、对于每一群兄弟节点，寻找一个begin值使得check[begin + a1…an]  == 0，
   也就是找到了n个空闲空间,a1…an是siblings中的n个节点对应的code。

  int pos = siblings.get(0).code;
        while (true)
        {
            pos++;
            begin = pos - siblings.get(0).code; // 当前位置离第一个兄弟节点的距离
            ……
        }

3、然后将这群兄弟节点的check设为check[begin + a1…an] = begin;很显然，
   叶子节点i的check[i]的值一定等于i，因为它是兄弟节点中的第一个，并且它的code为0

   check[begin + siblings.get(i).code] = begin;

4、接着对每个兄弟节点，如果它没有孩子，也就是上图除root外的绿色节点（叶子节点），
   令其base为负值；否则为该节点的子节点的插入位置（也就是begin值），同时插入子节
   点（迭代跳转到步骤2）。

   if (fetch(siblings.get(i), new_siblings) == 0)  // 无子节点，也就是叶子节点，代表一个词的终止且不为其他词的前缀
               {
                   base[begin + siblings.get(i).code] = -siblings.get(i).left - 1;
                   ……
               }
               else
               {
                   int h = insert(new_siblings);   // dfs
                   base[begin + siblings.get(i).code] = h;
               }


前缀查询

定义当前状态p = base[0] = 1。按照字符串char的顺序walk：

如果base[p] == check[p] && base[p] < 0 则查到一个词；

然后状态转移，增加一个字符  p = base[char[i-1]] + char[i] + 1 。加1是为了与null节点区分开。

如果转移后base[char[i-1]] == check[base[char[i-1]] + char[i] + 1]，
那么下次p就从base[base[char[i-1]] + char[i] + 1]开始。


基于双数组Trie树的Aho Corasick自动机

双数组Trie树能高速O(n)完成单串匹配，并且内存消耗可控，然而软肋在于多模式匹配，如果要匹配多个模式串，
必须先实现前缀查询，然后频繁截取文本后缀才可多匹配，这样一份文本要回退扫描多遍，性能极低。

AC自动机能高速完成多模式匹配，然而具体实现聪明与否决定最终性能高低。大部分实现都是一个Map<Character, State>
了事，无论是TreeMap的对数复杂度，还是HashMap的巨额空间复杂度与哈希函数的性能消耗，都会降低整体性能。

如果能用双数组Trie树表达AC自动机，就能集合两者的优点，得到一种近乎完美的数据结构。