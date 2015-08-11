package com.ireader.ml.classifier.tree.singletree.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zxsted on 15-8-6.
 *
 * 决策树的一个节点对应的决策规则
 */
public class Rule {

    // 该决策树规则包含的所有的条件
    // conditions 的key 是属性ID（从1 开始编号），value 是该属性取值
    // 如果conditions 包括 {<1,middle>,<3,no>} 就表示
    // "属性1 == middle && 属性3 == no" 这个条件
    public Map<Integer,String> conditions = new HashMap<Integer,String>();

    public String label = "";

    // 将该规则输出， 输出的数据格式如下：
    // "condition:label" ，即在满足 condition 时， 该规则输出label
    // condition 按照下面的格式组织：aid1，avalue1&aid2,avalue2&...
    // aid1 表示条件中第一个属性的id， avalue1表示在该属性上的取值
    // 多个条件之间是用'&'分割，
    // 上述条件表示了 (aid1 == avalue)&&(aid2 == avalue2) 这个条件
    // 注意这里不需要强调顺序
    public String toString(){
        StringBuilder str = new StringBuilder();
        // 输出条件部分s
        for(Integer aid: conditions.keySet()){
            str.append(aid.toString() +"," + conditions.get(aid)+"&");
        }

        // 将str 末尾的&替换为label分隔符":"
        str.setCharAt(str.length() - 1,':');
        // 输出label
        str.append(this.label);
        return str.toString();
     }

    // 根据一行文本解析出规则对象
    // 输入文本的格式见 toString() 函数的注释
    public static Rule parse(String source) {
        Rule rule = new Rule();
        if(source.length() <= 1)
            return null;
        if(source.charAt(0) == ':' && source.length() > 1) {
            // 说明没有条件，只有标签
            rule.label = source.split(":")[1];
            return rule;
        }

        String conditionPart = source.split(":")[0];
        for (String condition:conditionPart.split("\\&")) {
            String aid = condition.split("\\,")[0];
            String value = condition.split("\\,")[1];
            rule.conditions.put(new Integer(aid),value);
        }

        // 如果有类标签则读取类标签
        if(source.split(":").length == 2){
            rule.label = source.split(":")[1];
        }
        return rule;
    }

}






















