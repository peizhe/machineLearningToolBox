package com.ireader.matrix.multiply.writable;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by zxsted on 15-9-1.
 *
 * 双位索引
 */
public class IndexPair implements WritableComparable{

    public int index1;
    public int index2;

    @Override
    public int compareTo(Object other) {
        IndexPair o = (IndexPair) other;
        if(this.index1 < o.index1) {
            return -1;
        } else if(this.index1 > o.index1) {
            return +1;
        }

        if(this.index2  < o.index2) {
            return -1;
        } else if(this.index2 > o.index2) {
            return +1;
        }
        return 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(index1);
        out.writeInt(index2);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        index1 = in.readInt();
        index2 = in.readInt();
    }

    @Override
    public int hashCode() {
        return index1 << 16 + index2;
    }
}
