package com.yc.eps.weixin;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.yc.commons.HttpUtil;
import com.yc.commons.HttpUtil.HttpResponse;
import com.yc.commons.Tools;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.third.IWeixinPayService;
import com.yc.edsi.payment.third.RechargePayPO;
import com.yc.edsi.payment.third.WeixinAppKeys;
import com.yc.edsi.payment.third.WeixinPayPO;
import com.yc.edsi.system.DictionaryPO;
import com.yc.edsi.system.ISystemService;
import com.yc.eps.common.PayCatchTools;
import com.yc.eps.common.PayTools;

@Service
public class WeixinPayService implements IWeixinPayService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WeixinPayService.class);

    private final static String WEB_APPSECRET = "e1bc1f510ab11e5e9e152abc1fedc537";

    @Resource
    private ISystemService systemService;
    @Resource
    private ICommunityService communityService;

    @Value("#{eps['weixin.serverUrl']}")
    private String serverUrl;
    @Value("#{eps['weixin.notifyUrl']}")
    private String notifyUrl;

    @Override
    public WeixinPayPO getWeixinReq(String ip, String desc, String pkgName, String clientPayId, double p,
            OwnerUserPO ownerUserPO, String code, String openId) throws EdsiException {
        SortedMap<String, String> paramMap = new TreeMap<String, String>();

        int payType = 3;
        if (StringUtils.isNotBlank(clientPayId)) {
            RechargePayPO rechargePayPO = PayCatchTools.getRPP(clientPayId);
            payType = rechargePayPO.getTypePay();
        }

        WeixinAppKeys appKeys = this.getAppKeys(pkgName, payType, ownerUserPO.getCommunityId());

        if (appKeys == null) {
            throw new EdsiException("获取微信支付配置文件失败");
        }

        // 将元转为分
        Long totalFee = Math.round(Tools.multiply(p, 100L));
        String noncestr = String.valueOf(systemService.getId());

        paramMap.put("appid", appKeys.getAppId());
        paramMap.put("mch_id", appKeys.getMchId());
        // 随机数
        paramMap.put("nonce_str", noncestr);
        paramMap.put("body", desc);
        paramMap.put("out_trade_no", noncestr);
        paramMap.put("total_fee", String.valueOf(totalFee));
        paramMap.put("spbill_create_ip", ip);
        paramMap.put("notify_url", notifyUrl);
        if (StringUtils.isNotBlank(code)) {
            paramMap.put("trade_type", "JSAPI");
            openId = this.getOpenId(appKeys.getAppId(), code);
            if (StringUtils.isNotBlank(openId)) {
                paramMap.put("openid", openId);
            } else {
                throw new EdsiException("获取微信支付配置文件失败");
            }
        } else if (StringUtils.isNotBlank(openId)) {
            paramMap.put("trade_type", "JSAPI");
            paramMap.put("openid", openId);
        } else {
            paramMap.put("trade_type", "APP");
        }
        paramMap.put("attach", PayTools.buildReqInnerParam(ownerUserPO.getOwnerId(), ownerUserPO.getCommunityId(),
                payType, clientPayId));
        paramMap.put("sign", WeixinUtil.createSign(paramMap, appKeys.getPartnerkey()));

        String xmlStr = WeixinUtil.createXML(paramMap);
        HttpResponse response = null;
        try {
            response = HttpUtil.post(serverUrl, xmlStr);
            SortedMap<String, String> resultMap = WeixinUtil.parseXML(response.getBody());
            String prepayId = resultMap.get("prepay_id");
            if (StringUtils.isBlank(prepayId)) {
                if (response != null) {
                    LOGGER.error("访问微信没有返回prepay_id，请求信息：{}，响应信息：{}", xmlStr, response.toString());
                }
                throw new EdsiException("调用微信支付出现异常");
            }
            paramMap.clear();
            if (StringUtils.isBlank(openId)) {
                // APP支付
                paramMap.put("appid", appKeys.getAppId());
                paramMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                paramMap.put("noncestr", noncestr);
                paramMap.put("partnerid", appKeys.getMchId());
                paramMap.put("prepayid", prepayId);
                paramMap.put("package", "Sign=WXPay");
                paramMap.put("sign", WeixinUtil.createSign(paramMap, appKeys.getPartnerkey()));
            } else {
                // 网页支付
                paramMap.put("appId", appKeys.getAppId());
                paramMap.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
                paramMap.put("nonceStr", noncestr);
                paramMap.put("package", "prepay_id=" + prepayId);
                paramMap.put("signType", "MD5");
                paramMap.put("sign", WeixinUtil.createSign(paramMap, appKeys.getPartnerkey()));

                paramMap.put("timestamp", paramMap.get("timeStamp"));
                paramMap.put("noncestr", paramMap.get("nonceStr"));
                paramMap.put("appid", paramMap.get("appId"));
                paramMap.put("openid", openId);
            }

            return WeixinPayPO.buildReqPO(paramMap);

        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            if (response != null) {
                LOGGER.error("访问微信出现错误，请求信息：{}，响应信息：{}", xmlStr, response.toString());
            }
            LOGGER.error("", e);
        }

        return null;
    }

    public String getPartnerkey(String appId) throws EdsiException {
        String pAndM = this.getPAndM(appId);
        if (StringUtils.isBlank(pAndM)) {
            throw new EdsiException("调用微信支付出现异常");
        }

        String[] values = pAndM.split("-");
        return values[0];

    }

    private String getPAndM(String appId) throws EdsiException {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        List<DictionaryPO> list = systemService.getDictionaryPOByType(ISystemService.PAY_WEIXIN_PARAM);
        if (list != null) {
            for (DictionaryPO dictionaryPO : list) {
                if (appId.equals(dictionaryPO.getValue())) {
                    return dictionaryPO.getValue2();
                }
            }
        }
        list = systemService.getDictionaryPOByType(ISystemService.COMM_WEIPAY);
        if (list != null) {
            for (DictionaryPO dictionaryPO : list) {
                if (appId.equals(dictionaryPO.getValue())) {
                    return dictionaryPO.getValue2();
                }
            }
        }
        return null;

    }

    @Override
    public String getOpenId(String code, String pkgName, int payType, long communityId) throws EdsiException {
        WeixinAppKeys weixinAppKeys = this.getAppKeys(pkgName, payType, communityId);
        if (weixinAppKeys != null) {
            return this.getOpenId(weixinAppKeys.getAppId(), code);
        }
        return null;
    }

    @Override
    public WeixinAppKeys getAppKeys(String pkgName, int payType, long communityId) throws EdsiException {
        if (StringUtils.isBlank(pkgName) || communityId <= 0) {
            return null;
        }

        CommunityPO communityPO = communityService.getById(communityId);
        if (CommunityPO.ADVERTAT_PLAT.equals(communityPO.getAdvertAt()) && payType != IPaymentService.PAY_TYPE_FEE) {
            // 收款方是平台，且不是物业费
            List<DictionaryPO> list = systemService.getDictionaryPOByType(ISystemService.PAY_WEIXIN_PARAM);
            if (list != null) {
                for (DictionaryPO dictionaryPO : list) {
                    if (StringUtils.isNotBlank(dictionaryPO.getValue2())
                            && dictionaryPO.getValue2().indexOf(pkgName) > 0) {
                        return this.buildAppKeys(dictionaryPO);
                    }
                }
            }
        } else if (CommunityPO.ADVERTAT_COMM.equals(communityPO.getAdvertAt())
                || payType == IPaymentService.PAY_TYPE_FEE) {
            // 收款方是物业
            List<DictionaryPO> list = systemService.getDictionaryPOByType(ISystemService.COMM_WEIPAY);
            if (list != null) {
                for (DictionaryPO dictionaryPO : list) {
                    if (dictionaryPO.getKey().equals(String.valueOf(communityId))
                            && dictionaryPO.getValue2().indexOf(pkgName) > 0) {
                        return this.buildAppKeys(dictionaryPO);
                    }
                }
            }
        } else {
            return null;
        }

        return null;
    }

    private WeixinAppKeys buildAppKeys(DictionaryPO dictionaryPO) throws EdsiException {
        WeixinAppKeys appKeys = new WeixinAppKeys();
        appKeys.setAppId(dictionaryPO.getValue());

        String pAndM = dictionaryPO.getValue2();
        if (StringUtils.isBlank(pAndM)) {
            throw new EdsiException("调用微信支付出现异常");
        }
        String[] values = pAndM.split("-");
        appKeys.setPartnerkey(values[0]);
        appKeys.setMchId(values[1]);
        return appKeys;
    }

    private String getOpenId(String appId, String code) {
        StringBuffer sb = new StringBuffer();
        sb.append("https://api.weixin.qq.com/sns/oauth2/access_token?appid=");
        sb.append(appId).append("&secret=").append(WEB_APPSECRET);
        sb.append("&code=").append(code).append("&grant_type=authorization_code");
        try {
            HttpResponse response = HttpUtil.get(sb.toString());
            LOGGER.debug("或取OPENID返回值：{}", response.toString());
            JSONObject obj = JSONObject.parseObject(response.getBody());
            return obj.get("openid").toString();
        } catch (Exception e) {
            LOGGER.error("发送请求失败，{}", sb.toString());
            LOGGER.error("", e);
        }
        return null;
    }
}
