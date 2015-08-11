package com.ireader.ml.classifier.NaiveBayes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by zxsted on 15-8-5.
 *
 *  加载数据
 */
public class NaiveBayesTrainData {

    public HashMap<String,Integer> freq;
    public NaiveBayesTrainData()
    {
        freq = new HashMap<String,Integer>();
    }

    public void getData(String file, Configuration conf) throws IOException{

        int i;
        Path data_path = new Path(file);
        Path file_path;
        String temp[],line;
        FileSystem fs = data_path.getFileSystem(conf);

        FileStatus[] status = fs.listStatus(data_path);

        for(i = 0 ; i < status.length; i++)
        {
            file_path = status[i].getPath();
            if(fs.getFileStatus(file_path).isDirectory() == true)
                continue;

            line  = file_path.toString();
            temp = line.split("/");
            if(temp[temp.length - 1].substring(0,5).equals("part-"))
                continue;

            System.err.println(line);

            FSDataInputStream fin = fs.open(file_path);
            InputStreamReader inr = new InputStreamReader(fin,"utf-8");
            BufferedReader bfr = new BufferedReader(inr);
            while((line = bfr.readLine()) != null)
            {
                String res[] = line.split("\t");
                freq.put(res[0],new Integer(res[1]));
                System.out.println(line);
            }
            bfr.close();
            inr.close();
            fin.close();
        }
    }
}
