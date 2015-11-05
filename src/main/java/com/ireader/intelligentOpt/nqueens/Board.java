package com.ireader.intelligentOpt.nqueens;

import java.io.Serializable;

/**
 * Created by zxsted on 15-8-20.
 */
public class Board implements Serializable,Cloneable {

    private static final long serialVersionUID = -2530321259544461828L;

    // 其潘的大小
    private int queenSize;

    // 期盼的布局
    private int[][] layout;

    public Board(int size) {
        this.queenSize = size;

        this.layout = new int[queenSize][queenSize];
        // 初始化， 是期盼的所有位置搜客用， 全部置为0

        for (int i = 0 ; i < queenSize; i++) {
            for(int j = 0; j < queenSize; j++) {
                layout[i][j] = 0;
            }
        }
    }


    public int getQueenSize() {
        return queenSize;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setQueenSize(int queenSize) {
        this.queenSize = queenSize;
    }

    public int[][] getLayout() {
        return layout;
    }

    public void setLayout(int[][] layout) {
        this.layout = layout;
    }
}
