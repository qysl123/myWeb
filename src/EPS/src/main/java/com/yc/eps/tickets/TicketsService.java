package com.yc.eps.tickets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yc.commons.HttpUtil;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.contactpeople.ContactPeoplePO;
import com.yc.edsi.contactpeople.IContactPeopleService;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerAccount;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.system.DictionaryPO;
import com.yc.edsi.system.ISystemService;
import com.yc.edsi.tickets.ITicketsService;
import com.yc.edsi.tickets.TicketsOrderPO;
import com.yc.eps.juhe.JuheDataService;

@Service
public class TicketsService implements ITicketsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TicketsService.class);

    // 0：刚提交，待处理；1：失败／失效／取消的订单；2：占座成功待支付（此时可取消订单，超时不支付将失效）；3：支付成功待出票；4：出票成功；
    // 5：出票失败；6：正在处理线上退票请求；7：有乘客退票（改签）成功（status保存的是最后一次操作该订单后的状态，先有乘客退票失败，
    // 然后有乘客退票成功，那么status为7）；8：有乘客退票失败

    public final static String ORDER_STATUS_0 = "0";
    public final static String ORDER_STATUS_1 = "1";
    public final static String ORDER_STATUS_2 = "2";
    public final static String ORDER_STATUS_3 = "3";
    public final static String ORDER_STATUS_4 = "4";
    public final static String ORDER_STATUS_5 = "5";
    public final static String ORDER_STATUS_6 = "6";
    public final static String ORDER_STATUS_7 = "7";
    public final static String ORDER_STATUS_8 = "8";

    @Resource
    private IPaymentService paymentService;
    @Resource
    private IContactPeopleService contactPeopleService;
    @Resource
    private ISystemService systemService;
    @Resource
    private ITicketsDao ticketsDao;
    // 开车前多少分钟不能退票
    @Value("#{eps['tickets.returnBeforeMinutes']}")
    private int returnBeforeMinutes = 1;
//    @Value("#{eps['isTest']}")
    private boolean isTest = false;

    @Override
    public String queryTrain(String fromStation, String toStation, String date) throws EdsiException {
        String fromCode = this.getStationCode(fromStation);
        String toCode = this.getStationCode(toStation);

        String url = "http://op.juhe.cn/trainTickets/ticketsAvailable?key=" + JuheDataService.KEY_TRAIN_TICKETS
                + "&train_date=" + date + "&from_station=" + fromCode + "&to_station=" + toCode;
        try {
            if (isTest) {
                return "{\"reason\":\"成功的返回\",\"result\":{\"list\":[{\"rwx_price\":0,\"end_station_name\":\"公主岭\",\"swz_price\":0,\"swz_num\":\"--\",\"to_station_name\":\"长春南\",\"ydz_num\":\"--\",\"yz_num\":\"1000\",\"rw_num\":\"--\",\"arrive_days\":\"0\",\"rz_num\":\"--\",\"access_byidcard\":\"0\",\"yz_price\":1,\"ywz_price\":0,\"sale_date_time\":\"0800\",\"from_station_code\":\"CCT\",\"rz_price\":0,\"gjrw_num\":\"--\",\"to_station_code\":\"CET\",\"ydz_price\":0,\"wz_price\":1,\"tdz_price\":0,\"run_time\":\"00:11\",\"yw_num\":\"--\",\"edz_price\":0,\"qtxb_price\":0,\"can_buy_now\":\"Y\",\"yw_price\":0,\"rw_price\":0,\"train_type\":\"6\",\"note\":\"\",\"train_no\":\"110000633604\",\"train_code\":\"6336\",\"from_station_name\":\"长春\",\"run_time_minute\":\"11\",\"ywx_price\":0,\"arrive_time\":\"18:19\",\"start_station_name\":\"长春\",\"start_time\":\"18:08\",\"wz_num\":\"1000\",\"edz_num\":\"--\",\"qtxb_num\":\"--\",\"train_start_date\":\"20151224\",\"gjrw_price\":0,\"tdz_num\":\"--\"}]},\"error_code\":0}";
            } else {
                return HttpUtil.get(url).getBody();
            }
        } catch (Exception e) {
            throw new EdsiException("查询车站信息出现异常！");
        }
    }

    private String getStationCode(String stationName) throws EdsiException {
        String code = null;
        try {
            code = systemService.getDictionaryValue(ISystemService.TRAIN_TICKETS, stationName);
            if (StringUtils.isBlank(code)) {
                String url = "http://op.juhe.cn/trainTickets/cityCode?stationName=" + stationName + "&key="
                        + JuheDataService.KEY_TRAIN_TICKETS;
                String data = HttpUtil.get(url).getBody();
                JSONObject obj = JSONObject.parseObject(data);
                obj = JSONObject.parseObject(obj.getString("result"));
                if (obj.containsKey("code")) {
                    throw new EdsiException("车站不存在，请重新查询！");
                }
                code = obj.getString("code");
                if (StringUtils.isBlank(code)) {
                    throw new EdsiException("车站不存在，请重新查询！");
                }
                DictionaryPO dictionaryPO = new DictionaryPO();
                dictionaryPO.setKey(stationName);
                dictionaryPO.setValue(code);
                dictionaryPO.setType(ISystemService.TRAIN_TICKETS);
                systemService.insertDictionaryItem(dictionaryPO);
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(this.getClass().getName(), e);
            throw new EdsiException("查询车站信息出现异常！");
        }
        return code;
    }

    public String submitTrain(String jsonStr, OwnerUserPO user) throws EdsiException {
        Long toId = systemService.getId();

        JSONObject obj = JSONObject.parseObject(jsonStr);

        TicketsOrderPO ticketsOrderPO = new TicketsOrderPO();
        ticketsOrderPO.setToId(toId);
        ticketsOrderPO.setBody(jsonStr);
        ticketsOrderPO.setCommunityId(user.getCommunityId());
        ticketsOrderPO.setOwnerId(user.getOwnerId());
        ticketsOrderPO.setFromStationCode(obj.getString("from_station_code"));
        ticketsOrderPO.setFromStationName(obj.getString("from_station_name"));
        ticketsOrderPO.setToStationName(obj.getString("to_station_name"));
        ticketsOrderPO.setToStationCode(obj.getString("to_station_code"));
        ticketsOrderPO.setTrainDate(obj.getString("train_date"));
        ticketsOrderPO.setCheci(obj.getString("checi"));
        ticketsOrderPO.setCreateTime(TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS());
        ticketsOrderPO.setOrderStatus(ORDER_STATUS_0);

        // 以下为自定义的额外字段，存入数据库后，要删除再提交聚合
        ticketsOrderPO.setStartTime(obj.getString("start_time"));
        ticketsOrderPO.setArriveTime(obj.getString("arrive_time"));
        ticketsOrderPO.setArriveDays(obj.getString("arrive_days"));

        obj.remove("start_time");
        obj.remove("arrive_time");
        obj.remove("arrive_days");

        double price = 0d;
        int ticketsCount = 0;

        List<ContactPeoplePO> contactPeoplePOs = new ArrayList<ContactPeoplePO>();

        JSONArray ja = obj.getJSONArray("passengers");
        int jaSize = ja.size();
        // 计算总价格，同时记录常用联系人
        for (; ticketsCount < jaSize; ticketsCount++) {
            JSONObject o = ja.getJSONObject(ticketsCount);
            price = Tools.add(price, o.getDoubleValue("price"));

            ContactPeoplePO cpp = new ContactPeoplePO();
            cpp.setCardType(o.getString("passporttypeseid"));
            cpp.setCardNo(o.getString("passportseno"));
            cpp.setName(o.getString("passengersename"));
            cpp.setPiaoType(o.getString("piaotype"));
            contactPeoplePOs.add(cpp);
        }
        ticketsOrderPO.setPayMoney(price);

        Map<String, String> param = new HashMap<String, String>();
        param.put("key", JuheDataService.KEY_TRAIN_TICKETS);
        param.put("user_orderid", String.valueOf(toId));
        Set<String> set = obj.keySet();
        for (String key : set) {
            param.put(key, obj.getString(key));
        }

        String url = "http://op.juhe.cn/trainTickets/submit";
        try {
            if (isTest) {
                ticketsOrderPO.setOrderId(String.valueOf(systemService.getId()));
                ticketsDao.insert(ticketsOrderPO);
                return ticketsOrderPO.getOrderId();
            } else {
                String body = HttpUtil.post(url, param, null).getBody();
                obj = JSONObject.parseObject(body);
                int error_code = obj.getIntValue("error_code");
                if (error_code == 0) {
                    obj = JSONObject.parseObject(obj.getString("result"));
                    ticketsOrderPO.setOrderId(obj.getString("orderid"));
                    ticketsDao.insert(ticketsOrderPO);
                    return obj.getString("orderid");
                } else {
                    LOGGER.error("订票出错{}", body);
                    throw new EdsiException(obj.getString("reason"));
                }
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(this.getClass().getName(), e);
            throw new EdsiException("提交订单出现异常！");
        } finally {
            if (contactPeoplePOs.size() > 0) {
                try {
                    contactPeopleService.insert(contactPeoplePOs, user.getCommunityId(), user.getPropertyId(),
                            user.getOwnerId());
                } catch (Exception e) {
                    // 插入联系人失败，不影响主流程，所以什么也不做
                    LOGGER.error("插入联系人出错{}", e);
                }
            }
        }
    }

    @Override
    public List<TicketsOrderPO> getTrainList(String status, Long communityId, Long ownerId) throws EdsiException {
        return ticketsDao.getList(status, communityId, ownerId);
    }

    @Override
    public Object getTrainInfo(String orderId, Long communityId, Long ownerId) throws EdsiException {
        try {
            TicketsOrderPO ticketsOrderPO = ticketsDao.getTickets(orderId, communityId, ownerId);
            JSONObject obj = JSONObject.parseObject(ticketsOrderPO.getBody());
            if (obj.containsKey("status")) {
                obj.put("status", ticketsOrderPO.getOrderStatus());
            }
            obj.put("start_time", ticketsOrderPO.getStartTime());
            obj.put("arrive_time", ticketsOrderPO.getArriveTime());
            obj.put("arrive_days", ticketsOrderPO.getArriveDays());
            obj.put("server_time", TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS());
            obj.put("return_minutes", returnBeforeMinutes);
            return obj;
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("查询订单出错，订单id：" + orderId, e);
            throw new EdsiException("查询订单信息出错，请稍后再试！");
        }
    }

    @Override
    public void payTrain(String orderId, Long communityId, Long ownerId) throws EdsiException {
        double price = 0d;
        try {
            TicketsOrderPO ticketsOrderPO = ticketsDao.getTickets(orderId, communityId, ownerId);
            if (ticketsOrderPO == null || ticketsOrderPO.getToId() == null || ticketsOrderPO.getToId() <= 0) {
                throw new EdsiException("未查询到订单信息！");
            }
            if (!ORDER_STATUS_2.equals(ticketsOrderPO.getOrderStatus())) {
                throw new EdsiException("未占座成功，无法支付订单，占座成功后再支付！");
            }
            price = ticketsOrderPO.getPayMoney();
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("TicketsService", e);
            throw new EdsiException("个人账户支付失败，请稍后再试");
        }

        // 先扣款
        OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
        ownerPaymentPO.setCommunityId(communityId);
        ownerPaymentPO.setOwnerId(ownerId);
        ownerPaymentPO.setAmount(Tools.subtract(0, price));
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
        ownerPaymentPO.setDes("购买火车票,订单ID：" + orderId);
        ownerPaymentPO.setPayType("火车票");
        ownerPaymentPO.setDisplayDes("购买火车票");
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_CONSUME);
        ownerPaymentPO.setSysUserOptFlag(OwnerPaymentPO.SYS_OPT_TYPE_SHENGHUOFEE);

        try {
            OwnerAccount account = paymentService.getOwnerAccount(ownerId, communityId);
            if (account != null && account.getMoneyAmt() >= price) {
                ownerPaymentPO = paymentService.ownerConsume(ownerPaymentPO);
            } else {
                LOGGER.error("TicketsService，现金余额不足或账号信息不存在");
                throw new EdsiException("账户余额不足，请稍后再试");
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("TicketsService", e);
            throw new EdsiException("个人账户支付失败，请稍后再试");
        }

        // 扣款通过，支付火车票
        String url = "http://op.juhe.cn/trainTickets/pay?orderid=" + orderId + "&key="
                + JuheDataService.KEY_TRAIN_TICKETS;
        try {
            String body = HttpUtil.get(url).getBody();
            JSONObject obj = JSONObject.parseObject(body);
            int error_code = obj.getIntValue("error_code");
            if (error_code != 0) {
                // 支付失败
                LOGGER.error("支付火车票订单出现异常，交互报文：{}", body);
                JuheDataService.rolBack(ownerPaymentPO, paymentService);
                throw new EdsiException(obj.getString("result"));
            }

            TicketsOrderPO ticketsOrderPO = new TicketsOrderPO();
            ticketsOrderPO.setCommunityId(communityId);
            ticketsOrderPO.setOwnerId(ownerId);
            ticketsOrderPO.setOrderId(orderId);
            ticketsOrderPO.setOrderStatus(ORDER_STATUS_3);
            ticketsOrderPO.setPaymentId(ownerPaymentPO.getPaymentId());
            ticketsDao.update(ticketsOrderPO);
        } catch (Exception e) {
            LOGGER.error("TicketsService", e);
            throw new EdsiException("提交订单出现异常！");
        }

    }

    @Override
    public void delete(String orderId, Long communityId, Long ownerId) throws EdsiException {
        TicketsOrderPO ticketsOrderPO = ticketsDao.getTickets(orderId, communityId, ownerId);
        if (ORDER_STATUS_2.equals(ticketsOrderPO.getOrderStatus())) {
            // 只有占座成功才需要取消订单
            String url = "http://op.juhe.cn/trainTickets/cancel?orderid=" + orderId + "&key="
                    + JuheDataService.KEY_TRAIN_TICKETS;
            try {
                String body = HttpUtil.get(url).getBody();
                JSONObject obj = JSONObject.parseObject(body);
                int error_code = obj.getIntValue("error_code");
                if (error_code != 0) {
                    LOGGER.error("取消火车票订单出现异常，交互报文：{}", body);
                    throw new EdsiException("取消订单出现异常！");
                }
            } catch (EdsiException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.error("TicketsService", e);
                throw new EdsiException("取消订单出现异常！");
            }
        }

        ticketsOrderPO = new TicketsOrderPO();
        ticketsOrderPO.setCommunityId(communityId);
        ticketsOrderPO.setOrderId(orderId);
        ticketsOrderPO.setOwnerId(ownerId);
        ticketsOrderPO.setStatus(Constant.STATUS_DELETE);
        ticketsDao.update(ticketsOrderPO);
    }

    @Override
    public void returnTrain(JSONObject object, String orderId, Long communityId, Long ownerId) throws EdsiException {
        String url = "http://op.juhe.cn/trainTickets/refund?orderid=" + orderId + "&key="
                + JuheDataService.KEY_TRAIN_TICKETS;

        try {
            Map<String, String> param = new HashMap<String, String>();
            param.put("tickets", "[" + JSONObject.toJSONString(object) + "]");
            String body = HttpUtil.post(url, param, null).getBody();
            JSONObject obj = JSONObject.parseObject(body);
            int error_code = obj.getIntValue("error_code");
            if (error_code == 0) {
                String ticketNo = object.getString("ticket_no");
                obj = obj.getJSONObject("result");
                // 更新数据库内状态
                TicketsOrderPO ticketsOrderPO = new TicketsOrderPO();
                ticketsOrderPO.setCommunityId(communityId);
                ticketsOrderPO.setOwnerId(ownerId);
                ticketsOrderPO.setOrderId(orderId);

                ticketsOrderPO.setOrderStatus(TicketsService.ORDER_STATUS_6);
                // 如果使用报文返回值，会有无法把钱返回给业主的情况
                // ticketsOrderPO.setOrderStatus(obj.getString("status"));

                // 要更新原有报文，否则在同步线程同步前，用户无法看到哪张票在退
                JSONObject returnObj = JSONObject.parseObject("{}");
                returnObj.put("returnsuccess", "");

                TicketsOrderPO oldOrderPO = ticketsDao.getTickets(orderId, communityId, ownerId);
                JSONObject oldObj = JSONObject.parseObject(oldOrderPO.getBody());
                JSONArray passengers = oldObj.getJSONArray("passengers");
                int passengerCount = passengers.size();
                for (int i = 0; i < passengerCount; i++) {
                    JSONObject passenger = passengers.getJSONObject(i);
                    if (ticketNo.equals(passenger.getString("ticket_no"))) {
                        passenger.put("returntickets", returnObj);
                        break;
                    }
                }
                ticketsOrderPO.setBody(JSONObject.toJSONString(oldObj));
                ticketsDao.update(ticketsOrderPO);

                // 插入退票表
                TicketsReturnPO ticketsReturnPO = new TicketsReturnPO();
                ticketsReturnPO.setCommunityId(communityId);
                ticketsReturnPO.setOwnerId(ownerId);
                ticketsReturnPO.setOrderId(orderId);
                ticketsReturnPO.setSubmitTime(TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS());
                ticketsReturnPO.setTicketNo(ticketNo);
                ticketsDao.insertTicketR(ticketsReturnPO);
            } else {
                throw new EdsiException(obj.getString("reason"));
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("退票出错", e);
            throw new EdsiException("查询订单信息出错，请稍后再试！");
        }
    }

    public static void main(String[] args) {
        JSONObject returnObj = JSONObject.parseObject("{}");
        returnObj.put("returnsuccess", "");
        System.out.println(JSONObject.toJSONString(returnObj));

        String bod = "{\"checi\":\"6336\",\"deal_time\":\"2015-12-10 13:14:22\",\"finished_time\":\"2015-12-10 13:17:29\",\"from_station_code\":\"CCT\",\"from_station_name\":\"长春\",\"msg\":\"出票成功\",\"orderamount\":\"1.00\",\"orderid\":\"1449724398176\",\"ordernumber\":\"E130525298\",\"passengers\":[{\"cxin\":\"05车厢,036座\",\"passengerid\":\"1000\",\"passengersename\":\"文小伟\",\"passportseno\":\"510922198508241619\",\"passporttypeseid\":\"1\",\"passporttypeseidname\":\"二代身份证\",\"piaotype\":\"1\",\"piaotypename\":\"成人票\",\"price\":\"1.0\",\"reason\":0,\"ticket_no\":\"E1305252981050036\",\"zwcode\":\"1\",\"zwname\":\"硬座\"}],\"pay_time\":\"2015-12-10 13:17:18\",\"status\":\"4\",\"submit_time\":\"2015-12-10 13:13:18\",\"to_station_code\":\"CET\",\"to_station_name\":\"长春南\",\"train_date\":\"2015-12-10\",\"user_orderid\":\"1657363\"}";
        JSONObject oldObj = JSONObject.parseObject(bod);
        JSONArray passengers = oldObj.getJSONArray("passengers");
        int passengerCount = passengers.size();
        for (int i = 0; i < passengerCount; i++) {
            JSONObject passenger = passengers.getJSONObject(i);
            if ("E1305252981050036".equals(passenger.getString("ticket_no"))) {
                passenger.put("returntickets", returnObj);
                break;
            }
        }
        System.out.println(JSONObject.toJSONString(oldObj));
    }
}
