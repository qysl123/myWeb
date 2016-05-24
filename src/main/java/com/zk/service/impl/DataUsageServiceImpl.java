package com.zk.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yc.chat.util.JsonUtil;
import com.zk.base.*;
import com.zk.service.DataUsageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service("dataUsageService")
public class DataUsageServiceImpl implements DataUsageService {

    @Value("http://liu.gzelian.com/api.aspx?")
    private String BASEURL;

    //约定秘钥
    @Value("68753b2b5f2443fea226bc7c06fe7eac")
    private String SECRETKEY;


    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    private Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        });
        sortMap.putAll(map);
        return sortMap;
    }

    private String getSign(Map<String, String> sortMap) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (sortMap != null) {
            for (String s : sortMap.keySet()) {
                if (sortMap.get(s) != null) {
                    sb.append(s).append("=");
                    sb.append(URLEncoder.encode(sortMap.get(s), HttpUtil.CHARSET_UTF8));
                    sb.append("&");
                }
            }
            sb.append("key=" + SECRETKEY);
        }
        return Tools.getMd5(sb.toString());
    }

    @Override
    public String getPackage(String account, String type) {
        getBalance(account);
        String url = BASEURL;
        String result = "";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("account", account);
        paramMap.put("type", type);
        try {
            paramMap.put("sign", getSign(sortMapByKey(paramMap)));
            paramMap.put("v", "1.1");
            paramMap.put("action", "getPackage");
            result = HttpUtil.post(url, paramMap).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataUsagePackages response = JSON.parseObject(result, DataUsagePackages.class);
        System.out.println(response);
        for (DataUsagePackage p : response.getPackages()) {
            System.out.println(p.toString());
        }


//        chargeDataUsage(account, "13881906371", "");
        return result;
    }

    public String chargeDataUsage(String account, String moblie, String packge) {
        String url = BASEURL;
        String result = "";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("account", account);
        paramMap.put("mobile", moblie);
        paramMap.put("package", packge);
        try {
            paramMap.put("sign", getSign(sortMapByKey(paramMap)));
            paramMap.put("v", "1.1");
            paramMap.put("action", "charge");
            paramMap.put("range", "0");
            paramMap.put("outTradeNo", "LL" + System.currentTimeMillis() + (int) (Math.random() * 100));
            result = HttpUtil.post(url, paramMap).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataUsageBaseResponse response = JSONObject.parseObject(result, DataUsageBaseResponse.class);
        System.out.println(response);

        return "";
    }

    public String getBalance(String account){
        String url = BASEURL;
        String result = "";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("account", account);
        try {
            paramMap.put("sign", getSign(sortMapByKey(paramMap)));
            paramMap.put("v", "1.1");
            paramMap.put("action", "getBalance");
            result = HttpUtil.post(url, paramMap).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(JSONObject.toJSONString(result));
        return "";
    }
}

