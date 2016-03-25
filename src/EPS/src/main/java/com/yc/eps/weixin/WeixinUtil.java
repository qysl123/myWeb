package com.yc.eps.weixin;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.yc.commons.Tools;

public class WeixinUtil {

    /**
     * 创建签名
     * 
     * @param packageParams
     * @return
     */
    public static String createSign(SortedMap<String, String> packageParams, String partnerkey) {
        StringBuffer sb = new StringBuffer();
        Set<String> set = packageParams.keySet();
        for (String key : set) {
            String value = packageParams.get(key);
            if (value != null && !value.equals("") && !key.equals("sign") && !key.equals("key")) {
                sb.append(key + "=" + value + "&");
            }
        }
        sb.append("key=");
        sb.append(partnerkey);

        String sign = sb.toString();
        sign = Tools.getMd5UpperString(sign);

        return sign;
    }

    /**
     * 创建XML
     * 
     * @param packageParams
     * @return
     */
    public static String createXML(SortedMap<String, String> packageParams) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>\n");
        Set<String> set = packageParams.keySet();

        for (String key : set) {
            String value = packageParams.get(key);
            if (value != null && !value.equals("") && !key.equals("appkey")) {
                sb.append("<" + key + "><![CDATA[" + value + "]]></" + key + ">\n");
            }
        }

        sb.append("</xml>");
        return sb.toString();
    }

    public static SortedMap<String, String> parseXML(String xmlStr) {
        SortedMap<String, String> result = new TreeMap<String, String>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlStr.getBytes("UTF-8")));
            Element root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            int nodeLength = nodeList.getLength();
            for (int i = 0; i < nodeLength; i++) {
                Node node = nodeList.item(i);
                if (!"#text".equalsIgnoreCase(node.getNodeName())) {
                    result.put(node.getNodeName(), node.getTextContent());
                }
            }
        } catch (Exception e) {
            result.clear();
            e.printStackTrace();
        }
        return result;
    }

}
