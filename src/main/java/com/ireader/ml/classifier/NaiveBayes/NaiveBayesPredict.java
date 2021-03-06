package com.ireader.ml.classifier.NaiveBayes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by zxsted on 15-8-5.
 */
public class NaiveBayesPredict {

    public static class NBPredictMapper extends Mapper<Object,Text,Text,Text> {

        public NaiveBayesConf nBConf;
        public NaiveBayesTrainData nBTData;

        public void setup(Context context){

            try{
                Configuration conf = new Configuration();

                nBConf = new NaiveBayesConf();
                nBConf.ReadNavieBayesConf(conf.get("conf"),conf);
                nBTData = new NaiveBayesTrainData();
                nBTData.getData(conf.get("train_result"),conf);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        public void map(Object key, Text value, Context context)
            throws IOException,InterruptedException{

            Scanner scan = new Scanner(value.toString());

            String str,vals[],temp ;
            int i,j,k,fxyi,fyi,fyij,maxf,idx;
            Text id;
            Text cls;

            while(scan.hasNextLine())
            {
                str = scan.nextLine();
                vals = str.split(" ");
                maxf = -100;
                idx = -1;

                // 计算当前行的数据 在各个类别上的概率


                for(i = 0; i < nBConf.class_num;i++)
                {
                    fxyi = 1;
                    String cl = nBConf.classNames.get(i);
                    Integer integer = nBTData.freq.get(cl);
                    if(integer == null)
                        fyi = 0;
                    else
                        fyi = integer.intValue();

                    for(j = 1; j < vals.length;j++)
                    {
                        temp = cl + "#" + nBConf.proNames.get(j-1) + "#" + vals[j];

                        integer = nBTData.freq.get(temp);

                        if(integer == null)
                            fyij = 0;
                        else
                            fyij = integer.intValue();
                        fxyi = fxyi * fyij;
                    }
                    if (fyi * fxyi > maxf)
                    {
                        maxf = fyi * fxyi;
                        idx = i;
                    }

                }

                id = new Text(vals[0]);
                cls = new Text(nBConf.classNames.get(idx));
                context.write(id,cls);
            }
        }
    }
}
