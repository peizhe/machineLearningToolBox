package com.ireader.ml.feature.wordconcurrence;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.RecordReader;

/**
 * Created by zxsted on 15-8-6.
 */
public class WholeFileInputFormat extends FileInputFormat<Text,BytesWritable> {


    @Override
    protected boolean isSplitable(JobContext context,Path filename) {

        return false;       // 设置为false 表示 不将文件进行分割
    }

    @Override
    public RecordReader<Text,BytesWritable> createRecordReader(InputSplit split,
                                                                TaskAttemptContext context) {

        return new SingleFileNameReader((FileSplit) split,context.getConfiguration());
    }


}
