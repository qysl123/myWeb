package com.yc.eps.tickets;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yc.commons.HttpUtil;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.tickets.TicketsOrderPO;
import com.yc.eps.juhe.JuheDataService;

/**
 * 
 * 充值状态同步线程
 * 
 * @author <a href="mailto:jiab@yichenghome.com">Jia bin</a>
 * @version 1.0
 * @since 2015年3月25日
 */
@Service
public class TicketsThread {
    private final static Logger LOGGER = LoggerFactory.getLogger(TicketsThread.class);
    @Value("#{eps['juhe.tickets.sleep.interval']}")
    private long SLEEP_TIME;

//    @Value("#{eps['isTest']}")
    private boolean isTest = false;

    private TicketsSysThread ticketsSysThread;

    @Resource
    private ITicketsDao ticketsDao;
    @Resource
    private TicketsService ticketsService;
    @Resource
    private IPaymentService paymentService;
    private static final List<String> PROCESSING_STATUS = new ArrayList<String>();

    @PostConstruct
    private synchronized void init() {
        PROCESSING_STATUS.add(TicketsService.ORDER_STATUS_0);
        PROCESSING_STATUS.add(TicketsService.ORDER_STATUS_2);
        PROCESSING_STATUS.add(TicketsService.ORDER_STATUS_3);
        PROCESSING_STATUS.add(TicketsService.ORDER_STATUS_6);
        if (ticketsSysThread == null) {
            try {
                ticketsSysThread = new TicketsSysThread();
                ticketsSysThread.start();
                LOGGER.info("启动聚合火车票数据同步线程成功！！！");
            } catch (Exception e) {
                LOGGER.error("启动聚合火车票数据同步线程出现异常！", e);
            }

        }
    }

    class TicketsSysThread extends Thread {
        private boolean canRun = true;
        private long lastSleepTime = System.currentTimeMillis();

        public void run() {
            while (canRun) {
                long thisSleepTime = SLEEP_TIME - (System.currentTimeMillis() - lastSleepTime);
                if (isTest) {
                    thisSleepTime = 60000;
                }
                if (thisSleepTime > 0) {
                    try {
                        Thread.sleep(thisSleepTime);
                        lastSleepTime = System.currentTimeMillis();
                    } catch (Exception e) {
                        LOGGER.error("聚合数据同步线程休眠时出现异常", e);
                    }
                }
                try {
                    doProcess();
                } catch (Exception e) {
                    LOGGER.error("聚合数据火车票同步线程出现异常", e);
                }
            }
        }

        public void setCanRun(boolean canRun) {
            this.canRun = canRun;
        }

    }

    private void doProcess() throws Exception {
        List<TicketsOrderPO> list = ticketsDao.getListByOrderStatus(PROCESSING_STATUS);
        if (list != null) {
            for (TicketsOrderPO tp : list) {
                try {
                    sysTrainInfo(tp.getOrderId(), tp.getCommunityId(), tp.getOwnerId());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    public void sysTrainInfo(String orderId, Long communityId, Long ownerId) throws EdsiException {
        String url = "http://op.juhe.cn/trainTickets/orderStatus?orderid=" + orderId + "&key="
                + JuheDataService.KEY_TRAIN_TICKETS;
        try {
            String body = HttpUtil.get(url).getBody();
            JSONObject obj = JSONObject.parseObject(body);
            parseOrderStatus(obj, orderId, communityId, ownerId);
            return;
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("查询订单出错，订单id：" + orderId, e);
            throw new EdsiException("查询订单信息出错，请稍后再试！");
        }
    }

    public synchronized void parseOrderStatus(JSONObject obj, String orderId, Long communityId, Long ownerId) {
        int error_code = obj.getIntValue("error_code");
        if (error_code != 0) {
            LOGGER.error("从聚合查询订单状态出错，订单id：{}，失败原因：{}", orderId, obj.getString("reason"));
            return;
        }

        obj = obj.getJSONObject("result");
        TicketsOrderPO ticketsOrderPO = ticketsDao.getTickets(orderId, communityId, ownerId);
        // 检查是否需要更新数据库内状态
        if (ticketsOrderPO.getOrderStatus().equals(obj.getString("status"))) {
            return;
        }

        // 更新状态不一致的订单
        String oldStatus = ticketsOrderPO.getOrderStatus();
        String oldBody = ticketsOrderPO.getBody();
        ticketsOrderPO.setOrderStatus(obj.getString("status"));
        ticketsOrderPO.setBody(JSONObject.toJSONString(obj));
        ticketsDao.update(ticketsOrderPO);
        // 如有退款，做退款处理
        if (oldStatus.equals(TicketsService.ORDER_STATUS_3)
                && TicketsService.ORDER_STATUS_5.equals(ticketsOrderPO.getOrderStatus())) {
            // 出票失败、退款
            OwnerPaymentPO ownerPaymentPO = paymentService.getOwnerPaymentPO(ticketsOrderPO.getOwnerId(),
                    ticketsOrderPO.getCommunityId(), ticketsOrderPO.getPaymentId());
            JuheDataService.rolBack(ownerPaymentPO, paymentService);
            return;
        }

        if (oldStatus.equals(TicketsService.ORDER_STATUS_6)
                && TicketsService.ORDER_STATUS_7.equals(ticketsOrderPO.getOrderStatus())) {
            // 有人退票成功
            JSONObject oldObj = JSONObject.parseObject(oldBody);
            JSONArray passengers = oldObj.getJSONArray("passengers");
            int passengerCount = passengers.size();
            for (int i = 0; i < passengerCount; i++) {
                JSONObject passenger = passengers.getJSONObject(i);
                if (passenger.containsKey("returntickets")) {
                    // 有退票
                    String ticketNo = passenger.getString("ticket_no");
                    JSONObject returntickets = passenger.getJSONObject("returntickets");
                    if (returntickets.getBooleanValue("returnsuccess")
                            || "1".equals(returntickets.getString("returntype"))) {
                        // 退票成功，并且是线上退票，需要给用户退款
                        TicketsReturnPO returnPO = ticketsDao.getTicketsR(orderId, ticketNo);
                        if ("0".equals(returnPO.getFinish())) {
                            double price = returntickets.getDoubleValue("returnmoney");
                            // 没处理过退款
                            if (price > 0) {
                                OwnerPaymentPO ownerPaymentPO = paymentService.getOwnerPaymentPO(
                                        ticketsOrderPO.getOwnerId(), ticketsOrderPO.getCommunityId(),
                                        ticketsOrderPO.getPaymentId());
                                ownerPaymentPO.setAmount(Tools.subtract(0, price));
                                ownerPaymentPO = JuheDataService.rolBack(ownerPaymentPO, paymentService);
                                if (ownerPaymentPO != null) {
                                    returnPO.setPaymentId(ownerPaymentPO.getPaymentId());
                                }
                            }
                            returnPO.setFinish("1");
                            returnPO.setPrice(price);
                            returnPO.setFinishTime(TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS());
                            ticketsDao.updateTicketR(returnPO);
                        }
                    }
                }
            }
        }

    }

    @PreDestroy
    private void destroy() {
        if (ticketsSysThread != null) {
            ticketsSysThread.setCanRun(false);
        }
    }
}
