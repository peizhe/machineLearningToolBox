package com.ireader.graph.mr.sssp;

import com.ireader.graph.mr.MoreIterations;
import com.ireader.graph.mr.Node;
import com.ireader.graph.mr.bfs.TraverseGraph;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by zxsted on 15-11-2.
 *
 * 单源最短路径
 */
public class SSSP implements Tool {

    @Override
    public void setConf(Configuration configuration) {

    }

    @Override
    public Configuration getConf() {
        return null;
    }

    static class SSSPMapper extends TraverseGraph.TravereMapper {

        public void map(Object key, Text value,Context context)
                               throws IOException,InterruptedException {

            Node inNode = new Node(value.toString());

            super.map(key,value,context,inNode);
        }
    }


    static class SSSPReducer extends TraverseGraph.TracerseReducer {

        public void reduce(Text key,Iterable<Text> values,Context context)
                                       throws IOException ,InterruptedException {

            Node outNode = new Node();

            outNode = super.reduce(key,values,context,outNode);

            // 如果当前节点已经访问，但是没有处理
            if (outNode.getColor() == Node.Color.GRAY)
                context.getCounter(MoreIterations.numberOfIteration).increment(1);

        }
    }


    public int run(String[] args) throws Exception {

        int iterationCount = 0;

        String input = null;
        String output = null;

        Job job;
        Configuration conf = getConf();

        long terminationValue = 1;

        // 当还有灰色的节点 ，那么执行循环处理
        while (terminationValue > 0) {

            if(iterationCount == 0) {
                input = args[0];
            } else {
                input = args[1] + (iterationCount+1);
            }

            output = args[1] + (iterationCount + 1);

            job = new Job(conf,"SSSP");

            job.setJarByClass(SSSP.class);
            job.setMapperClass(SSSP.SSSPMapper.class);
            job.setReducerClass(SSSP.SSSPReducer.class);
            job.setNumReduceTasks(10);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            FileInputFormat.setInputPaths(job, new Path(input));
            FileOutputFormat.setOutputPath(job, new Path(output));

            job.waitForCompletion(true);


            /** ============= 取出计数器 =========================================== */
            Counters jobCntrs = job.getCounters();
            terminationValue = jobCntrs.findCounter(MoreIterations.numberOfIteration).getValue();
            iterationCount++;

        }

        return 0;
    }


    public static void main(String[] args) throws Exception {

        int res = ToolRunner.run(new Configuration(), new SSSP(), args);

        if (args.length != 2) {
            System.err.println("Usage:<in><output name>");
        }

        System.exit(res);
    }



}
