package com.ireader.ml.classifier.largelogisticregression;

import com.ireader.local.ann.dto.DataPoint;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * Created by zxsted on 15-8-11.
 */
//public class WDInputFormat extends FileInputFormat<Text,WDataPoint> {
//
//    @Override
//    protected boolean isSplitable(JobContext context,Path filename) {
//        return true;
//    }
//
//    @Override
////    public RecordReader<Text,DataPoint> createRecordReader(InputSplit split,
////                                                           TaskAttemptContext context) {
////        return new WDReader((FileSplit) split,context.getConfiguration());
////    }
//
//    public static class WDReader extends RecordReader<Text,WDataPoint> {
//
//
//    }
//
//}
