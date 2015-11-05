package com.ireader.ml.classifier.tree.singletree.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zxsted on 15-10-25.
 *
 * 决策树一个节点对应的决策规则(从根节点一直到叶子节点)：
 * 即该节点的路径上的所有解测条件的列表
 *
 * 一条规则的形式为 ：
 *
 *  fid，value，left&fid，value，left&fid，value，right:label
 *
 */
public class Rule {


    // conditions 的key 是属性ID （从1 开始编号），value 是该属性的取值
    // 一个condition 的格式 为 fid，value，left  或者为 fid，value，right
    public List<String> conditions = new ArrayList<String>();

    // 如果是叶子节点那么label为叶子节点代表的值，如果为非叶子节点，那么是空
    public Double preval = Double.NaN;

    /**
     * 将该条规则以字符串的格式输出
     *fid，value，left&fid，value，left&fid，value，right:label
     * */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (String condition : conditions) {
            sb.append(condition + "&");
        }

        // 将末尾的& 替换为 ： 分隔符
        sb.setCharAt(sb.length() - 1, ':');

        // 输出label值
        sb.append(this.preval);

        return sb.toString();
    }

    /**
     *  根据一行文本解析出对象
      */
    public static Rule parse(String source) {

        Rule rule = new Rule();

        if(source.length() <= 1)
            return null;

        // 没有条件只有label
        if(source.charAt(0) == ':' && source.length() > 1) {
            rule.preval = Double.parseDouble(source.split(":")[1]);
            return rule;
        }

        String conditionPart = source.split(":")[0];

        for (String condition : conditionPart.split("&")){
            rule.conditions.add(condition);
        }

        // 如果有类标签则读取标签
        if (source.split(":").length == 2) {
            rule.preval = Double.parseDouble(source.split(":")[1]);
        }

        return rule;
    }


    /**
     *  判断 特征id 是否在 Rule
     * */
    public Boolean contains(int fid) {

        boolean isContain = false;

        for (String cond : conditions) {
            Integer curfid = Integer.parseInt(cond.split(",")[0].trim());
            if (fid == curfid) isContain = true;
        }

        return isContain;
    }

}
