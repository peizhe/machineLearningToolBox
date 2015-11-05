package com.ireader.intelligentOpt.nqueens;

import java.io.Serializable;

/**
 * Created by zxsted on 15-8-20.
 */
public class Queen implements Serializable,Cloneable {

    private static final long serialVersionUID = 7354459222300557644L;

    // 皇后的位置
    private int position;

    public Queen() {

    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // 使用clone 来复用对象
    public Object Clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
