package com.yc.eps.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.third.AlipayRSAPO;
import com.yc.edsi.system.ISystemService;
import com.yc.eps.common.PayTools;

@Service
public class AlipayService {

    @Resource
    private ISystemService systemService;
    @Resource
    private ICommunityService communityService;
    @Resource
    private IAlipayDao alipayDao;

    @Value("#{eps['notify.url']}")
    private String notifyUrl;

    public String getClientMessage(AlipayRSAPO alipayRSAPO) throws EdsiException {

        ClientKeys clientKeys = AlipayService.buileClientKeys(alipayRSAPO.getCommunityId(), systemService,
                communityService, alipayRSAPO.getPayType());

        if (clientKeys == null) {
            throw new EdsiException("暂时无法使用支付宝支付！");
        }

        alipayRSAPO.setBody(PayTools.buildReqInnerParam(alipayRSAPO.getOwnerId(), alipayRSAPO.getCommunityId(),
                alipayRSAPO.getPayType(), alipayRSAPO.getClientPayId()));
        alipayRSAPO.setOutTradeNo(systemService.getId());
        alipayRSAPO.setNotifyUrl(notifyUrl);
        String info = this.getNewOrderInfo(alipayRSAPO, clientKeys);
        String sign = Rsa.sign(info, clientKeys.getPrivateKey());
        try {
            sign = URLEncoder.encode(sign, Constant.CHARSET);
        } catch (UnsupportedEncodingException e) {
        }

        info += "&sign=\"" + sign + "\"&" + "sign_type=\"RSA\"";

        return info;
    }

    private String getNewOrderInfo(AlipayRSAPO alipayRSAPO, ClientKeys clientKeys) {
        StringBuilder sb = new StringBuilder();
        // 合作者身份ID,不可空
        sb.append("partner=\"");
        // 卖家支付宝账号，不可空
        sb.append(clientKeys.getPartner());
        sb.append("\"&seller_id=\"");
        sb.append(clientKeys.getSeller());
        sb.append("\"&out_trade_no=\"");
        sb.append(alipayRSAPO.getOutTradeNo());
        sb.append("\"&subject=\"");
        sb.append(alipayRSAPO.getSubject());
        sb.append("\"&body=\"");
        sb.append(alipayRSAPO.getBody());
        sb.append("\"&total_fee=\"");
        sb.append(alipayRSAPO.getTotalFee());
        sb.append("\"&notify_url=\"");

        // 网址需要做URL编码
        try {
            sb.append(URLEncoder.encode(alipayRSAPO.getNotifyUrl(), Constant.CHARSET));
        } catch (UnsupportedEncodingException e) {
        }
        // 接口名称，固定值，不可空
        sb.append("\"&service=\"mobile.securitypay.pay");
        sb.append("\"&_input_charset=\"");
        sb.append(Constant.CHARSET);

        // 支付类型。默认值为：1（商品购买）。不可空
        sb.append("\"&payment_type=\"1");

        // 未付款交易的超时时间，可空
        sb.append("\"&it_b_pay=\"30m");
        sb.append("\"");

        return new String(sb);
    }

    public static ClientKeys buileClientKeys(long communityId, ISystemService systemService,
            ICommunityService communityService, int payType) throws EdsiException {
        CommunityPO communityPO = communityService.getById(communityId);
        if (CommunityPO.ADVERTAT_PLAT.equals(communityPO.getAdvertAt()) && payType != IPaymentService.PAY_TYPE_FEE) {
            // 收款方是平台，且不是物业费
            return new ClientKeys(true);
        } else if (CommunityPO.ADVERTAT_COMM.equals(communityPO.getAdvertAt())
                || payType == IPaymentService.PAY_TYPE_FEE) {
            // 收款方是物业或者缴纳物业费
            String keys = systemService.getDictionaryValue(ISystemService.COMM_ALIPAY, String.valueOf(communityId));
            if (StringUtils.isBlank(keys)) {
                throw new EdsiException("暂时无法使用支付宝支付！");
            }
            // key规则 partner-seller-privateKey-publicKey
            String[] ss = keys.split("-");
            if (ss == null || ss.length != 4) {
                throw new EdsiException("暂时无法使用支付宝支付！");
            }

            ClientKeys clientKeys = new ClientKeys(false);
            clientKeys.setPartner(ss[0]);
            clientKeys.setSeller(ss[1]);
            clientKeys.setPrivateKey(ss[2]);
            clientKeys.setPublicKey(ss[3]);
            return clientKeys;
        }
        return null;
    }
}
