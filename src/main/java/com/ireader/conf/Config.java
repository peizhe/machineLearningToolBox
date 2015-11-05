package com.ireader.conf;

import java.io.*;
import java.util.Properties;

/**
 * Created by zxsted on 15-8-5.
 */
public class Config {


    private Properties prop = null;


    /**
     *  使用用户指定的配置文件 ，配置模型
     * */
    public Config(String propFile) throws UnsupportedEncodingException {


        prop = new Properties();

        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(propFile),"utf-8"));

            prop.load(br);

        }catch(IOException ie){
            ie.printStackTrace();
        } finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *  加载工程默认 模型配置文件
     * */
    public Config(){
        prop = new Properties();

        InputStream propin= Config.class.getClassLoader().getResourceAsStream("model_default.properties");

        if(null != propin)
            try {
                prop.load(propin);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    public String getString(String key,String defalutval){
        return prop.getProperty(key,defalutval);
    }


    public int getInt(String key,int  defaultval) {

        return Integer.parseInt(prop.getProperty(key, String.valueOf(defaultval)));
    }

    public long getLong(String key,long defaultval) {
        return Long.parseLong(prop.getProperty(key, String.valueOf(defaultval)));
    }

    public double getDouble(String key, double defaultval) {
        return Double.parseDouble(prop.getProperty(key, String.valueOf(defaultval)));
    }

    public float getDouble(String key, float defaultval) {
        return Float.parseFloat(prop.getProperty(key, String.valueOf(defaultval)));
    }

    public boolean getBoolean(String key, boolean defaultval){
        return Boolean.parseBoolean(prop.getProperty(key, String.valueOf(defaultval)));

    }



    public static void main(String[] args) {

        // /home/zxsted/IdeaProjects/machineLearnToolbox/resources

        try {
            Config conf = new Config("/home/zxsted/IdeaProjects/machineLearnToolbox/resources/model.properties");
            int iter = conf.getInt("iter",0);
            double learnRate = conf.getDouble("learnRate", 0.0);
            String  weightSavePath = conf.getString("weightSavePath", "/input");
            System.out.println("iter is :" + iter);
            System.out.println("learnRate is:" +learnRate );
            System.out.println("weightSave path is:" + weightSavePath);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
