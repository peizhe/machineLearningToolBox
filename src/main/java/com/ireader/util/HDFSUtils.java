package com.ireader.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by zxsted on 15-8-9.
 *
 * hdfs 文件操作类， 源码链接 http://www.cnblogs.com/liuling/category/487231.html
 */
public class HDFSUtils {

    // 创建新的文件
    public static void createFile(String dst,byte[] contents) throws IOException {

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path dstPath = new Path(dst); // 目标路径
        // 打开一个输出流
        FSDataOutputStream outputStream = fs.create(dstPath);
        outputStream.write(contents);
        outputStream.close();
        fs.close();
        System.out.println("文件创建成功！");

    }


    // 上传本地文件
    public static void uploadFile(String src,String dst) throws IOException{
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path srcPath =  new Path(src);   //
        Path dstPath = new Path(dst);

        fs.copyFromLocalFile(false, srcPath, dstPath);

        //
        System.out.println("Upload to " + conf.get("fs.default.name"));
                System.out.println("------------list files------------" + "\n");
                FileStatus[] fileStatus = fs.listStatus(dstPath);
                for (FileStatus file : fileStatus)
                 {
                    System.out.println(file.getPath());
                 }
                 fs.close();
    }

    // 文件重命名
    public static void rename(String oldName,String newName) throws IOException{
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path oldPath = new Path(oldName);
        Path newPath = new Path(newName);
        boolean isok = fs.rename(oldPath, newPath);
        if(isok) {
            System.out.println("rename ok!");
        }else{
            System.out.println("rename failure");
        }
        fs.close();
    }

    // 删除文件
    public static void delete(String filePath) throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        boolean isok = fs.deleteOnExit(path);
        if(isok){
            System.out.println("delete ok!");
        }else {
            System.out.println("delete failure");
        }
        fs.close();
    }

    // 创建目录
    public static void mkdir(String path) throws IOException{
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path srcPath = new Path(path);
        boolean isok = fs.mkdirs(srcPath);
        if(isok){
            System.out.println("create dir ok!");
            }else{
                 System.out.println("create dir failure");
               }
        fs.close();
    }

    // 读取文件的全部内容
    public static void readFile(String filePath) throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path srcPath = new Path(filePath);
        InputStream in = null;
        try{
            in = fs.open(srcPath);
            IOUtils.copyBytes(in, System.out, 4096, false);

        }finally{
            IOUtils.closeStream(in);
        }
    }

    // 压缩文件
    public static void compress(String codecClassName) throws Exception {
        Class<?> codecClass = Class.forName(codecClassName);
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
        // 指定压缩文件路径
        FSDataOutputStream outputStream = fs.create(new Path("/user/hadoop/text.gz"));
        //指定要被压缩的文件路径
        FSDataInputStream in = fs.open(new Path("/user/hadoop/aa.txt"));
        // 创建压缩输出流
        CompressionOutputStream out = codec.createOutputStream(outputStream);
        IOUtils.copyBytes(in,out,conf);
        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
    }

    // 解压缩
    public static void uncompress(String fileName) throws Exception{
        Class<?> codecClass = Class.forName("org.apache.hadoop.io.compress.GzipCodec");
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        CompressionCodec codec = (CompressionCodec)ReflectionUtils.newInstance(codecClass, conf);
        FSDataInputStream inputStream = fs.open(new Path("/user/hadoop/text.gz"));
        //把text文件里到数据解压，然后输出到控制台
        InputStream in = codec.createInputStream(inputStream);
        IOUtils.copyBytes(in, System.out, conf);
        IOUtils.closeStream(in);
        }

    // 使用文件名来推断二来的codec来对文件进行解压缩
    public static void uncompress1(String uri) throws IOException{
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);

        Path inputPath = new Path(uri);
        CompressionCodecFactory factory = new CompressionCodecFactory(conf);
        CompressionCodec codec = factory.getCodec(inputPath);
        if(codec == null){
               System.out.println("no codec found for " + uri);
               System.exit(1);
             }
        String outputUri = CompressionCodecFactory.removeSuffix(uri, codec.getDefaultExtension());
        InputStream in = null;
        OutputStream out = null;
        try {
                in = codec.createInputStream(fs.open(inputPath));
                out = fs.create(new Path(outputUri));
                IOUtils.copyBytes(in, out, conf);
               } finally{
                  IOUtils.closeStream(out);
                  IOUtils.closeStream(in);
              }
    }


    /** hdfs PathFilter
     *  public FileStatus[] globStatus(Path pathPattern) throw IOException
     *  public FileStatus[] globStatus(Path pathPattern, PathFilter filter) throw IOException
     */

    static class RegexExcludePathFilter implements PathFilter{
        private final String regex;
        public RegexExcludePathFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public boolean accept(Path path) {
            return !path.toString().matches(regex);
        }
    }


    public static void list() throws IOException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        FileStatus[] status = fs.globStatus(new Path("hdfs://master:9000/user/hadoop/test/*"),
                new RegexExcludePathFilter(".*txt") );
        //FileStatus[] status = fs.globStatus(new Path("hdfs://master:9000/user/hadoop/test/*"));
        Path[] listedPaths = FileUtil.stat2Paths(status);
        for (Path p : listedPaths) {
            System.out.println(p);
        }

    }


}
