package com.ireader.ml.classifier.tree.singletree.dto;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by zxsted on 15-9-22.
 *
 *  org.w3c.dom document 和xml 字符串 互转: http://blog.csdn.net/wmyasw/article/details/8686420
 */

interface XMLInterface {

    /**
     *  建立 XML 文档
     *
     * */
    public void createXML(String filename);

    /**
     *  解析xml 文档
     * */
    public void parseXml(String filename);
}

public class XmlTest implements  XMLInterface{

    private Document document;

    public void init() {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();
            this.document = builder.newDocument();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createXML(String filename) {
        Element root = this.document.createElement("scores");
        this.document.appendChild(root);
        Element employee = this.document.createElement("employee");
        Element name = this.document.createElement("name");
        name.appendChild(this.document.createTextNode("ted"));
        employee.appendChild(name);
        Element sex = this.document.createElement("sex");
        sex.appendChild(this.document.createTextNode("man"));
        employee.appendChild(sex);
        Element age = this.document.createElement("age");
        age.setAttribute("maxage", "49");
        age.appendChild(this.document.createTextNode("26"));
        employee.appendChild(age);
        root.appendChild(employee);

        TransformerFactory tf = TransformerFactory.newInstance();

        try {
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "gb2312");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
//            System.out.println("生成XML文件成功！");
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    /**
     *  将document 文件转换为XML字符串
     * */
    public static String toStringFromDoc(Document document) {
        String result = null;

        if(document != null) {
            StringWriter strWtr = new StringWriter();
            StreamResult strResult = new StreamResult(strWtr);
            TransformerFactory tfac = TransformerFactory.newInstance();

            try{
                javax.xml.transform.Transformer  t = tfac.newTransformer();
                t.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
                t.setOutputProperty(OutputKeys.INDENT,"yes");
                t.setOutputProperty(OutputKeys.METHOD,"xml");     // xml,html

                // text

//                t.transform(new DOMSource(document.getDocumentElement().getChildNodes().item(1)),strResult);
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


    public void parseXml(String fileName) {

        System.out.println(new File(fileName).getAbsolutePath());

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(fileName);

            String result = toStringFromDoc(document);

            System.out.println(result);

            System.out.println("=======================================");

            NodeList employees = document.getChildNodes();
            for(int i = 0 ; i < employees.getLength();i++) {
                Node employee = employees.item(i);
                NodeList employeeInfo = employee.getChildNodes();
                for (int j = 0; j < employeeInfo.getLength();j++) {
                    Node node = employeeInfo.item(j);
                    NodeList employeeMeta = node.getChildNodes();
                    for(int k = 0 ; k < employeeMeta.getLength(); k++) {
                        String nodeName = employeeMeta.item(k).getNodeName();
                        String textContent = employeeMeta.item(k).getTextContent();

                        System.out.println(nodeName);
                    }
                }
            }
//            System.out.println("parse done!");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        XmlTest dd = new XmlTest();

        String fileName = "/home/zxsted/data/tempxml.xml";

        dd.init();
        dd.createXML(fileName);  // build XML
        dd.parseXml(fileName);   // parse XML
    }
}
