package com.zk.service;

import com.zk.common.Tools;
import com.zk.entity.TrustPayBaseResponse;
import com.zk.entity.TrustPayPO;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.open.sdk.java.common.enums.OpenSdkSupportHttpMethod;
import org.open.sdk.java.service.OpenApiSinatureAccessService;
import org.open.sdk.java.tools.MyStringUtil;

import java.util.HashMap;
import java.util.Map;

public class TrustPayService {

    private static final String appId = "1317d47de818422d8fb08a60923711b0";
    private static final String appKey = "efda9aa733844c48a10cbc30a6a59d62";
    private static final String sourceNo = "mer600220160000010601016160010";
    private static final String merchantNo="mer600220160000010601016160010";
    private static final String channel = "H5";
    private static final String request_url="https://demo-openapi.wjjr.cc";

    /**
     * 获取accessToken
     * @return
     */
    public static String getAccessToken() {
        String result = "";

        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", appId);
        params.put("scope", "ACCESSTOKEN");
        params.put("sourceNo", sourceNo);
        params.put("channel", channel);
        String url = request_url + "/auth/service_access_token";
        Object resultObj = OpenApiSinatureAccessService.openApiSinatureAccess(params, url, OpenSdkSupportHttpMethod.HTTP_GET_METHOD.getMehtod(), appKey);
        JSONObject json = JSONObject.fromObject(resultObj.toString());
        int code = json.getInt("code");
        if (code == 1) {
            JSONObject json1 = json.getJSONObject("data");
            String accessToken = json1.getString("access_token");
            if (MyStringUtil.isNotEmpty(accessToken)) {
                result = accessToken;
            }
        }
        return result;
    }

    /**
     * 创建会员
     * @param accessToken
     */
    public static void createUser(String accessToken) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", appId);
        params.put("accessToken", accessToken);
        params.put("sourceNo", sourceNo);
//        params.put("loginName", loginName);
//        params.put("outCustomerId", outCustomerId);
//        params.put("mobile", mobile);
        params.put("channel", channel);

        String url = request_url + "/service/createUser";
        Object result = OpenApiSinatureAccessService.openApiSinatureAccess(params, url, OpenSdkSupportHttpMethod.HTTP_POST_METHOD.getMehtod(), appKey);
        JSONObject json = JSONObject.fromObject(result.toString());
        int code = json.getInt("code");
        if (code == 1) {
            System.out.println("创建会员成功");
        }
    }

    /**
     * 获取收银台
     * @param accessToken
     * @param longinName
     * @return
     */
    public static JSONObject testCreateCashTokenAndGetWidgetPage(TrustPayPO trustPayPO, String accessToken) {
        Map<String, String> params = new HashMap<String, String>();
        // 将元转为分
        Long totalFee = Math.round(Tools.multiply(trustPayPO.getAmount().doubleValue(), 100L));

        String outTradeNo = "ZXH" + System.currentTimeMillis();
        params.put("mobile", "");
        params.put("appId", appId);
        params.put("accessToken", accessToken);
        params.put("channel", channel);

        params.put("productNo", "345");
        params.put("scenesCode", "020200002");
        params.put("orderDesc", "性能测试测试");
        params.put("orderName", "性能测试的订单");
        params.put("urlKey", "cash_desk");
        params.put("outTradeNo", trustPayPO.getOutTradeNo());
        params.put("paySource", "IOS");
        params.put("amount", String.valueOf(totalFee));
        params.put("productName", "Loadrunner产品盯订单");
        params.put("loginName", String.valueOf(trustPayPO.getPhone()));
        params.put("orderBeginTime", "2015-09-22 11:00:30");// 2015-09-22

        params.put("childMerchantNo", merchantNo);
        params.put("merchantNo", merchantNo);
        params.put("sourceNo", sourceNo);
        params.put("currency", "CNY");
        params.put("orderNotifyUrl", "http://www.baidu.com/notify");
        params.put("orderFrontNotifyUrl", "http://yxs.im/sJOrs4");

        params.put("extendParams", "121511120000?thsds=1234");
        params.put("outCustomerId", String.valueOf(trustPayPO.getPhone()));
        String url = request_url + "/service/cash_desk";
        Object result = OpenApiSinatureAccessService.openApiSinatureAccess(params, url, OpenSdkSupportHttpMethod.HTTP_POST_METHOD.getMehtod(), appKey);
        System.out.println(result);
        JSONObject json = JSONObject.fromObject(result.toString());
        JSONObject.toBean(json, TrustPayBaseResponse.class);
        int code = json.getInt("code");
        if (code == 1) {
            JSONObject json1 = json.getJSONObject("data");
            json1 = json1.getJSONObject("resultInfo");
            return json1;
        }
        return null;
    }
}

