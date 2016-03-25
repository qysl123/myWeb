package com.yc.eps.unipay;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unionpay.acp.sdk.SDKConfig;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.third.UniPayPO;
import com.yc.edsi.system.ISystemService;
import com.yc.eps.common.PayTools;

@Service
public class UniPayService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UniPayService.class);

    @Resource
    private ISystemService systemService;

    @Value("#{eps['uni.backUrl']}")
    private String backUrl;

    @Value("#{eps['uni.merId']}")
    private String merId;

    @Value("#{eps['uni.version']}")
    private String version;

    public String getUniTn(UniPayPO uniPO) throws EdsiException {
        // 将元转为分
        Long totalFee = Math.round(Tools.multiply(uniPO.getTotalFee(), 100L));

        Map<String, String> data = new HashMap<String, String>();
        data.put("version", version);
        data.put("encoding", Constant.CHARSET);
        data.put("signMethod", "01");
        // 交易类型 01-消费
        data.put("txnType", "01");
        // 交易子类型 01:自助消费 02:订购 03:分期付款
        data.put("txnSubType", "01");
        // 业务类型 000202 B2B业务
        data.put("bizType", "000202");
        // 渠道类型 07-互联网渠道
        data.put("channelType", "07");
        // 商户/收单后台接收地址 必送
        data.put("backUrl", backUrl);
        // 接入类型:商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
        data.put("accessType", "0");
        // 商户号码
        data.put("merId", merId);
        // 订单号 商户根据自己规则定义生成，每订单日期内不重复
        data.put("orderId", UniPayCore.getUniPaiId(systemService.getId(), 15));
        // 订单发送时间 格式： YYYYMMDDhhmmss 商户发送交易时间，根据自己系统或平台生成
        data.put("txnTime", TimeUtil.getNowDate(TimeUtil.YYYYMMDDHHMMSS));
        // 交易金额 分
        data.put("txnAmt", String.valueOf(totalFee));
        // 交易币种
        data.put("currencyCode", "156");
        data.put("customerIp", uniPO.getIpAddr());

        data.put("reqReserved", PayTools.buildReqInnerParam(uniPO.getOwnerId(), uniPO.getCommunityId(),
                IPaymentService.PAY_TYPE_RECHARGE, uniPO.getClientPayId()));

        LOGGER.info("调用银联支付参数：{}", data);
        Map<String, String> submitFromData = UniPayCore.signData(data);

        Map<String, String> resmap = UniPayCore.submitUrl(submitFromData, SDKConfig.getConfig().getAppRequestUrl());

        if (resmap == null) {
            return null;
        }
        return resmap.get("tn");
    }

}
