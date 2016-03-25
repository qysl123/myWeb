package com.yc.eps.trustpay;

import com.yc.edsi.payment.third.TrustPayPO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TrustPayUtil {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TrustPayUtil.class);

    public static String appId;

    public static String appKey;

    public static String sourceNo;

    public static String merchantNo;

    public static String trustPayUrl;

    public static String notifyUrl;

    static {
        try {
            // 获取信息
            Properties properties = new Properties();
            InputStream inp = TrustPayUtil.class.getClassLoader().getResourceAsStream("eps.properties");
            properties.load(inp);
            appId = properties.getProperty("trust.pay.appId", "");
            appKey = properties.getProperty("trust.pay.appKey", "");
            sourceNo = properties.getProperty("trust.pay.source", "");
            merchantNo = properties.getProperty("trust.pay.merchant", "");
            trustPayUrl = properties.getProperty("trust.pay.url", "");
            notifyUrl = properties.getProperty("trust.notify.url", "");
        } catch (NumberFormatException e) {
            LOGGER.error("获取信通宝配置出错!", e);
        } catch (IOException e) {
            LOGGER.error("获取信通宝配置出错!", e);
        }
    }

    public static String buildReqExtendParams(TrustPayPO trustPayPO, String createTime) {
        StringBuffer sb = new StringBuffer();
        sb.append("ownerId-").append(trustPayPO.getOwnerId());
        sb.append("!communityId-").append(trustPayPO.getCommunityId());
        sb.append("!ownerUserId-").append(trustPayPO.getOwnerUserId());
        sb.append("!paySource-").append(trustPayPO.getPaySource());
        sb.append("!loginName-").append(trustPayPO.getLoginName());
        sb.append("!createTime-").append(createTime);
        return sb.toString();
    }

    public static Map<String, String> parseReqInnerParam(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        String[] kvs = s.split("!");
        for (String ss : kvs) {
            String[] kv = ss.split("-");
            if ("ownerId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("ownerId", kv[1]);
            } else if ("ownerUserId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("ownerUserId", kv[1]);
            } else if ("communityId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("communityId", kv[1]);
            } else if ("paySource".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("paySource", kv[1]);
            } else if ("createTime".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("createTime", kv[1]);
            } else if ("loginName".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("loginName", kv[1]);
            }
        }

        return map;
    }
}
