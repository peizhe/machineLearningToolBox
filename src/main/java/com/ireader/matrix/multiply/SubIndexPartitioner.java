package com.ireader.matrix.multiply;

import com.ireader.matrix.multiply.writable.Key;
import com.ireader.matrix.multiply.writable.Value;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * Created by zxsted on 15-9-1.
 */
public class SubIndexPartitioner extends Partitioner<Key,Value> {

    Configuration conf = this.conf;

    private int JB = conf.getInt("MatrixMultiply.JB",10);
    private int KB = conf.getInt("MatrixMultiply.KB",20);

    @Override
    public int getPartition(Key key,Value value, int numPartitions) {
        int kb,ib,jb;

        kb = key.index1;
        ib = key.index2;
        jb = key.index3;

        return ((ib*JB + jb)*KB + kb) % numPartitions;

    }
}
