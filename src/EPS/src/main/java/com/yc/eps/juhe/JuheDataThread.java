package com.yc.eps.juhe;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.yc.chat.util.JsonUtil;
import com.yc.commons.HttpUtil;
import com.yc.commons.TimeUtil;
import com.yc.edsi.community.OwnerBoundPO;
import com.yc.edsi.juhe.JuheBaseResp;
import com.yc.edsi.juhe.JuheDataPO;
import com.yc.edsi.juhe.JuheQueryCond;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerPaymentPO;

/**
 * 
 * 充值状态同步线程
 * 
 * @author <a href="mailto:jiab@yichenghome.com">Jia bin</a>
 * @version 1.0
 * @since 2015年3月25日
 */
@Service
public class JuheDataThread {
    private final static Logger LOGGER = LoggerFactory.getLogger(JuheDataThread.class);
    @Value("#{eps['juhe.sleep.interval']}")
    private long SLEEP_TIME;

    private JuheDataSysThread juheDataSysThread;

    @Resource
    private JuheDataService juheDataService;
    @Resource
    private IPaymentService paymentService;
    private static final List<String> PROCESSING_STATUS = new ArrayList<String>();

    @PostConstruct
    private synchronized void init() {
        if (juheDataSysThread == null) {
            try {
                juheDataSysThread = new JuheDataSysThread();
                juheDataSysThread.start();
                LOGGER.info("启动聚合数据同步线程成功！！！");
            } catch (Exception e) {
                LOGGER.error("启动聚合数据同步线程出现异常！", e);
            }

        }
        PROCESSING_STATUS.add(JuheDataPO.STATUS_PROCESSING);
    }

    class JuheDataSysThread extends Thread {
        private boolean canRun = true;
        private long lastSleepTime = System.currentTimeMillis();

        public void run() {
            while (canRun) {
                long thisSleepTime = SLEEP_TIME - (System.currentTimeMillis() - lastSleepTime);
                if (thisSleepTime > 0) {
                    try {
                        Thread.sleep(thisSleepTime);
                        lastSleepTime = System.currentTimeMillis();
                    } catch (Exception e) {
                        LOGGER.error("聚合数据同步线程休眠时出现异常", e);
                    }
                }
                chickStatus();
            }
        }

        public void setCanRun(boolean canRun) {
            this.canRun = canRun;
        }

    }

    private void chickStatus() {
        try {
            JuheQueryCond juheQueryCond = new JuheQueryCond();
            juheQueryCond.setStatuses(PROCESSING_STATUS);

            List<JuheDataPO> lists = juheDataService.getPayList(juheQueryCond);
            if (lists != null) {
                for (JuheDataPO juheDataPO : lists) {
                    juheDataPO = this.getStatus(juheDataPO);
                    if (juheDataPO != null) {
                        juheDataPO.setFinishTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
                        if (juheDataPO.getStatus().equalsIgnoreCase(JuheDataPO.STATUS_SUCCESS)) {
                            // 充值成功，更新状态
                            juheDataService.updateJuhePayStatus(juheDataPO);
                        } else if (juheDataPO.getStatus().equalsIgnoreCase(JuheDataPO.STATUS_FAILURE)) {
                            // 充值失败，需要回滚
                            juheDataService.updateJuhePayStatus(juheDataPO);
                            OwnerPaymentPO ownerPaymentPO = paymentService.getOwnerPaymentPO(juheDataPO.getOwnerId(),
                                    juheDataPO.getCommunityId(), juheDataPO.getPaymentId());
                            JuheDataService.rolBack(ownerPaymentPO, paymentService);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("聚合数据同步线程数据处理出现异常", e);
        }

    }

    private static final String STATUS_URL_MOBILE = "http://op.juhe.cn/ofpay/mobile/query";
    private static final String STATUS_URL_TEL = "http://op.juhe.cn/ofpay/broadband/query";
    private static final String STATUS_URL_SDM = "http://op.juhe.cn/ofpay/public/ordersta";

    private JuheDataPO getStatus(JuheDataPO juheDataPO) {
        try {
            Map<String, String> map = new HashMap<String, String>();
            String url = null;
            if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_ELEC)
                    || juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_GAS)
                    || juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_WATER)) {
                url = STATUS_URL_SDM;
                map.put("key", JuheDataService.KEY_SDM);
            } else if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_MOBILE)) {
                url = STATUS_URL_MOBILE;
                map.put("key", JuheDataService.KEY_MOBILE);
            } else if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_TEL)) {
                url = STATUS_URL_TEL;
                map.put("key", JuheDataService.KEY_TEL);
            }
            map.put("orderid", juheDataPO.getOrderid());

            String resp = HttpUtil.post(url, map).getBody();
            Type type = new TypeToken<JuheBaseResp<Object>>() {
            }.getType();
            LOGGER.info("聚合数据查询充值状态返回的信息：{}", resp);
            JuheBaseResp<Object> juheBaseResp = JsonUtil.convert(resp, type);

            if (juheBaseResp.getError_code() == 0) {
                juheDataPO.setStatus(JuheDataPO.STATUS_SUCCESS);
            } else {

                if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_ELEC)
                        || juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_GAS)
                        || juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_WATER)) {
                    if (juheBaseResp.getError_code() == 209322 || juheBaseResp.getReason().indexOf("充值中") >= 0) {
                        juheDataPO.setStatus(JuheDataPO.STATUS_PROCESSING);
                    } else {
                        juheDataPO.setStatus(JuheDataPO.STATUS_FAILURE);
                    }
                } else if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_MOBILE)) {
                    if (juheBaseResp.getError_code() == 208511 || juheBaseResp.getReason().indexOf("充值中") >= 0) {
                        juheDataPO.setStatus(JuheDataPO.STATUS_PROCESSING);
                    } else {
                        juheDataPO.setStatus(JuheDataPO.STATUS_FAILURE);
                    }
                } else if (juheDataPO.getBoundType().equalsIgnoreCase(OwnerBoundPO.BOUND_TYPE_TEL)) {
                    if (juheBaseResp.getError_code() == 208513 || juheBaseResp.getError_code() == 211513
                            || juheBaseResp.getReason().indexOf("充值中") >= 0) {
                        juheDataPO.setStatus(JuheDataPO.STATUS_PROCESSING);
                    } else {
                        juheDataPO.setStatus(JuheDataPO.STATUS_FAILURE);
                    }
                }
            }

            return juheDataPO;
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        }
    }

    @PreDestroy
    private void destroy() {
        if (juheDataSysThread != null) {
            juheDataSysThread.setCanRun(false);
        }
    }
}
