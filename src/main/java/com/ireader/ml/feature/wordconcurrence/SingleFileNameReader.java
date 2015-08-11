package com.ireader.ml.feature.wordconcurrence;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * Created by zxsted on 15-8-6.
 */
public class SingleFileNameReader extends RecordReader<Text,BytesWritable> {

    private FileSplit fileSplit;
    private Configuration conf;

    private boolean processed = false;    // 表示该分片是否已经处理完毕
    private Text key = null;
    private BytesWritable value = null;

    private FSDataInputStream fis = null;

    // reader 的功能是读取一个分片
    public SingleFileNameReader(FileSplit fileSplit,Configuration conf) {
        this.fileSplit = fileSplit;
        this.conf = conf;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public float getProgress() throws IOException,InterruptedException {

        return processed?1.0f:0.0f;
    }

    @Override
    public Text getCurrentKey() throws IOException,InterruptedException {
        return key;
    }

    @Override
    public BytesWritable getCurrentValue() throws IOException,InterruptedException{

        return value;
    }

    /*
    @Override
    public void initialize(InputSplit arg0,TaskAttemptContext arg1) throws IOException {
        fileSplit = (FileSplit) arg0;
        Configuration job = arg1.getConfiguration();   // 从 inputSplit中获取 conf

        Path file = fileSplit.getPath();
        FileSystem fs = file.getFileSystem(job);
        fis = fs.open(file);

    }*/

    @Override
    public boolean nextKeyValue() throws IOException,InterruptedException {
        if(key == null)
        {
            key = new Text();
        }
        if(value == null)
        {
            value = new BytesWritable();
        }

        if(!processed)
        {
            // content 保存分片中所有的内容
            byte[] content = new byte[(int) fileSplit.getLength()];
            Path file = fileSplit.getPath();
            System.out.println(file.getName());      // 打印分片名字
            key.set(file.getName());                 // 将文件名字 设置为key
            try{
                // 将文件的直接 复制到 content 数组中
                IOUtils.readFully(fis, content, 0, content.length);
                value.set(new BytesWritable(content));   // 将所有内容作为key
            } catch(IOException e){
                e.printStackTrace();
            }finally{
                IOUtils.closeStream(fis);
            }
            processed = true;
            return true;
        }
        return false;    // 表示没有 要处理的数据了
    }

    @Override
    public void initialize(InputSplit split,
                           TaskAttemptContext context) throws IOException {
        fileSplit = (FileSplit) split;
        Configuration job = context.getConfiguration();
        Path file = fileSplit.getPath();
        FileSystem fs = file.getFileSystem(job);
        fis = fs.open(file);
    }



}
