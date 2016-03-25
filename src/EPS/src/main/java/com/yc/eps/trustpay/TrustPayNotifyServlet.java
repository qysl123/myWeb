package com.yc.eps.trustpay;

import com.alibaba.fastjson.JSONObject;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.third.TrustPayPO;
import com.yc.eps.common.PayTools;
import org.open.sdk.java.tools.SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TrustPayNotifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(TrustPayNotifyServlet.class);

    private final static Map<Long, String> HAS_PAY_CACHE = new TreeMap<>();

    private IPaymentService paymentService;
    private ITrustPayDao trustPayDao;
    private IOwnerService ownerService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        paymentService = wac.getBean(IPaymentService.class);
        trustPayDao = wac.getBean(ITrustPayDao.class);
        ownerService = wac.getBean(IOwnerService.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TrustPayBaseResponse response = new TrustPayBaseResponse();
        Map<String, String> reqParam = PayTools.getAllRequestParam(req);
        Map<String, String> reqMap;
        if (null != reqParam && !reqParam.isEmpty()) {
            Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
            reqMap = new HashMap<>(reqParam.size());
            while (it.hasNext()) {
                Entry<String, String> e = it.next();
                String key = e.getKey();
                String value = e.getValue();
                reqMap.put(key, value);
                LOGGER.info("信通宝请求参数，KEY：{}，VALUE：{}", key, value);
            }
        } else {
            LOGGER.info("收到信通宝请求参数为空{}", req.getRemoteHost());
            response.setCode(TrustPayBaseResponse.CODE_ERROR_PARAM);
            resp.getOutputStream().write(JSONObject.toJSONString(response).getBytes());
            return;
        }

        TrustPayPO trustPayPO;

        try {
            trustPayPO = TrustPayPO.buildTrustPayPO(reqMap);
            Map<String, String> parseMap = TrustPayUtil.parseReqInnerParam(reqParam.get("extendParams"));
            trustPayPO.setOwnerId(Long.valueOf(parseMap.get("ownerId")));
            trustPayPO.setCommunityId(Long.valueOf(parseMap.get("communityId")));
            trustPayPO.setOwnerUserId(Long.valueOf(parseMap.get("ownerUserId")));
            trustPayPO.setPaySource(parseMap.get("paySource"));
            trustPayPO.setLoginName(parseMap.get("loginName"));
        } catch (Exception e) {
            LOGGER.error("系统处理出现异常", e);
            response.setCode(TrustPayBaseResponse.CODE_ERROR_PARAM);
            resp.getOutputStream().write(JSONObject.toJSONString(response).getBytes());
            return;
        }

        // 验证签名
        if (!SignatureUtils.validateSignature(reqMap, TrustPayUtil.appKey)) {
            trustPayPO.setSingResult("0");
            trustPayDao.insertTrustPayTrade(trustPayPO);
            LOGGER.info("验证签名结果[失败].{}", reqMap.toString());

            response.setCode(TrustPayBaseResponse.CODE_ERROR_SIGNATURE);
            resp.getOutputStream().write(JSONObject.toJSONString(response).getBytes());
            return;
        } else {
            trustPayPO.setSingResult("1");
            LOGGER.info("验证签名结果[成功].{}", reqMap.toString());
        }

        synchronized (HAS_PAY_CACHE) {
            if (HAS_PAY_CACHE.containsValue(trustPayPO.getOutTradeNo())) {
                LOGGER.info("信通宝支付重复通知：{}", trustPayPO.toString());
                return;
            }

            if (trustPayDao.getTrustFinishTrade(trustPayPO.getOutTradeNo()) > 0) {
                LOGGER.info("信通宝支付重复通知：{}", trustPayPO.toString());
                return;
            }
            // 清除过期的缓存
            clearCacheMap();
            HAS_PAY_CACHE.put(System.currentTimeMillis(), trustPayPO.getOutTradeNo());
        }
        trustPayDao.insertTrustPayTrade(trustPayPO);

//        try {
//            paymentService.ownerRecharge(ownerPaymentPO);
//        } catch (Exception e) {
//            LOGGER.error("系统处理出现异常", e);
//            LOGGER.error("异常信息" + ownerPaymentPO.toString());
//        }

        resp.getOutputStream().write(JSONObject.toJSONString(response).getBytes());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("信通宝跳转成功!");
    }

    public static void clearCacheMap() {
        if (HAS_PAY_CACHE.size() > 500) {
            HAS_PAY_CACHE.clear();
        }
    }

}
