package com.yc.etcp.common;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {
    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public static String getBodyString(HttpServletRequest request) {
        String type = request.getHeader("content-type");
        if (type != null && type.contains("application/x-www-form-urlencoded")) {
            return getFormDataString(request);
        }

        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream,
                    Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private static String getFormDataString(HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        Map map = request.getParameterMap();
        for (Object key : map.keySet()) {
            String[] values = (String[]) map.get(key);
            if (values != null && values.length != 0) {
                resultMap.put((String) key, values[0]);
            }
        }
        return JSONObject.toJSONString(resultMap);
    }
}
