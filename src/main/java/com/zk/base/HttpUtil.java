package com.zk.base;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CONTENTTYPE = "contentType";
    public static final String POST = "POST";
    public static final String GET = "GET";

    private static final Map<String, String> BROWSER_HEADERS = new HashMap<String, String>();

    static {
        BROWSER_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
        BROWSER_HEADERS
                .put("Accept",
                        "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, */*");
    }

    public static Map<String, String> getBrowserHeaders() {
        return BROWSER_HEADERS;
    }

    public static HttpResponse get(String url) throws Exception {
        return get(url, null);
    }

    public static HttpResponse get(String url, Map<String, String> headers) throws Exception {
        return send(url, null, GET, headers);
    }

    public static HttpResponse post(String url, String content) throws Exception {
        return post(url, content, null);
    }

    public static HttpResponse post(String url, String content, Map<String, String> headers) throws Exception {
        return send(url, content, POST, headers);
    }

    public static HttpResponse post(String url, Map<String, String> parameters) throws Exception {
        return post(url, parameters, null);
    }

    public static HttpResponse post(String url, Map<String, String> parameters, Map<String, String> headers)
            throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if (parameters != null) {
            for (String s : parameters.keySet()) {
                if (parameters.get(s) != null) {
                    stringBuilder.append(s).append("=");
                    stringBuilder.append(URLEncoder.encode(parameters.get(s), CHARSET_UTF8));
                    stringBuilder.append("&");
                }
            }
            if (stringBuilder.length() > 0) {
                stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }

        return send(url, stringBuilder.toString(), POST, headers);
    }

    public static HttpResponse send(String url, String content, String method, Map<String, String> headers)
            throws Exception {
        String charset = CHARSET_UTF8;
        HttpURLConnection conn = null;
        URL httpUrl = null;
        httpUrl = new URL(url);
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            if (headers != null && headers.size() > 0) {
                for (String name : headers.keySet()) {
                    conn.setRequestProperty(name, headers.get(name));
                }
            }

            if (!GET.equals(method)) {
                outputStream = conn.getOutputStream();
                if (POST.equalsIgnoreCase(method)) {
                    outputStream.write(content.getBytes(charset));
                }
                outputStream.flush();
                outputStream.close();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("状态码返回错误，返回的状态码是 ： " + responseCode);
            }
            inputStream = conn.getInputStream();
            HttpResponse result = new HttpResponse();
            result.setBody(IOUtils.toString(inputStream, charset));
            result.setCode(responseCode);
            result.setHeaders(conn.getHeaderFields());

            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error(HttpUtil.class.getName(), e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(HttpUtil.class.getName(), e);
                }
            }
        }
    }

    public static class HttpResponse {
        private String body;
        private int code;
        private Map<String, List<String>> headers;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Http resposne Code :").append(code).append(",Body：").append(body);
            return sb.toString();
        }
    }
}
