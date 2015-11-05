package com.ireader.matrix.multiply.writable;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by zxsted on 15-9-1.
 *
 * 三位 索引
 */
public class Key implements WritableComparable{

    public int index1;
    public int index2;
    public int index3;
    public byte m;
    public boolean useM;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(index1);
        out.writeInt(index2);
        out.writeInt(index3);

        if(useM) out.writeByte(m);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        index1 = in.readInt();
        index2 = in.readInt();
        index3 = in.readInt();

        if(useM) {
            m = in.readByte();
        }
    }

    @Override
    public int compareTo(Object other) {
        Key o = (Key) other;
        if(this.index1 < o.index1) {
            return -1;
        } else if(this.index1 > o.index1) {
            return +1;
        }

        if(this.index2 < o.index2) {
            return -1;
        } else if(this.index2 > o.index2) {
            return +1;
        }

        if(this.index3 < o.index3) {
            return -1;
        } else if(this.index3 > o.index3) {
            return +1;
        }

        if(!useM) return 0;

        if(this.m < o.m) {
            return -1;
        } else if(this.m > o.m) {
            return +1;
        }

        return 0;
    }



}
