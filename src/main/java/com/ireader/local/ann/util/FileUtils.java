package com.ireader.local.ann.util;

/**
 * Created by zxsted on 15-8-3.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *文件操作类 ，
 *@author  ted
 *@version
 * */
public class FileUtils {

    /**
     * 读出文件的全部内容到一个变量
     * @param filename : the file to read
     * @param encoding :the encoding of the file
     * @return the content of the input file
     * */
    public static String read (String filename,String encoding)
    {
        BufferedReader in;
        String content = "";
        try{
            in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(filename),encoding));

            char[] newContent = new char[40960];
            int numRead = -1;
            while((numRead = in.read(newContent)) != -1)
            {
                content += new String(newContent,0,numRead);
            }
            in.close();
        }catch(Exception e)
        {
            content = "";
        }
        return content;
    }

    /**
     * 读出文件的内容到一个list中
     * */
    public static List<String> readLine(String filename, String encoding)
    {
        List<String> lines = new ArrayList<String>();
        try{
            String content = "";
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(filename),encoding));

            while((content = in.readLine()) != null)
            {
                content = content.trim();
                if (content.length() == 0)
                    continue;
                lines.add(content);
            }
            in.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
        return lines;
    }

    /**
     * 将一条String写入文件中
     * @param filename
     * @param encoding
     * @strToWrite string
     * @return boolean
     * */
    public static boolean write(String filename,String encoding, String strToWrite)
    {
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename),encoding));
            out.write(strToWrite);
            out.close();
        }catch(Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Get all file (non-recursively ) from a directory
     * @param directory The directory to read
     * */
    public static String[] getAllFiles(String directory)
    {
        File dir = new File(directory);
        String[] fns = dir.list();
        return fns;
    }

    /**
     * Get all file (non-recursively) from a directory
     * @param directory The directory to read
     * @return A list of filenames (without path) in the input directory
     * */
    public static List<String> getAllFiles2(String directory)
    {
        File dir = new File(directory);
        String[] fns = dir.list();
        List<String> files = new ArrayList<String>();
        if(fns != null)
            for(int i = 0; i < fns.length; i++)
                files.add(fns[i]);
        return files;
    }

    /**
     * Test whether a file /directory exists.
     * @param file the file/directory to test
     * */
    public static boolean exists(String file)
    {
        File f = new File(file);
        return f.exists();
    }

    /**
     * 复制一个文件
     * @param srcFile  the source file
     * @param dstFile the copied file
     * */
    public static void copyFile(String srcFile,String dstFile)
    {
        try{
            FileInputStream fis = new FileInputStream(new File(srcFile));
            FileOutputStream fos = new FileOutputStream(new File(dstFile));
            try{
                byte[] buf = new byte[40960];
                int i = 0;
                while((i = fis.read(buf)) != -1){
                    fos.write(buf,0,i);
                }
            }catch(Exception e)
            {
                System.out.println("Error in fileUtils.copyFile: " + e.toString());
            }finally{
                if (fis != null) fis.close();
                if(fos != null) fos.close();
            }
        }catch(Exception ex)
        {
            System.out.println("Error in FileUtils.copyFile: " + ex.toString());
        }
    }

    /**
     * 将源文件夹下的文件复制到目标文件夹下
     * @param srcDir
     * @param dstDir
     * @param files
     * */
    public static void copyFiles(String srcDir,String dstDir,List<String> files)
    {
        for(int i = 0; i < files.size(); i++)
            FileUtils.copyFile(srcDir+files.get(i), dstDir+files.get(i));
    }

    public  static final int BUF_SIZE = 51200;

    /**
     * 解压缩 zip文件
     * @param file_input
     * @param dir_output
     * */
    public static int gunzipFile(File file_input,File dir_output) {
        // create a buffered gzip input stream to the archive file.
        GZIPInputStream gzip_in_stream;
        try{
            FileInputStream in = new FileInputStream(file_input);
            BufferedInputStream source = new BufferedInputStream(in);
            gzip_in_stream  = new GZIPInputStream(source);
        }catch(IOException e) {
            System.out.println("Error in gunzipFile(): " + e.toString());
            return 0;
        }

        //use the name of the archive for the output file anem but
        //with ".gz" stripped off
        String file_input_name = file_input.getName();
        String file_output_name = file_input_name.substring(0,file_input_name.length() -3);

        //create the decompressed output file
        File output_file = new File (dir_output,file_output_name);

        //Decompress the gzipped file by reading it via
        //the GZIP input stream.will need a buffer.
        byte[] input_buffer = new byte[BUF_SIZE];
        int len = 0;
        try{
            //create a buffered output stream to the file.
            FileOutputStream out = new FileOutputStream(output_file);
            BufferedOutputStream destination = new BufferedOutputStream(out,BUF_SIZE);

            //now read fro the gzip stream which will decopress the data
            //and write to the output stream
            while((len = gzip_in_stream.read(input_buffer,0,BUF_SIZE) ) != -1)
                destination.write(input_buffer,0,len);
            destination.flush();
            out.close();
        }
        catch(IOException e) {
            System.out.println("Error in gunzipFile():" + e.toString());
            return 0;
        }

        try{
            gzip_in_stream.close();
        }catch(IOException e) {
            return 0;
        }
        return 1;
    }

    /**
     * Gzip an input file
     * @param inputFile the input file to gzip
     * @param gzipFilename the gunzipped file name
     * @return 1 if succeeds, 0 otherwise
     * */
    public static int gzipFile(String inputFile,String gzipFilename) {
        try{
            //SPecify gzip file namef
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipFilename));

            //Specify the input file to be compressed
            FileInputStream in = new FileInputStream(inputFile);

            //Transfer bytes from the input file
            // to the gzip output stream
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf,0,len);
            }
            in.close();

            //Finish creation of gzip file
            out.finish();
            out.close();
        }
        catch(Exception ex)
        {
            return 0;
        }
        return 1;
    }
}
