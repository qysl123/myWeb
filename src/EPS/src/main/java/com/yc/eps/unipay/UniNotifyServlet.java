package com.yc.eps.unipay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.unionpay.acp.sdk.SDKConstants;
import com.unionpay.acp.sdk.SDKUtil;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.payment.third.UniPayPO;
import com.yc.eps.common.PayTools;

public class UniNotifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(UniNotifyServlet.class);

    private final static Map<Long, String> HAS_PAY_CACHE = new TreeMap<Long, String>();

    private IPaymentService paymentService;
    private IUniPayDao uniPayDao;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        paymentService = wac.getBean(IPaymentService.class);
        uniPayDao = wac.getBean(IUniPayDao.class);
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("ISO-8859-1");
        String encoding = req.getParameter(SDKConstants.param_encoding);
        Map<String, String> reqParam = PayTools.getAllRequestParam(req);
        Map<String, String> valideData = null;
        if (null != reqParam && !reqParam.isEmpty()) {
            Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
            valideData = new HashMap<String, String>(reqParam.size());
            while (it.hasNext()) {
                Entry<String, String> e = it.next();
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                value = new String(value.getBytes("ISO-8859-1"), encoding);
                valideData.put(key, value);
                LOGGER.info("银联请求参数，KEY：{}，VALUE：{}", key, value);
            }
        } else {
            LOGGER.info("收到银联请求参数为空{}", req.getRemoteHost());
            return;
        }

        UniPayPO uniPayPO = null;

        try {
            uniPayPO = UniPayCore.buildUniPayPO(valideData);
            Map<String, String> parseMap = PayTools.parseReqInnerParam(reqParam.get("reqReserved"));
            uniPayPO.setOwnerId(Long.valueOf(parseMap.get("ownerId")));
            uniPayPO.setCommunityId(Long.valueOf(parseMap.get("communityId")));
        } catch (Exception e) {
            LOGGER.error("系统处理出现异常", e);
            return;
        }
        // 验证签名
        if (!SDKUtil.validate(valideData, encoding)) {
            uniPayPO.setSingResult("0");
            uniPayDao.insertUniTrade(uniPayPO);
            LOGGER.info("验证签名结果[失败].{}", valideData.toString());
            return;
        } else {
            uniPayPO.setSingResult("1");
            LOGGER.info("验证签名结果[成功].{}", valideData.toString());
        }

        synchronized (HAS_PAY_CACHE) {
            if (HAS_PAY_CACHE.containsValue(uniPayPO.getOutTradeNo())) {
                LOGGER.info("银联支付重复通知：{}", valideData.toString());
                return;
            }

            if (uniPayDao.getUniFinishTrade(uniPayPO.getOutTradeNo()) > 0) {
                LOGGER.info("银联支付重复通知：{}", valideData.toString());
                return;
            }
            // 清除过期的缓存
            clearCacheMap();
            HAS_PAY_CACHE.put(System.currentTimeMillis(), uniPayPO.getOutTradeNo());
        }
        uniPayDao.insertUniTrade(uniPayPO);

        OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();

        ownerPaymentPO.setOwnerId(uniPayPO.getOwnerId());
        ownerPaymentPO.setCommunityId(uniPayPO.getCommunityId());

        ownerPaymentPO.setAmount(uniPayPO.getTotalFee());
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_RECHARGE);
        ownerPaymentPO.setSysUserOptFlag("1");
        ownerPaymentPO.setCreateTime(uniPayPO.getCreateTime());
        ownerPaymentPO.setBillNo("01");// 充值统计时需要票据号，wangxv，2014年5月16日15:44:29
        ownerPaymentPO.setCreateUser("unipay");
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());

        ownerPaymentPO.setPayType("银联");
        ownerPaymentPO.setDes("银联充值,outTradeNo:" + uniPayPO.getOutTradeNo());
        ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_UNPAYRECHARGE);

        try {
            paymentService.ownerRecharge(ownerPaymentPO);
        } catch (Exception e) {
            LOGGER.error("系统处理出现异常", e);
            LOGGER.error("异常信息" + ownerPaymentPO.toString());
        }
    }

    public static void clearCacheMap() {
        if (HAS_PAY_CACHE.size() > 500) {
            HAS_PAY_CACHE.clear();
        }
    }

}
