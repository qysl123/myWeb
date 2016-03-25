package com.yc.eps.trustpay;

import com.alibaba.fastjson.JSONObject;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.third.ITrustPayService;
import com.yc.edsi.payment.third.TrustPayPO;
import org.apache.commons.lang3.StringUtils;
import org.open.sdk.java.common.enums.OpenSdkSupportHttpMethod;
import org.open.sdk.java.service.OpenApiSinatureAccessService;
import org.open.sdk.java.tools.MyStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class TrustPayService implements ITrustPayService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrustPayService.class);

    @Resource
    private ITrustPayDao trustPayDao;

    @Override
    public String doTrustPay(TrustPayPO trustPayPO) throws EdsiException {
        String result = "";
        trustPayPO.setOutTradeNo("ZXH" + System.currentTimeMillis() + (int) (Math.random() * 1000));
        //验证token
        String accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            throw new EdsiException("获取信通宝token出现出错!");
        }
        //判断是否有信通宝账号
        String loginName = trustPayPO.getLoginName();
        TrustPayBaseResponse response;
        int tradeCount = trustPayDao.getTradeCountByLoginName(loginName);
        if (tradeCount <= 0) {
            response = createTrustPayUser(accessToken, trustPayPO);
            if (!TrustPayBaseResponse.CODE_SUCCESS.equals(response.getCode())) {
                LOGGER.error("创建信通宝会员出现异常, 返回的报文:{}", JSONObject.toJSONString(response));
                throw new EdsiException("创建信通宝会员出现出错!");
            }
        }

        JSONObject resultObj = getCashDesk(trustPayPO, accessToken);
        if (resultObj != null) {
            result = resultObj.getString("widgetPageUrl");
        }
        return result;
    }

    /**
     * 获取accessToken
     *
     * @return
     */
    private String getAccessToken() {
        String result = "";

        Map<String, String> params = new HashMap<>();
        params.put("appId", TrustPayUtil.appId);
        params.put("scope", "ACCESSTOKEN");
        params.put("sourceNo", TrustPayUtil.sourceNo);
        params.put("channel", "H5");
        String url = TrustPayUtil.trustPayUrl + "/auth/service_access_token";
        LOGGER.info("调用信通宝获取token接口参数：{}", params);
        Object resultObj = OpenApiSinatureAccessService.openApiSinatureAccess(params, url, OpenSdkSupportHttpMethod.HTTP_GET_METHOD.getMehtod(), TrustPayUtil.appKey);
        TrustPayBaseResponse response = JSONObject.parseObject(resultObj.toString(), TrustPayBaseResponse.class);
        if (TrustPayBaseResponse.CODE_SUCCESS.equals(response.getCode())) {
            JSONObject obj = (JSONObject) response.getData();
            String accessToken = obj.getString("access_token");
            if (MyStringUtil.isNotEmpty(accessToken)) {
                result = accessToken;
            }
        } else {
            LOGGER.error("信通宝获取token出现异常, 返回的报文:{}", JSONObject.toJSONString(response));
        }
        return result;
    }

    /**
     * 创建信通宝用户
     *
     * @param accessToken
     * @param trustPayPO
     * @return
     */
    private TrustPayBaseResponse createTrustPayUser(String accessToken, TrustPayPO trustPayPO) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", TrustPayUtil.appId);
        params.put("accessToken", accessToken);
        params.put("sourceNo", TrustPayUtil.sourceNo);
        params.put("loginName", trustPayPO.getLoginName());
        params.put("outCustomerId", String.valueOf(trustPayPO.getOwnerId()));

        String url = TrustPayUtil.trustPayUrl + "/service/createUser";
        LOGGER.info("调用信通宝创建用户接口参数：{}", params);
        Object result = OpenApiSinatureAccessService.openApiSinatureAccess(params, url, OpenSdkSupportHttpMethod.HTTP_POST_METHOD.getMehtod(), TrustPayUtil.appKey);
        TrustPayBaseResponse response = JSONObject.parseObject(result.toString(), TrustPayBaseResponse.class);

        return response;
    }

    /**
     * 访问信通宝收银台
     *
     * @param trustPayPO
     * @param accessToken
     * @return
     */
    private JSONObject getCashDesk(TrustPayPO trustPayPO, String accessToken) {
        // 将元转为分
        Long amount = Math.round(Tools.multiply(trustPayPO.getAmount(), 100L));
        String createTime = TimeUtil.formatNow(TimeUtil.YYYY_MM_DD_HH_MM_SS);
        Map<String, String> data = new HashMap<>();
        //系统参数
        data.put("appId", TrustPayUtil.appId);
        data.put("accessToken", accessToken);
        data.put("sourceNo", TrustPayUtil.sourceNo);
        //业务参数
        data.put("urlKey", "cash_desk");
        data.put("loginName", trustPayPO.getLoginName());
        data.put("outTradeNo", trustPayPO.getOutTradeNo());
        data.put("merchantNo", TrustPayUtil.merchantNo);
        data.put("childMerchantNo", TrustPayUtil.merchantNo);
        data.put("amount", String.valueOf(amount));
        data.put("currency", trustPayPO.getCurrency());
        data.put("orderBeginTime", createTime);
        data.put("orderNotifyUrl", TrustPayUtil.notifyUrl);
        data.put("orderFrontNotifyUrl", TrustPayUtil.notifyUrl);
        data.put("paySource", trustPayPO.getPaySource());
        data.put("productNo", trustPayPO.getProductNo());
        data.put("productName", trustPayPO.getProductName());
        data.put("outCustomerId", String.valueOf(trustPayPO.getOwnerId()));
        data.put("orderDesc", trustPayPO.getOrderDesc());
        data.put("orderName", trustPayPO.getOrderName());
        data.put("extendParams", TrustPayUtil.buildReqExtendParams(trustPayPO, createTime));

        LOGGER.info("调用信通宝支付参数：{}", data);
        String url = TrustPayUtil.trustPayUrl + "/service/cash_desk";
        Object result = OpenApiSinatureAccessService.openApiSinatureAccess(data, url, OpenSdkSupportHttpMethod.HTTP_POST_METHOD.getMehtod(), TrustPayUtil.appKey);
        TrustPayBaseResponse response = JSONObject.parseObject(result.toString(), TrustPayBaseResponse.class);
        if (TrustPayBaseResponse.CODE_SUCCESS.equals(response.getCode())) {
            JSONObject dataObj = (JSONObject) response.getData();
            return (JSONObject) dataObj.get("resultInfo");
        } else {
            LOGGER.error("信通宝支付出现异常, 返回的报文:{}", JSONObject.toJSONString(response));
        }
        return null;
    }
}