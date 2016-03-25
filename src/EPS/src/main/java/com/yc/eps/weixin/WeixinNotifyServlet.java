package com.yc.eps.weixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.yc.edsi.commons.Constant;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.fee.IFeeService;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.payment.third.RechargePayPO;
import com.yc.edsi.payment.third.WeixinPayPO;
import com.yc.eps.common.PayCatchTools;
import com.yc.eps.common.PayTools;

public class WeixinNotifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(WeixinNotifyServlet.class);
    private final static String RESP_SINGFAIL = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名失败]]></return_msg></xml>";
    private final static String RESP_SUCCESS = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    private final static Map<Long, String> HAS_PAY_CACHE = new TreeMap<Long, String>();

    private static IPaymentService paymentService;
    private static IWeixinPayDao weixinPayDao;
    private static WeixinPayService weixinPayService;
    private ICommunityService communityService;
    private IFeeService feeService;
    private IOwnerService ownerService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (paymentService == null || weixinPayDao == null) {
            WebApplicationContext wac = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(getServletContext());
            paymentService = wac.getBean(IPaymentService.class);
            weixinPayDao = wac.getBean(IWeixinPayDao.class);
            weixinPayService = wac.getBean(WeixinPayService.class);
            communityService = wac.getBean(ICommunityService.class);
            feeService = wac.getBean(IFeeService.class);
            ownerService = wac.getBean(IOwnerService.class);
        }

    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(Constant.CHARSET);
        int size = req.getContentLength();
        InputStream is = req.getInputStream();
        byte[] reqBodyBytes = PayTools.readBytes(is, size);
        String res = new String(reqBodyBytes);
        String ip = PayTools.getIpAddr(req);
        LOGGER.info("收到微信支付请求，请求IP地址：{},请求报文{}:", ip, res);
        if (StringUtils.isBlank(res)) {
            return;
        }
        SortedMap<String, String> reqMap = WeixinUtil.parseXML(res);
        String sign = reqMap.get("sign");
        reqMap.remove("sign");
        String partnerkey = null;
        try {
            partnerkey = weixinPayService.getPartnerkey(reqMap.get("appid"));
        } catch (Exception e1) {
            LOGGER.info("收到微信支付请求，获取partnerkey出错。" + res, e1);
            resp.getOutputStream().write(RESP_SINGFAIL.getBytes());
            return;
        }

        String reqSign = WeixinUtil.createSign(reqMap, partnerkey);
        if (!sign.equals(reqSign)) {
            LOGGER.info("收到微信支付请求，验证签名失败，请求报文:{}", res);
            resp.getOutputStream().write(RESP_SINGFAIL.getBytes());
            return;
        }

        reqMap.putAll(PayTools.parseReqInnerParam(reqMap.get("attach")));
        WeixinPayPO weixinPayPO = WeixinPayPO.buildRespPO(reqMap);
        if (StringUtils.isNotBlank(reqMap.get("clientPayId"))) {
            weixinPayPO.setClientPayId(reqMap.get("clientPayId"));
            RechargePayPO rechargePayPO = PayCatchTools.getRPP(reqMap.get("clientPayId"));
            if (rechargePayPO != null) {
                weixinPayPO.setIds(PayTools.longListToString(rechargePayPO.getIds(), 4000));
                weixinPayPO.setPayType(rechargePayPO.getTypePay());
            }
        }

        if (res.length() < 2000) {
            weixinPayPO.setMapBody(res);
        } else {
            weixinPayPO.setMapBody(res.substring(0, 2000));
        }

        synchronized (HAS_PAY_CACHE) {
            String outTradeNo = reqMap.get("out_trade_no");
            if (HAS_PAY_CACHE.containsValue(outTradeNo)) {
                LOGGER.info("收到微信支付请求，重复通知，请求报文:{}", res);
                resp.getOutputStream().write(RESP_SUCCESS.getBytes());
                return;
            }

            // 防止缓存失效，需要再次判断数据库
            int count = weixinPayDao.getFinishTrade(outTradeNo);
            if (count > 0) {
                LOGGER.info("收到微信支付请求，重复通知，请求报文:{}", res);
                HAS_PAY_CACHE.put(System.currentTimeMillis(), outTradeNo);
                resp.getOutputStream().write(RESP_SUCCESS.getBytes());
                return;
            }

            // 清除过期的缓存
            clearCacheMap();
            HAS_PAY_CACHE.put(System.currentTimeMillis(), outTradeNo);
        }
        weixinPayDao.insertTrade(weixinPayPO);
        if (reqMap.containsKey("result_code") && "SUCCESS".equals(reqMap.get("result_code"))) {
            // 支付成功
            OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
            try {
                CommunityPO communityPO = communityService.getById(Long.parseLong(reqMap.get("communityId")));
                if ((!reqMap.get("payType").equals(String.valueOf(IPaymentService.PAY_TYPE_FEE)))
                        || CommunityPO.ADVERTAT_COMM.equals(communityPO.getAdvertAt())) {
                    // 非物业费直接存入个人账户，物业独立支付，也进个人账户

                    ownerPaymentPO.setOwnerId(weixinPayPO.getOwnerId());
                    ownerPaymentPO.setCommunityId(weixinPayPO.getCommunityId());

                    ownerPaymentPO.setAmount(weixinPayPO.getTotalFee());
                    ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_RECHARGE);
                    ownerPaymentPO.setSysUserOptFlag("1");
                    ownerPaymentPO.setCreateTime(weixinPayPO.getCreateTime());
                    ownerPaymentPO.setBillNo("01");// 充值统计时需要票据号，wangxv，2014年5月16日15:44:29
                    ownerPaymentPO.setCreateUser("weixinpay");
                    ownerPaymentPO.setCash(ownerPaymentPO.getAmount());

                    ownerPaymentPO.setPayType("微信");
                    ownerPaymentPO.setDes("微信充值,outTradeNo:" + weixinPayPO.getOutTradeNo());
                    ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_WXPAYRECHARGE);
                    paymentService.ownerRecharge(ownerPaymentPO);

                } else {
                    // 物业费，且专户专用
                    PayTools.payFee(reqMap, feeService, ownerService);
                }
            } catch (Exception e) {
                LOGGER.error("系统处理出现异常", e);
                LOGGER.error("异常信息" + ownerPaymentPO.toString());
            }
        }

        resp.getOutputStream().write(RESP_SUCCESS.getBytes());
    }

    public static void clearCacheMap() {
        if (HAS_PAY_CACHE.size() > 500) {
            HAS_PAY_CACHE.clear();
        }
    }
}
