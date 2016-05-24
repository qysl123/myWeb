package com.zk.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zk.base.*;
import com.zk.service.DataUsageService;
import com.zk.service.DataUsageService2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service("dataUsageService2")
public class DataUsageService2Impl implements DataUsageService2 {

    @Value("http://ll.10086gg.cn/Interface/InfcForEC.aspx?")
    private String BASEURL;

    //约定秘钥
    @Value("7e547b0863e1040756d072eb6836fa59")
    private String SECRETKEY;

    @Override
    public String doCharge() {
        String result = "";
        String url = BASEURL;
        String notifyUrl = "";
        try {
            notifyUrl = URLEncoder.encode("http://sameal4.6655.la:13741/myWeb/testController/doNotify.do", "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> paramMap = new HashMap<>();
        String orderId = "LL" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        paramMap.put("INTECMD", "A_NGADC");
        paramMap.put("USERNAME", "13551382258");
        paramMap.put("PASSWORD", "382258");
        paramMap.put("MOBILE", "18328509459");
        paramMap.put("ORDERID", orderId);
        paramMap.put("PDTVALUE", "3");
        paramMap.put("CTMRETURL", notifyUrl);
        paramMap.put("APIKEY", "59d3ab31c8454f5a9283dd3e2534dcb0");
        try {
//            result = HttpUtil.get(url+constructUrl(paramMap), null).getBody();
            result = invokeAPI(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void doNotify() {

    }

    private String constructUrl(Map<String, String> requestParamMap) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        if (requestParamMap != null) {
            for (String s : requestParamMap.keySet()) {
                if (requestParamMap.get(s) != null) {
                    stringBuilder.append(s).append("=");
                    stringBuilder.append(URLEncoder.encode(requestParamMap.get(s), HttpUtil.CHARSET_UTF8));
                    stringBuilder.append("&");
                }
            }
            if (stringBuilder.length() > 0) {
                stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }
        return stringBuilder.toString();
    }

    private String invokeAPI(Map<String, String> requestParamMap) throws UnsupportedEncodingException {
        Map<String, String> paramMap = new HashMap<>();

        paramMap.put("method", "get");
        paramMap.put("path", constructUrl(requestParamMap));
        paramMap.put("data", "");
        paramMap.put("headers", "");
        paramMap.put("params", "");
        String result = "";
        try {
            result  = HttpUtil.post("http://www.yichengshequ.com:6060/simpleRestProxy.jsp", JSONObject.toJSONString(paramMap)).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }
}

