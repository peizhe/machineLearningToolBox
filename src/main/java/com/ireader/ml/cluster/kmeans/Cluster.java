package com.ireader.ml.cluster.kmeans;

import com.ireader.ml.common.struct.DoubleVector;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by zxsted on 15-7-23.
 */
public class Cluster implements Writable {

    private int clusterID;
    private long numOfPoints;
    private DoubleVector center;

    public Cluster() {
        this.setClusterID(-1);
        this.setNumOfPoints(0);
        this.setCenter(new DoubleVector());
    }

    public Cluster(int clusterID,DoubleVector Center) {
        this.setClusterID(clusterID);
        this.setNumOfPoints(0);
        this.setCenter(center);
    }

    public Cluster(String line) {
        String[] value = line.split("，",3);
        clusterID = Integer.parseInt(value[0]);
        numOfPoints = Long.parseLong(value[1]);
        center = new DoubleVector(value[2]);
    }


    public String toString() {
        String result = String.valueOf(clusterID) + ","
                +String.valueOf(numOfPoints) + "," +center.toString() ;
        return result;
    }

    public void obseveInstance(DoubleVector doubleVector) {
        try{
            DoubleVector sum = center.multiply(numOfPoints).add(doubleVector);
            numOfPoints++;
            center = sum.divide(numOfPoints);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(clusterID);
        out.writeLong(numOfPoints);
        center.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        clusterID = in.readInt();
        numOfPoints = in.readLong();
        center.readFields(in);
    }




    public DoubleVector getCenter() {
        return center;
    }

    public void setCenter(DoubleVector center) {
        this.center = center;
    }

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public long getNumOfPoints() {
        return numOfPoints;
    }

    public void setNumOfPoints(long numOfPoints) {
        this.numOfPoints = numOfPoints;
    }
}
