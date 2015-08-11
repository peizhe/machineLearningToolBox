package com.ireader.ml.common.struct;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxsted on 15-7-23.
 */
public class ListWritable<T extends Writable> implements Writable{

    private List<T> list;
    private Class<T> clazz;




    public ListWritable(Class<T> clazz) {
        this.clazz = clazz;
        list = new ArrayList<T>();
    }

    public void add(T element) {
        list.add(element);
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(clazz.getName());
        out.writeInt(list.size());
        for(T element:list) {
            element.write(out);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException{
        try{
            clazz = (Class<T>)Class.forName(in.readUTF());
        }catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        int count = in.readInt();
        this.list = new ArrayList<T>();
        for(int i = 0 ; i < count;i++) {
            try{
                T obj = clazz.newInstance();
                obj.readFields(in);
                list.add(obj);
            }catch(InstantiationException e) {
                e.printStackTrace();
            }catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }
}
