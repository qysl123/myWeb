package com.yc.eps.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.yc.commons.HttpUtil;
import com.yc.commons.HttpUtil.HttpResponse;
import com.yc.commons.TimeUtil;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.fee.IFeeService;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.payment.third.AlipayRSAPO;
import com.yc.edsi.payment.third.RechargePayPO;
import com.yc.edsi.system.ISystemService;
import com.yc.eps.common.PayCatchTools;
import com.yc.eps.common.PayTools;

public class NotifyReceiverServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final static Logger LOGGER = LoggerFactory.getLogger(NotifyReceiverServlet.class);

    private final static Map<Long, Long> HAS_PAY_CACHE = new TreeMap<Long, Long>();

    private ISystemService systemService;
    private IAlipayDao alipayDao;
    private IPaymentService paymentService;
    private ICommunityService communityService;
    private IFeeService feeService;
    private IOwnerService ownerService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        alipayDao = wac.getBean(IAlipayDao.class);
        paymentService = wac.getBean(IPaymentService.class);
        systemService = wac.getBean(ISystemService.class);
        communityService = wac.getBean(ICommunityService.class);
        feeService = wac.getBean(IFeeService.class);
        ownerService = wac.getBean(IOwnerService.class);
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(Constant.CHARSET);
        String ipAddr = PayTools.getIpAddr(request);
        LOGGER.info("RSANotifyReceiverServlet,接收到支付宝通知 请求IP [{}]", ipAddr);

        PrintWriter out = response.getWriter();

        Map<String, String> reqParam = PayTools.getAllRequestParam(request);
        Map<String, String> innerMap = PayTools.parseReqInnerParam(reqParam.get("body"));

        LOGGER.info("RSANotifyReceiverServlet,接收到支付宝通知 请求参数 [{}]", reqParam);

        long outTradeNo = Long.parseLong(reqParam.get("out_trade_no"));
        String tradeStatus = reqParam.get("trade_status");
        if ((!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus)) && (!"TRADE_FINISHED".equalsIgnoreCase(tradeStatus))) {
            out.print("success");
            return;
        }

        boolean verified = false;

        try {
            ClientKeys clientKeys = AlipayService.buileClientKeys(Long.parseLong(innerMap.get("communityId")),
                    systemService, communityService, Integer.parseInt(innerMap.get("payType")));
            if (this.checkSign(reqParam, clientKeys.getPublicKey())) {
                if ("true".equalsIgnoreCase(this.checkId(reqParam.get("notify_id"), clientKeys.getPartner()))) {
                    verified = true;
                } else {
                    LOGGER.error("HTTP验证签名出错outTradeNo：{}", outTradeNo);
                }
            } else {
                LOGGER.error("RSA验证签名出错outTradeNo：{}", outTradeNo);
            }
        } catch (Exception e) {
            LOGGER.error(NotifyReceiverServlet.class.getName(), e);
            LOGGER.error("出错outTradeNo：{}", outTradeNo);
        }

        // 验证签名未通过
        if (!verified) {
            out.print("fail");
            LOGGER.info("RSANotifyReceiverServlet,签名验证失败！{}", outTradeNo);
            return;
        }

        try {
            synchronized (HAS_PAY_CACHE) {
                if (HAS_PAY_CACHE.containsValue(outTradeNo)) {
                    LOGGER.info("支付宝支付重复通知：{}", outTradeNo);
                    out.print("success");
                    return;
                }
                if (alipayDao.getFinshTrade(outTradeNo) > 0) {
                    // 重复通知
                    LOGGER.info("支付宝支付重复通知：{}", outTradeNo);
                    out.print("success");
                    return;
                }
                // 清除过期的缓存
                clearCacheMap();
                HAS_PAY_CACHE.put(System.currentTimeMillis(), outTradeNo);
            }
            AlipayRSAPO alipayRSAPO = new AlipayRSAPO();
            alipayRSAPO.setTradeStatus(tradeStatus);
            alipayRSAPO.setNotifyId(reqParam.get("notify_id"));
            alipayRSAPO.setOutTradeNo(outTradeNo);
            alipayRSAPO.setCommunityId(Long.parseLong(innerMap.get("communityId")));
            alipayRSAPO.setOwnerId(Long.parseLong(innerMap.get("ownerId")));
            alipayRSAPO.setPayType(Integer.parseInt(innerMap.get("payType")));
            RechargePayPO rechargePayPO = PayCatchTools.getRPP(innerMap.get("clientPayId"));
            if (rechargePayPO != null) {
                alipayRSAPO.setIds(PayTools.longListToString(rechargePayPO.getIds(), 4000));
            }
            alipayRSAPO.setClientPayId(innerMap.get("clientPayId"));
            alipayRSAPO.setTotalFee(Double.parseDouble(reqParam.get("total_fee")));
            alipayRSAPO.setCreateTime(TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS());
            alipayRSAPO.setMapBody(reqParam.toString());
            alipayDao.insertTrade(alipayRSAPO);

            CommunityPO communityPO = communityService.getById(alipayRSAPO.getCommunityId());
            if ((!innerMap.get("payType").equals(String.valueOf(IPaymentService.PAY_TYPE_FEE)))
                    || CommunityPO.ADVERTAT_COMM.equals(communityPO.getAdvertAt())) {
                // 非物业费直接存入个人账户，物业独立支付，也进个人账户
                OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
                ownerPaymentPO.setAmount(alipayRSAPO.getTotalFee());
                ownerPaymentPO.setOwnerId(alipayRSAPO.getOwnerId());
                ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_RECHARGE);
                ownerPaymentPO.setSysUserOptFlag("1");
                ownerPaymentPO.setCommunityId(alipayRSAPO.getCommunityId());
                ownerPaymentPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
                ownerPaymentPO.setBillNo("01");// 充值统计时需要票据号，wangxv
                                               // 2014年5月16日15:44:29
                ownerPaymentPO.setCreateUser("zhifubao");
                ownerPaymentPO.setCash(ownerPaymentPO.getAmount());

                ownerPaymentPO.setPayType("支付宝");
                ownerPaymentPO.setDes("支付宝充值,outTradeNo:" + outTradeNo);
                ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_ALIPAYRECHARGE);

                paymentService.ownerRecharge(ownerPaymentPO);
            } else {
                // 物业费，且专户专用
                PayTools.payFee(innerMap, feeService, ownerService);
            }

        } catch (Exception e) {
            LOGGER.error(NotifyReceiverServlet.class.getName(), e);
        }

        out.print("success");

    }

    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }

    private static int id = 0;

    public synchronized static String getId() {
        String result = String.valueOf(System.currentTimeMillis());
        if (id >= 999) {
            id = 0;
        }
        id = id + 1;
        if (id < 10) {
            return result + "00" + id;
        }
        if (id < 100) {
            return result + "0" + id;
        }

        return result + id;
    }

    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&partner=";

    private String checkId(String notifyId, String partner) throws NumberFormatException, EdsiException {

        try {
            HttpResponse response = HttpUtil.get(HTTPS_VERIFY_URL + partner + "&notify_id=" + notifyId);
            return response.getBody();
        } catch (Exception e) {
            LOGGER.error(getClass().getName(), e);
        }
        return null;
    }

    private boolean checkSign(Map<String, String> params, String singKey) {
        if (params == null || params.size() <= 0) {
            return false;
        }

        Map<String, String> signMap = new HashMap<String, String>();

        for (String paramKey : params.keySet()) {
            String value = params.get(paramKey);
            if (StringUtils.isBlank(value) || paramKey.equalsIgnoreCase("sign")
                    || paramKey.equalsIgnoreCase("sign_type")) {
                continue;
            }
            signMap.put(paramKey, value);
        }

        List<String> keys = new ArrayList<String>(signMap.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = signMap.get(key);

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return Rsa.doCheck(prestr, params.get("sign"), singKey);
    }

    public static void clearCacheMap() {
        if (HAS_PAY_CACHE.size() > 500) {
            HAS_PAY_CACHE.clear();
        }
    }
}
