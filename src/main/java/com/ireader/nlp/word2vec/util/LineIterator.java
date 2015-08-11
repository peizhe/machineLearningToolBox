package com.ireader.nlp.word2vec.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by zxsted on 15-7-29.
 */
public class LineIterator implements Iterator<String> {

    /** 输入缓存流 */
    private final BufferedReader  bufferedReader;
    /** 当前读取的行 */
    private String cachedLine;
    /** 标识输入流是否已经读取完毕 */
    private boolean finished = false;

    /** 构造函数 */
    public LineIterator(final Reader reader) throws IllegalAccessException {
        if(reader == null) {
            throw new IllegalAccessException(" 输入流不可为空");
        }

        if(reader instanceof BufferedReader) {
            bufferedReader  = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
    }


    /**
     *  标识输入流中是否还有行可供读入， 如果程序产生了 IOException
     *  close 将会被调用， 可以关闭输入流， 抛出 IllegalStateException
     *
     *  return 如果还有行可攻读如， 则返回 true 否则返回false
     * */
    public boolean hasNext() {
        if(cachedLine != null) {
            return true;
        } else if (finished) {
            return false;
        } else {
            try{
                while(true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        finished = true;
                    } else if (isValidLine(line)) {
                        cachedLine = line;
                        return true;
                    }
                }
            } catch(IOException ioe) {
                close();
                throw new IllegalStateException(ioe);
            }
        }
    }


    /**
     *  验证字符串， 这里实现的是直接返回true
     *  line : 代验证的字符串行
     *  return 符合条件的字符串返回 true
     * */
    protected boolean isValidLine(String line ) {
        return true;
    }

    /**
     *  从reader 中读取一行
     *
     *  输入流的下一行，
     *
     *  没有行可读取时抛出错误
     * */
    public String next() {
        return nextLine();
    }


    public String nextLine() {
        if(!hasNext()) {
            throw new NoSuchElementException("no more lines");
        }

        String currentLine = cachedLine;
        cachedLine = null;
        return currentLine;
    }

    /**
     *  关闭reader
     *
     *  如果你指向读取一个大文件的头几行， 那么这个函数可以帮助你关闭输入流，
     *  如果没有调用close 函数， 那么 Reader 将会保持打开的状态， 这一方法可以安全的使用多次。
     * */
    public void close() {
        finished = true;
        try{
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        cachedLine = null;
    }

    /**
     *  不支持移除
     * */
    public void remove() {
        throw new UnsupportedOperationException("Remove unsupported on LineIterator");
    }
}
