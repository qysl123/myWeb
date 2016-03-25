package com.yc.eps.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yc.edsi.fee.FeeClientPayPO;
import com.yc.edsi.fee.IFeeService;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerPO;
import com.yc.edsi.payment.third.RechargePayPO;

public class PayTools {
    private final static Logger LOGGER = LoggerFactory.getLogger(PayTools.class);

    public static void payFee(Map<String, String> innerMap, IFeeService feeService, IOwnerService ownerService) {
        RechargePayPO rechargePayPO = PayCatchTools.getRPP(innerMap.get("clientPayId"));
        if (rechargePayPO == null) {
            LOGGER.error(PayTools.class.getName() + ":第三方支付扣款成功，但是未找到rechargePayPO");
            return;
        }

        Long communityId = Long.valueOf(innerMap.get("communityId"));
        Long ownerId = Long.valueOf(innerMap.get("ownerId"));

        StringBuilder logInfo = new StringBuilder();
        logInfo.append("ownerId:").append(ownerId);
        logInfo.append(",communityId:").append(communityId);
        logInfo.append(",clientPayId:").append(innerMap.get("clientPayId"));

        if (rechargePayPO.getIds() == null || rechargePayPO.getIds().size() <= 0) {
            LOGGER.error(PayTools.class.getName() + " :缴费失败，Ids不存在，内部参数：" + logInfo);
            return;
        } else {
            logInfo.append(",feeIds:");
            for (long feeId : rechargePayPO.getIds()) {
                logInfo.append(feeId).append(",");
            }
        }

        try {
            OwnerPO ownerPO = ownerService.getById(ownerId, communityId);
            List<FeeClientPayPO> feeClientPayPOs = new ArrayList<FeeClientPayPO>();
            for (long feeId : rechargePayPO.getIds()) {
                if (feeId > 0) {
                    FeeClientPayPO feeClientPayPO = new FeeClientPayPO();
                    feeClientPayPO.setFeeId(feeId);
                    feeClientPayPOs.add(feeClientPayPO);
                }
            }
            feeService.pay(ownerId, communityId, ownerPO.getOwnerName(), feeClientPayPOs);
            rechargePayPO.setStatus(RechargePayPO.STATUS_SUCCESS);
            rechargePayPO.setStatusMsg("缴费成功！");
            LOGGER.info(PayTools.class.getName() + ":缴费成功,内部信息：" + logInfo.toString());
        } catch (Exception e) {
            rechargePayPO.setStatusMsg("您的款项已收到，系统正在为您处理！");
            rechargePayPO.setStatus(RechargePayPO.STATUS_PENDING);
            LOGGER.error(PayTools.class.getName(), e);
            LOGGER.error(PayTools.class.getName() + ":缴费失败,内部信息：" + logInfo.toString());
        }
        rechargePayPO.setLastUpdateTime(System.currentTimeMillis());
        PayCatchTools.putRPP(rechargePayPO.getClientPayId(), rechargePayPO);
    }

    public static Map<String, String> parseReqInnerParam(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        String[] kvs = s.split("!");
        for (String ss : kvs) {
            String[] kv = ss.split("-");
            if ("ownerId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("ownerId", kv[1]);
            } else if ("communityId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("communityId", kv[1]);
            } else if ("payType".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("payType", kv[1]);
            } else if ("clientPayId".equalsIgnoreCase(kv[0]) && kv.length == 2) {
                map.put("clientPayId", kv[1]);
            }
        }

        return map;
    }

    public static String buildReqInnerParam(long ownerId, long communityId, int payType, String clientPayId) {
        StringBuffer sb = new StringBuffer();
        sb.append("ownerId-").append(ownerId);
        sb.append("!communityId-").append(communityId);
        sb.append("!payType-").append(payType);
        sb.append("!clientPayId-").append(clientPayId);
        return sb.toString();
    }

    /**
     * 获取请求参数中所有的信息
     * 
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
        }
        return ip;
    }

    public static String longListToString(List<Long> param, int maxLength) {
        StringBuilder stringBuilder = new StringBuilder();
        if (param != null && param.size() > 0) {
            for (Long id : param) {
                stringBuilder.append(id).append(",");
            }
        }
        String result = stringBuilder.toString();
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }

        return result;
    }

    public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return message;
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        return new byte[] {};
    }
}
