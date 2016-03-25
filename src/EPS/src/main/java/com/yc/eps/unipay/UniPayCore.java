package com.yc.eps.unipay;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unionpay.acp.sdk.HttpClient;
import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKUtil;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.payment.third.UniPayPO;

public class UniPayCore {

    private final static Logger LOGGER = LoggerFactory.getLogger(UniPayCore.class);

    private UniPayCore() {

    }

    static {
        // 从classpath加载acp_sdk.properties文件
        SDKConfig.getConfig().loadPropertiesFromSrc();
    }

    static Map<String, String> signData(Map<String, String> contentData) {
        Map<String, String> submitFromData = new HashMap<String, String>();
        for (String key : contentData.keySet()) {
            if (StringUtils.isNotBlank(contentData.get(key))) {
                submitFromData.put(key, contentData.get(key));
            }
        }

        SDKUtil.sign(submitFromData, Constant.CHARSET);

        return submitFromData;
    }

    static Map<String, String> submitUrl(Map<String, String> submitFromData, String requestUrl) {
        String resultString = null;
        HttpClient hc = new HttpClient(requestUrl, 30000, 30000);
        try {
            int status = hc.send(submitFromData, Constant.CHARSET);
            if (200 == status) {
                resultString = hc.getResult();
            }
        } catch (Exception e) {
            LOGGER.error("银联支付出现异常", e);
            return null;
        }

        Map<String, String> resData = new HashMap<String, String>();

        if (StringUtils.isNotBlank(resultString)) {
            // 将返回结果转换为map
            resData = SDKUtil.convertResultStringToMap(resultString);
            if (SDKUtil.validate(resData, Constant.CHARSET)) {
                LOGGER.info("验证签名成功,返回的报文：{}", resultString);
            } else {
                LOGGER.info("验证签名失败,返回的报文：{}", resultString);
            }
        }
        return resData;
    }


    public static String getUniPaiId(long preId, int length) {
        String s = String.valueOf(preId);
        int sl = length - s.length();
        for (int i = 0; i < sl; i++) {
            s = "0" + s;
        }

        return s;
    }

    public static UniPayPO buildUniPayPO(Map<String, String> map) {
        if (map == null || map.size() <= 0) {
            return null;
        }

        UniPayPO uniPayPO = new UniPayPO();
        uniPayPO.setOutTradeNo(map.get("orderId"));
        uniPayPO.setTotalFee(Tools.divide(map.get("txnAmt"), "100"));
        uniPayPO.setCreateTime(TimeUtil.parseToString(map.get("txnTime"), TimeUtil.YYYYMMDDHHMMSS,
                TimeUtil.YYYY_MM_DD_HH_MM_SS));
        uniPayPO.setQueryId(map.get("queryId"));
        uniPayPO.setRespCode(map.get("respCode"));
        uniPayPO.setRespMsg("respMsg");
        uniPayPO.setAccNo(map.get("accNo"));
        uniPayPO.setInfo(map.toString());

        return uniPayPO;
    }
}
