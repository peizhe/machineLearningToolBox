package com.ireader.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Properties;

/**
 * Created by zxsted on 15-9-24.
 */
public class DOMUtils {

    /**
     *  初始化一个DocumentBuilderFactory
     *
     * */
    public static DocumentBuilderFactory newDocumentBuilderFactory(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf;
    }

    /**
     *  初始化一个DocumentBuilder
     *
     *  @return a DocumentBuilder
     * */
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        return newDocumentBuilderFactory().newDocumentBuilder();
    }

    /**
     *  初始化一个空的Document对象返回
     * */
    public static Document newXMLDocument() throws ParserConfigurationException {

        return newDocumentBuilder().newDocument();
    }

    /**
     *  将一个XML String 转换为一个 Document 对象返回
     *
     *
     * */
    public static Document parseXMLDocument(String xmlString) {
        if(xmlString == null) {
            throw new IllegalArgumentException();
        }

        try{
            return newDocumentBuilder().parse(
                    new InputSource(new StringReader(xmlString))
            );
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 给定一个输入流 ，解析为一个Document 对象返回
     *
     * @param input
     * @return Document
     * */
    public static Document parseXMLDocument(InputStream input) {

        if(input == null) {
            throw new IllegalArgumentException("参数为null !");
        }

        try{
            return newDocumentBuilder().parse(input);
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    /**
     *  给定一个文件名 ， 解析该文件并返回一个document 对象
     *
     *  @param fileName : 待解析的文件名
     *
     *  @return Document
     * */
    public static Document loadXMLDocumentFromFile(String fileName) {
        if(fileName == null) {
            throw new IllegalArgumentException("未指定文件名以及其物理路径！");
        }

        try{
            return newDocumentBuilder().parse(new File(fileName));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  将dom文件转换为xml字符串
     * */
    public static String toStringFromDoc(Document document) {

        String result = null;

        if(document != null) {
            StringWriter strWtr = new StringWriter();               // 将结果输出到字符串中
            StreamResult strResult = new StreamResult(strWtr);      // 将结果传输到流中
            TransformerFactory tfac = TransformerFactory.newInstance();    // 转换factory

            try {
                Transformer t = tfac.newTransformer();
                t.setOutputProperty(OutputKeys.ENCODING,"utf-8");
                t.setOutputProperty(OutputKeys.INDENT,"yes");
                t.setOutputProperty(OutputKeys.METHOD,"xml");

                t.setOutputProperty(
                        "{http://xml/apache.org/xslt}indent-amount","4");
                // 注意 DOMSource 可以将任何文档对象封装为source
                t.transform(new DOMSource(document.getDocumentElement()),strResult);

            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

            result = strResult.getWriter().toString();

            try{
                strWtr.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     *  给定一个节点， 将该节点加入到新构造的Document中
     *  @param node
     *        a Document node
     *  @return a new Document
     * */
    public static Document newXMLDocument(Node node) throws ParserConfigurationException {
        Document doc = newXMLDocument();
        doc.appendChild(doc.importNode(node,true));
        return doc;
    }

    /**
     *  获取一个 transformer 对象， 因为 使用时 都做相同的初始化， 所以提取出来作为公共 函数
     * */
    public static Transformer newTransformer() {
        try{
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Properties properties = transformer.getOutputProperties();
            properties.setProperty(OutputKeys.ENCODING,"gb2312");
            properties.setProperty(OutputKeys.METHOD,"xml");
            properties.setProperty(OutputKeys.VERSION,"1.0");
            properties.setProperty(OutputKeys.INDENT,"no");
            transformer.setOutputProperties(properties);
            return transformer;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  自己构建xml 字符串
     * */
    public static String errXMLString(String title,String errMsg,Class errClass) {
        StringBuffer msg = new StringBuffer(100);
//        msg.append("<?xml version='1.0' encoding='utf-8'?>");
        msg.append("<errNode title=\"" + title + "\" errMsg=\"" + errMsg + "\" errSource=\"" +errClass.getName()
                + "\"/>");

        return msg.toString();
    }



    public static void main(String args[]) {

        String errxml = errXMLString("java", "test err", DOMUtils.class);

        Document d = parseXMLDocument(errxml);

        System.out.println(d.getElementsByTagName("errNode").item(0).getAttributes().item(0).getNodeValue());

    }

}
