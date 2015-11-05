package com.ireader.intelligentOpt.nqueens;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-8-20.
 */
public class NQueens {

    private int numSolutions;   // 求解结果数量
    private int queenSize;      // 皇后的多少
    private Board board;        // 布局
    private List<Queen> queens = new ArrayList<Queen>();  // 皇后

    public NQueens() {

    }

    public NQueens(int size) {
        numSolutions = 0;
        queenSize = size;
        board = new Board(queenSize);

        for(int i = 0 ; i < queenSize;i++) {
            Queen queen = new Queen();
            queens.add(queen);
        }
    }



    public void putQueen(int index) {

        int row = index;

        // 如果这个列可用
        for(int col = 0; col < board.getQueenSize(); col++) {
            if(board.getLayout()[row][col] == 0) {
                // 姜黄后的位置置为此列位置
                queens.get(row).setPosition(col);
                // 然后将相应的位置（该列的正下方以及两个对角先设置为1，时期不可用）
                for(int i = row+1; i < board.getQueenSize();i++) {
                    board.getLayout()[i][col] ++;
                    if(row + col - i >= 0) {
                        board.getLayout()[i][row+col-i]++;
                    }

                    if(i+col-row < board.getQueenSize()) {
                        board.getLayout()[i][i+col - row]++;
                    }
                }

                if(row == board.getQueenSize() - 1) {
                    numSolutions++;
                    printSolution(numSolutions);
                } else {
                    putQueen(row+1);       // 递归进行
                }

                // 回溯， 将相应位置（此列的正下方以及两个对角线） 减去1
                for(int i = row+1; i < board.getQueenSize(); i++) {
                    board.getLayout()[i][col]--;
                    if(row+col - i >= 0) {
                        board.getLayout()[i][row+col -i]--;
                    }

                    if(i + col - row < board.getQueenSize()){
                        board.getLayout()[i][ i + col - row]--;
                    }
                }
            }
        }
    }

    //驱动
    public void solve(){
        System.out.println("Start solve ...");
        putQueen(0);
    }

    // 打印一条结果
    private void printSolution(int i) {
        System.out.println("The" + i + "solutions is:");
        for (int j = 0 ; j < board.getQueenSize();j++){
            for(int k = 0 ; k < board.getQueenSize(); k++) {
                System.out.print(queens.get(j).getPosition() == k? "*":"-");
            }
            System.out.println();
        }
        System.out.println();
    }


    public static void main(String[] args) {
        //
        NQueens  nQueens = new NQueens(8);
        nQueens.solve();
    }

}
