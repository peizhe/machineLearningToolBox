package com.ireader.matrix.multiply.writable;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by zxsted on 15-9-1.
 */
public class Value implements Writable{

    public int index1;
    public int index2;
    public double v;
    public String flag;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(index1);
        out.writeInt(index2);
        out.writeDouble(v);
        out.writeUTF(flag);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.index1 = in.readInt();
        this.index2 = in.readInt();
        this.v      = in.readDouble();
        this.flag   = in.readUTF();
    }
}
