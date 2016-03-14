package com.zk.service.impl;

import com.zk.base.HttpUtil;
import com.zk.base.Tools;
import com.zk.service.DataUsageService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    //可乐罐API基本url
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
            sb.append("key="+SECRETKEY);
        }
        return Tools.getMd5(sb.toString());
    }

    @Override
    public String getPackage(String account, String type) {
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

        System.out.println("result");
        return result;
    }
}

