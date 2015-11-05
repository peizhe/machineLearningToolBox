Aho Corasick自动机


AC自动机能高速完成多模式匹配

双数组Trie树能高速O(n)完成单串匹配，并且内存消耗可控，然而软肋在于多模式匹配，如果要匹配多个模式串，
必须先实现前缀查询，然后频繁截取文本后缀才可多匹配，这样一份文本要回退扫描多遍，性能极低。

Aho-Corasick算法简称AC算法，通过将模式串预处理为确定有限状态自动机，扫描文本一遍就能结束。其复杂度为
O(n)，即与模式串的数量和长度无关。


接口
返回所有匹配到的模式串


/**
 * 匹配母文本
 *
 * @param text 一些文本
 * @return 一个pair列表
 */
public List<Hit<V>> parseText(String text)

其中Hit是一个表示命中结果的结构：


/**
 * 一个命中结果
 *
 * @param <V>
 */
public class Hit<V>
{
    /**
     * 模式串在母文本中的起始位置
     */
    public final int begin;
    /**
     * 模式串在母文本中的终止位置
     */
    public final int end;
    /**
     * 模式串对应的值
     */
    public final V value;
}
即时处理接口

很明显，返回一个巨大的List并不是个好主意，AhoCorasickDoubleArrayTrie提供即时处理的结构：


/**
 * 处理文本
 *
 * @param text      文本
 * @param processor 处理器
 */
public void parseText(String text, IHit<V> processor)

其中IHit<V>是一个轻便的接口：


/**
 * 命中一个模式串的处理方法
 */
public interface IHit<V>
{
    /**
     * 命中一个模式串
     *
     * @param begin 模式串在母文本中的起始位置
     * @param end   模式串在母文本中的终止位置
     * @param value 模式串对应的值
     */
    void hit(int begin, int end, V value);
}
调用方法



        TreeMap<String, String> map = new TreeMap<>();
        String[] keyArray = new String[]
                {
                        "hers",
                        "his",
                        "she",
                        "he"
                };
        for (String key : keyArray)
        {
            map.put(key, key);
        }
        AhoCorasickDoubleArrayTrie<String> act = new AhoCorasickDoubleArrayTrie<>();
        act.build(map);
        act.parseText("uhers", new AhoCorasickDoubleArrayTrie.IHit<String>()
        {
            @Override
            public void hit(int begin, int end, String value)
            {
                System.out.printf("[%d:%d]=%s\n", begin, end, value);
            }
        });
        // 或者System.out.println(act.parseText("uhers"));
输出


[1:3]=he
[1:5]=hers

一些调试输出：

output:
107 : [0]
118 : [1]
120 : [2]
123 : [3, 0]
fail:
1 : 1
118 : 117
120 : 117
122 : 106
123 : 107
DoubleArrayTrie：
char =      ×    h    e     ×    i    s     s      ×    s     ×    h    e     ×
i    =      0   106   107   108   111   117   118   119   120   121   122   123   124
base =      1     5   108    -1     4    17   119    -2   121    -3    21   124    -4
check=      0     1     5   108     5     1     2   119     4   121    17    21   124