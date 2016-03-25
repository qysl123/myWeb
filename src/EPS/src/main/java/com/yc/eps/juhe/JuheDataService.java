package com.yc.eps.juhe;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.yc.chat.util.JsonUtil;
import com.yc.commons.HttpUtil;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.CommunityPO;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.community.IOwnerBoundService;
import com.yc.edsi.community.OwnerBoundPO;
import com.yc.edsi.juhe.BroadbandOnlineorderResp;
import com.yc.edsi.juhe.CarWeiZhangResp;
import com.yc.edsi.juhe.FeeSDMDebtsPO;
import com.yc.edsi.juhe.FeeSDMPayPO;
import com.yc.edsi.juhe.IJuheDataService;
import com.yc.edsi.juhe.JuheBaseResp;
import com.yc.edsi.juhe.JuheDataPO;
import com.yc.edsi.juhe.JuheQueryCond;
import com.yc.edsi.juhe.JuheSettlementPO;
import com.yc.edsi.juhe.MobileOnlineorderResp;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerAccount;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.system.ISystemService;
import com.yc.eps.unipay.UniPayCore;

@Service
public class JuheDataService implements IJuheDataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(JuheDataService.class);
    public final static String KEY_MOBILE = "773d0d9ba952849baf55b6ecf027b4ca";
    public final static String KEY_TEL = "2d928363c797870edcfaddbd2fe04da0";
    public final static String KEY_CAR = "8d4a8b066b94683d49fe0f84a971eecb";
    public final static String KEY_SDM = "55223f4813936d6091028c6971e401c0";
    public final static String KEY_CALLSXHH = "aae6eadb57562eb6c2f68f920e494da6";
    public final static String KEY_TRAIN_TICKETS = "9d5dcc447ffe26d21e77aa9fb49b65db";

    @Value("#{eps['juhe.url.pre']}")
    private String juhePreUrl;
    @Value("#{eps['juhe.openId']}")
    private String juheOpenId;

    @Value("#{eps['isTest']}")
    private boolean isTest;

    @Resource
    private IPaymentService paymentService;

    @Resource
    private ISystemService systemService;
    @Resource
    private IOwnerBoundService ownerBoundService;
    @Resource
    private ICommunityService communityService;
    @Resource
    private IJuheDataDao juheDataDao;

    @Override
    public MobileOnlineorderResp payMobile(Long communityId, Long ownerId, String phoneno, int cardnum, double inprice,
            long boundId) throws EdsiException {
        // 先绑定数据
        OwnerBoundPO ownerBoundPO = new OwnerBoundPO();
        ownerBoundPO.setBoundId(systemService.getId());
        ownerBoundPO.setCommunityId(communityId);
        ownerBoundPO.setOwnerId(ownerId);
        ownerBoundPO.setBoundType(OwnerBoundPO.BOUND_TYPE_MOBILE);
        ownerBoundPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ownerBoundPO.setBoundName("手机话费充值");
        ownerBoundPO.setDisplayKey(phoneno);
        ownerBoundPO.setStatus(Constant.STATUS_NORMAL);
        ownerBoundPO = ownerBoundService.addBound(ownerBoundPO);

        String orderId = UniPayCore.getUniPaiId(systemService.getId(), 15);

        // 先扣款，再充值
        OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
        ownerPaymentPO.setCommunityId(communityId);
        ownerPaymentPO.setOwnerId(ownerId);
        ownerPaymentPO.setAmount(Tools.subtract(0, inprice));
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
        ownerPaymentPO.setDes(phoneno + "手机话费充值" + cardnum + ",订单ID：" + orderId);
        ownerPaymentPO.setPayType(OwnerBoundPO.BOUND_TYPE_MOBILE);
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_CONSUME);
        ownerPaymentPO.setSysUserOptFlag(OwnerPaymentPO.SYS_OPT_TYPE_SHENGHUOFEE);

        Map<String, String> map = new HashMap<String, String>();
        map.put("phoneno", phoneno);
        map.put("cardnum", String.valueOf(cardnum));
        map.put("orderid", orderId);
        map.put("key", KEY_MOBILE);
        map.put("sign", Tools.getMd5(juheOpenId + KEY_MOBILE + phoneno + cardnum + orderId));

        try {
            OwnerAccount account = paymentService.getOwnerAccount(ownerId, communityId);
            if (account != null && account.getMoneyAmt() >= inprice) {
                ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_JUHE + ",手机话费" + phoneno);
                ownerPaymentPO = paymentService.ownerConsume(ownerPaymentPO);
            } else {
                LOGGER.error("MobileFeeService，现金余额不足或账号信息不存在");
                throw new EdsiException("账户余额不足，请稍后再试");
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("MobileFeeService", e);
            throw new EdsiException("个人账户支付失败，请稍后再试");
        }
        JuheBaseResp<MobileOnlineorderResp> juheBaseResp = null;
        try {
            String resp = null;
            if (isTest) {
                resp = "{\"reason\":\"提交充值成功\",\"result\":{\"cardid\":\"141806\",\"cardnum\":\"1\",\"ordercash\":10.02,\"cardname\":\"四川移动话费10元直充\",\"sporder_id\":\"20150325112047349\",\"game_userid\":\"13881940375\",\"game_state\":\"0\",\"uorderid\":\"000000000304154\"},\"error_code\":0}";
            } else {
                resp = HttpUtil.post(juhePreUrl + "mobile/onlineorder", map).getBody();
            }

            Type type = new TypeToken<JuheBaseResp<MobileOnlineorderResp>>() {
            }.getType();
            LOGGER.info("手机充值调用第三方接口返回的信息：{}", resp);
            juheBaseResp = JsonUtil.convert(resp, type);
        } catch (Exception e) {
            LOGGER.error("调用第三方接口失败，需要回滚", e);
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        }

        if (juheBaseResp.getError_code() != 0) {
            // 充值失败，回滚
            LOGGER.error("调用第三方接口失败，需要回滚,error_code:{},reason:{}", juheBaseResp.getError_code(),
                    juheBaseResp.getReason());
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        } else {
            MobileOnlineorderResp moResp = juheBaseResp.getResult();
            LOGGER.info("调用第三方接口成功，cardid{},game_userid:{},game_state{},uorderid:{},sporder_id{},ordercash()",
                    moResp.getCardid(), moResp.getGame_userid(), moResp.getGame_state(), moResp.getUorderid(),
                    moResp.getSporder_id(), moResp.getOrdercash());
            this.addJuhePay(ownerBoundPO, map.get("orderid"), String.valueOf(cardnum), moResp.getOrdercash(),
                    ownerPaymentPO.getAmount(), JuheDataPO.STATUS_PROCESSING, ownerPaymentPO.getPaymentId());
            return juheBaseResp.getResult();
        }
    }

    @Override
    public BroadbandOnlineorderResp payBroadband(Long communityId, Long ownerId, String teltype, String phoneno,
            String pervalue, String chargetype, long boundId) throws EdsiException {
        // 先绑定数据
        OwnerBoundPO ownerBoundPO = new OwnerBoundPO();
        ownerBoundPO.setBoundId(systemService.getId());
        ownerBoundPO.setCommunityId(communityId);
        ownerBoundPO.setOwnerId(ownerId);
        ownerBoundPO.setBoundType(OwnerBoundPO.BOUND_TYPE_TEL);
        ownerBoundPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ownerBoundPO.setBoundName("固话、宽带充值");
        ownerBoundPO.setDisplayKey(phoneno);
        ownerBoundPO.setStatus(Constant.STATUS_NORMAL);
        ownerBoundPO.setValue1(teltype);
        ownerBoundPO.setValue2(chargetype);
        ownerBoundPO = ownerBoundService.addBound(ownerBoundPO);

        Map<String, String> map = new HashMap<String, String>();
        map.put("teltype", teltype);
        map.put("phoneno", phoneno);
        map.put("pervalue", pervalue);
        map.put("chargetype", chargetype);
        map.put("orderid", UniPayCore.getUniPaiId(systemService.getId(), 15));
        map.put("key", KEY_TEL);
        map.put("sign", Tools.getMd5(juheOpenId + KEY_TEL + phoneno + pervalue + map.get("orderid")));

        // 先扣款，再充值
        OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
        ownerPaymentPO.setCommunityId(communityId);
        ownerPaymentPO.setOwnerId(ownerId);
        ownerPaymentPO.setAmount(Tools.subtract(0, Integer.parseInt(pervalue)));
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
        ownerPaymentPO.setDes(phoneno + "固话、宽带充值" + pervalue + ",订单ID：" + map.get("orderid"));
        ownerPaymentPO.setPayType(OwnerBoundPO.BOUND_TYPE_TEL);
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_CONSUME);
        ownerPaymentPO.setSysUserOptFlag(OwnerPaymentPO.SYS_OPT_TYPE_SHENGHUOFEE);

        try {
            OwnerAccount account = paymentService.getOwnerAccount(ownerId, communityId);
            if (account != null && account.getMoneyAmt() >= Integer.parseInt(pervalue)) {
                ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_JUHE + "," + phoneno);
                ownerPaymentPO = paymentService.ownerConsume(ownerPaymentPO);
            } else {
                LOGGER.error("MobileFeeService，现金余额不足或账号信息不存在");
                throw new EdsiException("个人账户支付失败，请稍后再试");
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("MobileFeeService", e);
            throw new EdsiException("个人账户支付失败，请稍后再试");
        }
        JuheBaseResp<BroadbandOnlineorderResp> juheBaseResp = null;
        try {
            String resp = null;
            if (isTest) {
                resp = "{\"reason\":\"提交充值成功\",\"result\":{\"cardid\":\"191806\",\"cardnum\":\"1\",\"ordercash\":\"10.0\",\"cardname\":\"四川电信话费10元直充\",\"sporder_id\":\"B20150325143836728\",\"game_userid\":\"028-87784991\",\"game_state\":\"0\",\"uorderid\":\"000000000304179\"},\"error_code\":0}";
            } else {
                resp = HttpUtil.post(juhePreUrl + "broadband/onlineorder", map).getBody();
            }

            Type type = new TypeToken<JuheBaseResp<BroadbandOnlineorderResp>>() {
            }.getType();
            LOGGER.info("固话充值调用第三方接口返回的信息：{}", resp);
            juheBaseResp = JsonUtil.convert(resp, type);
        } catch (Exception e) {
            LOGGER.error("调用第三方接口失败，需要回滚", e);
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        }

        if (juheBaseResp.getError_code() != 0) {
            // 充值失败，回滚
            LOGGER.error("调用第三方接口失败，需要回滚,error_code:{},reason:{}", juheBaseResp.getError_code(),
                    juheBaseResp.getReason());
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        } else {
            BroadbandOnlineorderResp moResp = juheBaseResp.getResult();
            LOGGER.info("调用第三方接口成功，cardid{},game_userid:{},game_state{},uorderid:{},sporder_id{},ordercash()",
                    moResp.getCardid(), moResp.getGame_userid(), moResp.getGame_state(), moResp.getUorderid(),
                    moResp.getSporder_id(), moResp.getOrdercash());
            this.addJuhePay(ownerBoundPO, map.get("orderid"), pervalue, moResp.getOrdercash(),
                    ownerPaymentPO.getAmount(), JuheDataPO.STATUS_PROCESSING, ownerPaymentPO.getPaymentId());
            return moResp;
        }

    }

    public FeeSDMDebtsPO getFeeSDMDebts(Long communityId, Long ownerId, Map<String, String> param) throws EdsiException {
        // 先绑定数据
        OwnerBoundPO ownerBoundPO = new OwnerBoundPO();
        ownerBoundPO.setBoundId(systemService.getId());
        ownerBoundPO.setCommunityId(communityId);
        ownerBoundPO.setOwnerId(ownerId);
        ownerBoundPO.setBoundType(param.get("boundType"));
        ownerBoundPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ownerBoundPO.setBoundName(param.get("boundType"));
        ownerBoundPO.setDisplayKey(param.get("account"));
        ownerBoundPO.setStatus(Constant.STATUS_NORMAL);
        ownerBoundPO.setValue1(param.get("provname"));
        ownerBoundPO.setValue2(param.get("provid"));
        ownerBoundPO.setValue3(param.get("cityname"));
        ownerBoundPO.setValue4(param.get("cityid"));
        ownerBoundPO = ownerBoundService.addBound(ownerBoundPO);

        param.put("key", KEY_SDM);

        JuheBaseResp<FeeSDMDebtsPO> juheBaseResp = null;
        try {
            String resp = HttpUtil.post(juhePreUrl + "public/balance", param).getBody();
            LOGGER.info("水电煤账单查询第三方接口返回的信息：{}", resp);
            Type type = new TypeToken<JuheBaseResp<FeeSDMDebtsPO>>() {
            }.getType();
            juheBaseResp = JsonUtil.convert(resp, type);
            return juheBaseResp.getResult();
        } catch (Exception e) {
            LOGGER.error("调用第三方接口失败!", e);
            throw new EdsiException("查询数据失败，请稍后再试！");
        }
    }

    public FeeSDMPayPO paySDM(Long communityId, Long ownerId, Map<String, String> map, String boundName)
            throws EdsiException {
        map.put("key", KEY_SDM);
        map.put("orderid", UniPayCore.getUniPaiId(systemService.getId(), 15));
        StringBuilder sb = new StringBuilder();
        sb.append(juheOpenId).append(KEY_SDM);
        sb.append(map.get("cardid")).append(map.get("cardnum")).append(map.get("orderid"));
        sb.append(map.get("provid")).append(map.get("cityid")).append(map.get("type")).append(map.get("code"));
        sb.append(map.get("account"));
        map.put("sign", Tools.getMd5(sb.toString()));

        // 先绑定数据
        OwnerBoundPO ownerBoundPO = new OwnerBoundPO();
        ownerBoundPO.setBoundId(systemService.getId());
        ownerBoundPO.setCommunityId(communityId);
        ownerBoundPO.setOwnerId(ownerId);
        ownerBoundPO.setBoundType(map.get("boundType"));
        ownerBoundPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ownerBoundPO.setBoundName(boundName);
        ownerBoundPO.setDisplayKey(map.get("account"));
        ownerBoundPO.setStatus(Constant.STATUS_NORMAL);
        ownerBoundPO.setValue1(map.get("provname"));
        map.remove("provname");
        ownerBoundPO.setValue2(map.get("provid"));
        ownerBoundPO.setValue3(map.get("cityname"));
        map.remove("cityname");
        ownerBoundPO.setValue4(map.get("cityid"));
        ownerBoundPO = ownerBoundService.addBound(ownerBoundPO);

        // 先扣款，再充值
        OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
        ownerPaymentPO.setCommunityId(communityId);
        ownerPaymentPO.setOwnerId(ownerId);
        ownerPaymentPO.setAmount(Tools.subtract("0", map.get("cardnum")));
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
        ownerPaymentPO.setDes(map.get("account") + map.get("boundType") + map.get("cardnum") + ",订单ID："
                + map.get("orderid"));
        ownerPaymentPO.setPayType(ownerBoundPO.getBoundType());
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_CONSUME);
        ownerPaymentPO.setSysUserOptFlag(OwnerPaymentPO.SYS_OPT_TYPE_SHENGHUOFEE);
        try {
            OwnerAccount account = paymentService.getOwnerAccount(ownerId, communityId);
            if (account != null && account.getMoneyAmt() >= Double.parseDouble(map.get("cardnum"))) {
                ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_JUHE + "," + boundName);
                paymentService.ownerConsume(ownerPaymentPO);
            } else {
                LOGGER.error("MobileFeeService，现金余额不足或账号信息不存在");
                throw new EdsiException("个人账户支付失败，请稍后再试");
            }
        } catch (Exception e) {
            LOGGER.error("MobileFeeService", e);
            throw new EdsiException("个人账户支付失败，请稍后再试");
        }

        JuheBaseResp<FeeSDMPayPO> juheBaseResp = null;
        try {
            String resp = HttpUtil.post(juhePreUrl + "public/order", map).getBody();
            LOGGER.info("水电煤充值调用第三方接口返回的信息：{}", resp);
            Type type = new TypeToken<JuheBaseResp<FeeSDMPayPO>>() {
            }.getType();
            juheBaseResp = JsonUtil.convert(resp, type);
        } catch (Exception e) {
            LOGGER.error("调用第三方接口失败，需要回滚", e);
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        }

        if (juheBaseResp.getError_code() != 0) {
            // 充值失败，回滚
            LOGGER.error("调用第三方接口失败，需要回滚,error_code:{},reason:{}", juheBaseResp.getError_code(),
                    juheBaseResp.getReason());
            rolBack(ownerPaymentPO, paymentService);
            throw new EdsiException("充值失败！");
        } else {
            FeeSDMPayPO moResp = juheBaseResp.getResult();
            LOGGER.info("调用第三方接口成功，cardid{},ordercash:{},cardname{},account:{},uorderid{},status()",
                    moResp.getCardid(), moResp.getOrdercash(), moResp.getCardname(), moResp.getAccount(),
                    moResp.getUorderid(), moResp.getStatus());
            this.addJuhePay(ownerBoundPO, map.get("orderid"), map.get("cardnum"), moResp.getOrdercash(),
                    ownerPaymentPO.getAmount(), JuheDataPO.STATUS_PROCESSING, ownerPaymentPO.getPaymentId());
            return moResp;
        }
    }

    @Override
    public List<JuheDataPO> getPayList(JuheQueryCond juheQueryCond) throws EdsiException {
        Map<String, Object> sqlMap = this.buildSqlMap(juheQueryCond);
        return juheDataDao.getJuhePayList(sqlMap);
    }

    @Override
    public int getPayListCount(JuheQueryCond juheQueryCond) throws EdsiException {
        Map<String, Object> sqlMap = this.buildSqlMap(juheQueryCond);
        return juheDataDao.getJuhePayListCount(sqlMap);
    }

    private Map<String, Object> buildSqlMap(JuheQueryCond juheQueryCond) {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("communityId", juheQueryCond.getCommunityId());
        sqlMap.put("ownerId", juheQueryCond.getOwnerId());
        sqlMap.put("displayKey", juheQueryCond.getDisplayKey());
        sqlMap.put("statuses", juheQueryCond.getStatuses());
        sqlMap.put("startTime", juheQueryCond.getStartTime());
        sqlMap.put("endTime", juheQueryCond.getEndTime());
        sqlMap.put("settleStatus", juheQueryCond.getSettleStatus());
        sqlMap.put("settlementId", juheQueryCond.getSettlementId());
        if (juheQueryCond.getStartIndex() >= 0) {
            sqlMap.put("startIndex", juheQueryCond.getStartIndex());
            sqlMap.put("pageSize", juheQueryCond.getPageSize());
        }
        return sqlMap;
    }

    public void updateJuhePayStatus(JuheDataPO juheDataPO) throws EdsiException {
        juheDataDao.updateJuhePayStatus(juheDataPO);
    }

    private void addJuhePay(OwnerBoundPO ownerBoundPO, String orderId, String cardnum, double ordercash,
            double inprice, String status, Long paymentId) {
        try {
            JuheDataPO juheDataPO = new JuheDataPO();
            juheDataPO.setPayId(systemService.getId());
            juheDataPO.setPaymentId(paymentId);
            juheDataPO.setBoundId(ownerBoundPO.getBoundId());
            juheDataPO.setCommunityId(ownerBoundPO.getCommunityId());
            juheDataPO.setPropertyId(ownerBoundPO.getPropertyId());
            juheDataPO.setOrderid(orderId);
            juheDataPO.setOwnerId(ownerBoundPO.getOwnerId());
            juheDataPO.setOwnerName(ownerBoundPO.getOwnerName());
            juheDataPO.setBoundType(ownerBoundPO.getBoundType());
            juheDataPO.setBoundName(ownerBoundPO.getBoundName());
            juheDataPO.setDisplayKey(ownerBoundPO.getDisplayKey());
            juheDataPO.setValue1(ownerBoundPO.getValue1());
            juheDataPO.setValue2(ownerBoundPO.getValue2());
            juheDataPO.setValue3(ownerBoundPO.getValue3());
            juheDataPO.setValue4(ownerBoundPO.getValue4());
            juheDataPO.setValue5(ownerBoundPO.getValue5());
            juheDataPO.setValue6(ownerBoundPO.getValue6());
            juheDataPO.setValue7(ownerBoundPO.getValue7());
            juheDataPO.setValue8(ownerBoundPO.getValue8());
            juheDataPO.setCardnum(cardnum);
            juheDataPO.setOrdercash(String.valueOf(ordercash));
            juheDataPO.setInprice(String.valueOf(inprice));
            juheDataPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
            juheDataPO.setStatus(status);
            juheDataDao.insertJuhePay(juheDataPO);
        } catch (Exception e) {
            LOGGER.error("插入已付款信息失败", e);
        }
    }

    public static OwnerPaymentPO rolBack(OwnerPaymentPO ownerPaymentPO, IPaymentService paymentService) {
        LOGGER.info("开始回滚，communityId:{},ownerId:{},des{}", ownerPaymentPO.getCommunityId(),
                ownerPaymentPO.getOwnerId(), ownerPaymentPO.getDes());
        ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_RECHARGE);
        ownerPaymentPO.setAmount(Tools.subtract(0, ownerPaymentPO.getAmount()));
        ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
        ownerPaymentPO.setDes(ownerPaymentPO.getDes() + ",支付失败，回滚");

        ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_JUHE_ROLBACK);
        try {
            return paymentService.ownerRecharge(ownerPaymentPO);
        } catch (Exception e) {
            LOGGER.error("回滚失败，需要手工处理", e);
        }

        LOGGER.info("回滚完成，communityId:{},ownerId:{},des{}", ownerPaymentPO.getCommunityId(),
                ownerPaymentPO.getOwnerId(), ownerPaymentPO.getDes());
        return null;
    }

    @Override
    public CarWeiZhangResp queryWZ(Long communityId, Long ownerId, Map<String, String> param, long boundId)
            throws EdsiException {

        // 先绑定数据
        OwnerBoundPO ownerBoundPO = new OwnerBoundPO();
        ownerBoundPO.setBoundId(systemService.getId());
        ownerBoundPO.setCommunityId(communityId);
        ownerBoundPO.setOwnerId(ownerId);
        ownerBoundPO.setBoundType(OwnerBoundPO.BOUND_TYPE_CAR);
        ownerBoundPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        ownerBoundPO.setBoundName(OwnerBoundPO.BOUND_TYPE_CAR);
        ownerBoundPO.setDisplayKey(param.get("hphm"));
        ownerBoundPO.setStatus(Constant.STATUS_NORMAL);
        ownerBoundPO.setValue1(param.get("cityCode"));
        ownerBoundPO.setValue3(param.get("engineno"));
        ownerBoundPO.setValue4(param.get("classno"));
        ownerBoundPO.setValue5(param.get("provinceCode"));
        ownerBoundPO.setValue6(param.get("cityName"));
        ownerBoundPO.setValue7(param.get("provinceName"));
        ownerBoundService.addBound(ownerBoundPO);

        Map<String, String> map = new HashMap<String, String>();
        map.put("cityid", param.get("cityCode"));
        map.put("carno", param.get("hphm"));
        map.put("engineno", param.get("engineno"));
        map.put("classno", param.get("classno"));
        map.put("key", KEY_CAR);

        try {
            String resp = null;
            if (isTest) {
                resp = "{\"reason\":\"@@T2查询成功\",\"result\":{\"province\":\"湖北\",\"city\":\"武汉\",\"carno\":\"鄂A70L02\",\"hpzl\":\"02\",\"lists\":[{\"date\":\"2015-02-12 12:08:00\",\"area\":\"【湖北武汉】黄孝河路育才路路口至江大路路口\",\"act\":\"机动车违反禁止停车标志指示的\",\"code\":\"10393\",\"fen\":\"3\",\"money\":\"200\",\"handled\":\"0\"},{\"date\":\"2015-02-12 12:08:00\",\"area\":\"【湖北武汉】就是那条路路口\",\"act\":\"机动车违反禁止停车标志指示的\",\"code\":\"10393\",\"fen\":\"12\",\"money\":\"2000\",\"handled\":\"0\"}]},\"error_code\":0}";
            } else {
                resp = HttpUtil.post("http://v.juhe.cn/wzcxy/query", map).getBody();
            }

            LOGGER.info("违章查询调用第三方接口返回的信息：{}", resp);
            Type type = new TypeToken<JuheBaseResp<CarWeiZhangResp>>() {
            }.getType();
            JuheBaseResp<CarWeiZhangResp> juheBaseResp = JsonUtil.convert(resp, type);
            if (juheBaseResp.getError_code() != 0) {
                throw new EdsiException(juheBaseResp.getReason());
            }
            return juheBaseResp.getResult();
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new EdsiException("查询信息失败，请稍后再试");
        }
    }

    @Override
    public void settlement(Long communityId, String createUser) throws EdsiException {
        // 计算结算金额
        String endTime = TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD);
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("communityId", communityId);
        sqlMap.put("endTime", endTime);
        List<JuheSettlementPO> list = juheDataDao.getNotSettlementList(sqlMap);
        if (list == null || list.size() != 1) {
            throw new EdsiException("系统出现异常，请与管理员联系！");
        }

        JuheSettlementPO juheSettlementPO = new JuheSettlementPO();
        juheSettlementPO.setSettlementId(systemService.getId());
        juheSettlementPO.setCommunityId(communityId);
        juheSettlementPO.setCreateUser(createUser);
        juheSettlementPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        juheSettlementPO.setEndTime(endTime);
        juheSettlementPO.setMoney(list.get(0).getMoney());

        sqlMap.put("settleStatus", "1");
        sqlMap.put("settlementId", juheSettlementPO.getSettlementId());
        juheDataDao.updateJuhePaySettleStatus(sqlMap);
        juheDataDao.settlement(juheSettlementPO);
    }

    public void changeSettlementStatus(Long settlementId, Long communityId, int status, String user)
            throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("settlementId", settlementId);
        sqlMap.put("communityId", communityId);
        sqlMap.put("status", String.valueOf(status));
        if (status == IJuheDataService.SETTLE_STATUS_20) {
            sqlMap.put("passUser", user);
        } else if (status == IJuheDataService.SETTLE_STATUS_30) {
            sqlMap.put("payUser", user);
        } else if (status == IJuheDataService.SETTLE_STATUS_40) {
            sqlMap.put("confirmUser", user);
        }
        sqlMap.put("dateTime", TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        sqlMap.put("preStatus", String.valueOf(status - 10));
        juheDataDao.changeSettlementStatus(sqlMap);
    }

    @Override
    public List<JuheSettlementPO> getSettlementList(Long communityId, int status, int startIndex, int size)
            throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("communityId", communityId);
        if (status == SETTLE_STATUS_NOTSETTLE) {
            // 查询待结算信息
            if (communityId == null || communityId.longValue() <= 0) {
                CommunityPO communityPO = new CommunityPO();
                communityPO.setAdvertAt(CommunityPO.ADVERTAT_COMM);
                List<CommunityPO> commList = communityService.getList(communityPO, -1, -1);
                sqlMap.put("commList", commList);
            }
            return juheDataDao.getNotSettlementList(sqlMap);
        } else {
            if (status != SETTLE_STATUS_SETTLE) {
                sqlMap.put("status", status);
            }
            if (startIndex >= 0) {
                sqlMap.put("startIndex", startIndex);
                sqlMap.put("pageSize", size);
            }
            return juheDataDao.getSettlementList(sqlMap);
        }

    }

    @Override
    public int getSettlementListCount(Long communityId, int status) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("communityId", communityId);
        if (status == SETTLE_STATUS_NOTSETTLE) {
            // 查询待结算信息
            if (communityId == null || communityId.longValue() <= 0) {
                CommunityPO communityPO = new CommunityPO();
                communityPO.setAdvertAt(CommunityPO.ADVERTAT_COMM);
                List<CommunityPO> commList = communityService.getList(communityPO, -1, -1);
                sqlMap.put("commList", commList);
            }
            return juheDataDao.getNotSettlementListCount(sqlMap);
        } else {
            if (status != SETTLE_STATUS_SETTLE) {
                sqlMap.put("status", status);
            }
            return juheDataDao.getSettlementListCount(sqlMap);
        }
    }

    @Override
    public void callPhone(String phone, String call) throws EdsiException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("phone", phone);
        map.put("call", call);
        map.put("unid", String.valueOf(systemService.getId()));
        map.put("key", KEY_CALLSXHH);
        map.put("sign", Tools.getMd5(KEY_CALLSXHH + juheOpenId + map.get("unid") + phone + call));
        JuheBaseResp<Object> juheBaseResp = null;
        try {
            String resp = HttpUtil.post("http://op.juhe.cn/huihu/query", map).getBody();
            LOGGER.info("双向回呼第三方接口返回的信息：{}", resp);
            Type type = new TypeToken<JuheBaseResp<Object>>() {
            }.getType();
            juheBaseResp = JsonUtil.convert(resp, type);
            if (juheBaseResp.getError_code() == 0) {
                return;
            } else {
                LOGGER.error("双向回呼调用第三方接口失败{}", resp);
                throw new EdsiException("呼叫失败！");
            }
        } catch (Exception e) {
            LOGGER.error("双向回呼调用第三方接口失败，需要回滚", e);
            throw new EdsiException("呼叫失败！");
        }

    }

}
