package com.ireader.intelligentOpt.ant.tsp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zxsted on 15-8-19.
 *
 * 蚁群类
 */
public class AOC {

    private Ant[] ants ;          // 蚂蚁
    private int antNum;           // 蚂蚁数目
    private int cityNum;          // 城市数量
    private int MAX_GEN;          // 运行迭代数目
    private float[][] pheromone;  // 信息素矩阵
    private int[][] distance;     // 距离矩阵
    private int bestLength ;      // 最佳长度
    private int[] bestTour ;      // 最佳路径


    // 三个超参数
    private float alpha;
    private float beta;
    private float rho;

    public AOC()
    {

    }


    //
    public AOC(int n,int m,int g, float a,float b,float r) {
        cityNum = n;
        antNum = m;
        ants = new Ant[antNum];
        MAX_GEN = g;
        alpha = a;
        beta = b;
        rho = r;
    }



    /**
     *  初始化ACO 算法类
     *  文件中存储 所有城市节点坐标数据
     * */
    private void init(String filename) throws IOException {
        // 读取数据
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));

        distance = new int[cityNum][cityNum];
        x = new int[cityNum];
        y = new int[cityNum];

        for(int i = 0 ; i < cityNum; i++) {
            strbuff = data.readLine();
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);
            y[i] = Integer.valueOf(strcol[2]);
        }

        // 计算距离矩阵，针对问题具体计算方法不一样 此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
        for(int i =0 ; i < cityNum; i++) {
            distance[i][i] = 0;  // 对角线为0
            for(int j = i + 1; i < cityNum; j++) {
                double rij = Math.sqrt(((x[i]-x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i]-y[j])) / 10.0);
                int tij = (int) Math.round(rij);
                if(tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                }else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }

        distance[cityNum - 1][cityNum - 1] = 0;


        // 初始化信息素 矩阵
        pheromone = new float[cityNum][cityNum];
        for(int i = 0; i < cityNum; i++)
        {
            for(int j = 0; j < cityNum; j++) {
                pheromone[i][j] = 0.1f;  // 初始化为0.1f
            }
        }

        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum+1];

        // 随机安方蚂蚁
        for(int i = 0 ; i < antNum; i++) {
            ants[i] = new Ant(cityNum);
            ants[i].init(distance,alpha,beta);
        }
    }


    // 驱动一群
    public void solve(){

        for(int g = 0 ; g < MAX_GEN;g++) {        //  遍历 代数
            for(int i = 0 ; i < antNum; i++){     //  对于每个蚂蚁分别计算
                for(int j = 1; j < cityNum; j++) {     // 遍历城市
                    ants[i].selectNextCity(pheromone);   // 根据信息素矩阵选择下一个方向
                }
                ants[i].getTabu().add(ants[i].getFirstCity());
                if(ants[i].getTourLength() < bestLength) {
                    bestLength = ants[i].getTourLength();
                    for(int k = 0; k < cityNum+1; k++ ) {
                        bestTour[k] = ants[i].getTabu().get(k).intValue();
                    }
                }

                for(int j = 0 ; j < cityNum; j++) {
                    // 为每个蚂蚁更新 信息素 变化矩阵
                    ants[i].getDelta()[ants[i].getTabu().get(j).intValue()][ants[i].getTabu().get(j+1).intValue()] =
                            (float) (1./ants[i].getTourLength());
                    ants[i].getDelta()[ants[i].getTabu().get(j+1).intValue()][ants[i].getTabu().get(j).intValue()] =
                            (float) (1./ants[i].getTourLength());

                }
            }

            updatePheromone();   // 更新 总的信息素 矩阵

            //重新初始化蚂蚁
            for(int i = 0 ; i < antNum; i++) {
                ants[i].init(distance,alpha,beta);
            }
        }

        // 打印最佳结果
        printOptimal();
    }

    // 更新信息素
    private void updatePheromone(){
        // 信息诉挥发
        for(int i = 0 ; i < cityNum; i++)
            for(int j = 0 ; j < cityNum ; j++)
                pheromone[i][j] = pheromone[i][j] * (1 - rho);
        // 信息素 更新
        for(int i = 0 ; i < cityNum; i++) {
            for(int j = 0 ; j < cityNum; j++) {
                for(int k = 0 ; k < antNum; k++) {
                    pheromone[i][j] += ants[k].getDelta()[i][j];
                }
            }
        }
    }

    // 打印最优结果
    private void printOptimal(){
        System.out.println("The optimal length is:" + bestLength);
        System.out.println("The optimal tour is:");
        for(int i = 0 ; i < cityNum+1; i++ ){
            System.out.println(bestTour[i]);
        }
    }

    /*=========================driver test========================================================*/

    public static void main(String[] args) throws IOException {
        AOC aco = new AOC(48,100,1000,1.f,5.f,0.5f);
        aco.init("c://data.txt");
        aco.solve();
    }



    /*=========================getter and setter====================================================*/


    public Ant[] getAnts() {
        return ants;
    }

    public void setAnts(Ant[] ants) {
        this.ants = ants;
    }

    public int getAntNum() {
        return antNum;
    }

    public void setAntNum(int antNum) {
        this.antNum = antNum;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getMAX_GEN() {
        return MAX_GEN;
    }

    public void setMAX_GEN(int MAX_GEN) {
        this.MAX_GEN = MAX_GEN;
    }

    public float[][] getPheromone() {
        return pheromone;
    }

    public void setPheromone(float[][] pheromone) {
        this.pheromone = pheromone;
    }

    public int[][] getDistance() {
        return distance;
    }

    public void setDistance(int[][] distance) {
        this.distance = distance;
    }

    public int getBestLength() {
        return bestLength;
    }

    public void setBestLength(int bestLength) {
        this.bestLength = bestLength;
    }

    public int[] getBestTour() {
        return bestTour;
    }

    public void setBestTour(int[] bestTour) {
        this.bestTour = bestTour;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getBeta() {
        return beta;
    }

    public void setBeta(float beta) {
        this.beta = beta;
    }

    public float getRho() {
        return rho;
    }

    public void setRho(float rho) {
        this.rho = rho;
    }
}
