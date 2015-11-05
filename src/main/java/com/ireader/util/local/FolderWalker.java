package com.ireader.util.local;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zxsted on 15-8-20.
 *
 *  遍历目录工具类
 */
public class FolderWalker {

    /**
     * 打开一个目录，获取全部的文件名
     * */
    public static List<File> open(String path)
    {
        List<File> fileList = new LinkedList<File>();
        File folder = new File(path);
        handleFolder(folder,fileList);
        return fileList;
    }

    /**
     * 递归 收集目录下所有文件 , 存储到fileList中
     * */
    private static void handleFolder(File folder,List<File> fileList) {
        File[] fileArray  = folder.listFiles();
        if(fileArray != null)
        {
            for(File file : fileArray)
            {
                if(file.isFile())
                {
                    fileList.add(file);
                }else{
                    handleFolder(file,fileList);
                }
            }
        }
    }

}
