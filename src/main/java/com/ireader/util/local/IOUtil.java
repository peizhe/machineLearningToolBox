package com.ireader.util.local;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by zxsted on 15-8-20.
 *
 *
 * 封装一些常用的IO 操作 ， 使用NIO ， 以及循环读取器
 */
public class IOUtil {

    /**
     *  序列化对象
     * */
    public static boolean saveObjectTo(Object o,String path)
    {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     *  反序列化对象
     * */
    public static Object readObjectFrom(String path)
    {
        ObjectInputStream ois = null;
        try{
            ois = new ObjectInputStream(new FileInputStream(path));

            Object o = ois.readObject();
            ois.close();
            return o;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  使用 byte数组一次性读取数组
     * */
    public static String readTxt(String path)
    {
        if(path == null) return null;
        File file = new File(path);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];       // 使用文件长度 初始化缓存

        try{
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);                               // 将文件内容一次读取代BYTE缓存中
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(fileContent, Charset.forName("UTF-8"));
    }


    /**
     *  读取csv文件
     * */
    public static LinkedList<String[]> readCsv(String path)
    {
        LinkedList<String[]> resultList = new LinkedList<String[]>();
        LinkedList<String> lineList = readLineList(path);

        for(String line : lineList)
        {
            resultList.add(line.split(","));
        }

        return resultList;
    }

    /**
     *  快速保存
     *  使用管道内存映射
     * */
    public static boolean saveTxt(String path, String content)
    {
        try {
            FileChannel fc = new FileOutputStream(path).getChannel();
            fc.write(ByteBuffer.wrap(content.getBytes()));    // 将内容转换为bytes 写入管道
            fc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean saveTxt(String path,StringBuilder content)
    {
        return saveTxt(path,content.toString());
    }

    /**
     *  将集合类型存储到txt中
     * */
    public static <T> boolean saveCollectionToTxt(Collection<T> collection, String path)
    {
        StringBuilder sb = new StringBuilder();
        for(Object o : collection)
        {
            sb.append(o);
            sb.append('\n');
        }
        return saveTxt(path,sb.toString());
    }

    /**
     *  将整个文件作为字节数组读取
     * */
    public static byte[] readBytes(String path)
    {
        try{
            FileInputStream fis = new FileInputStream(path);
            FileChannel channel = fis.getChannel();
            int fileSize = (int) channel.size();     // 使用管道获取文件bytes长度
            ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
            channel.read(byteBuffer);
            byteBuffer.flip();       // 对其游标
            byte[] bytes = byteBuffer.array();
            byteBuffer.clear();
            channel.close();
            fis.close();
            return bytes;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static LinkedList<String> readLineList(String path)
    {
        LinkedList<String> result = new LinkedList<String>();
        String txt = readTxt(path);
        if(txt == null) return result;
        StringTokenizer tokenizer = new StringTokenizer(txt,"\n");
        while(tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    /**
     *  使用省内存的方式读取 大文件
     * */
    public static LinkedList<String> readLineListWithLessMemory(String path)
    {
        LinkedList<String> result = new LinkedList<String>();
        String line = null;

        try{
            BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
            while((line = bw.readLine()) != null)
            {
                result.add(line);
            }
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static boolean saveMapToTxt(Map<Object,Object> map,String path)
    {
        return saveMapToTxt(map,path,"=");
    }

    public static boolean saveMapToTxt(Map<Object,Object> map,String path,String separator)
    {
        map = new TreeMap<Object,Object>(map);
        return saveEntrySetToTxt(map.entrySet(), path, separator);
    }

    public static boolean saveEntrySetToTxt(Set<Map.Entry<Object,Object>> entrySet,String path,String separator)
    {
        StringBuilder sbOut = new StringBuilder();
        for(Map.Entry<Object,Object> entry : entrySet)
        {
            sbOut.append(entry.getKey());
            sbOut.append(separator);
            sbOut.append(entry.getValue());
            sbOut.append("\n");
        }

        return saveTxt(path, sbOut.toString());
    }


    public static LineIterator readLine(String path)
    {
        return new LineIterator(path);
    }


    /**
     *  循环读取器， 用于边读取边处理 ,方便处理大文件
     *  ,以后可进行扩展，支持流水处理
     * */
    public static class LineIterator implements Iterator<String>{

        BufferedReader bw ;
        String line;

        public LineIterator(String path)
        {
            try{
                bw = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
                line = bw.readLine();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void close()
        {
            if(bw == null) return ;

            try{

                bw.close();
                bw = null;
            }catch(IOException e)
            {
                //
            }

            return;
        }


        @Override
        public boolean hasNext()
        {
            if(bw == null) return false;
            if(line == null)
            {
                try{
                    bw.close();
                    bw = null;
                }catch(IOException e)
                {
                    //
                }
                return false;
            }
            return true;
        }

        @Override
        public String next() {
            String preLine = line;
            try{
                if(bw != null)
                {
                    line = bw.readLine();
                    if(line == null && bw != null)
                    {
                        try{
                            bw.close();
                            bw = null;
                        }catch(IOException e)
                        {
                            // pass
                        }
                    }
                } else {
                    line = null;
                }
            } catch(IOException e)
            {
                  // pass
            }

            return preLine;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("只读不可写");
        }
    }

}


























