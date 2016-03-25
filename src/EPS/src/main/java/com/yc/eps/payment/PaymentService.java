/*
 * E社区
 * Copyright (c) 2013 YT All Rights Reserved.
 */
package com.yc.eps.payment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.yc.commons.LockMap;
import com.yc.commons.TimeUtil;
import com.yc.commons.Tools;
import com.yc.commons.Utility;
import com.yc.edsi.cash.ICashService;
import com.yc.edsi.cash.UserCashInfoPO;
import com.yc.edsi.commons.Constant;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.community.ICommunityService;
import com.yc.edsi.community.IPropertyService;
import com.yc.edsi.ordermain.IOrderMainService;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerPO;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.edsi.payment.IPaymentService;
import com.yc.edsi.payment.OwnerAccount;
import com.yc.edsi.payment.OwnerPaymentPO;
import com.yc.edsi.payment.OwnerPaymentStatisticsPO;
import com.yc.edsi.payment.SellerPaymentPO;
import com.yc.edsi.payment.score.IOwnerScoreService;
import com.yc.edsi.payment.third.AlipayRSAPO;
import com.yc.edsi.payment.third.UniPayPO;
import com.yc.edsi.seller.ISellerService;
import com.yc.edsi.seller.SellerPO;
import com.yc.edsi.system.ISystemService;
import com.yc.edsi.system.SystemGiveConfigPO;
import com.yc.edsi.teleCom.ITeleComService;
import com.yc.edsi.teleCom.TeleRechargeHistoryPO;
import com.yc.eps.alipay.AlipayService;
import com.yc.eps.unipay.UniPayService;
import com.yc.eps.weixin.WeixinPayService;

/**
 * 支付服务
 * 
 * @author <a href="mailto:jiab@yichenghome.com">Jia b</a>
 * @version 1.0
 * @since 2014年2月11日
 */

@Service
public class PaymentService implements IPaymentService {
    private final static Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final static String MODULE_NAME = "交易服务";

    @Resource
    private IPaymentDao paymentDao;

    @Resource
    private ISystemService systemService;

    @Resource
    private IOwnerService ownerService;

    @Resource
    private ISellerService sellerService;

    @Resource
    private IOrderMainService orderMainService;

    @Resource
    private AlipayService alipayService;

    @Resource
    private UniPayService uniPayService;

    @Resource
    private WeixinPayService weixinPayService;

    @Resource
    private IPropertyService propertyService;

    @Resource
    private ICommunityService communityService;

    @Resource
    private ICashService edsCashService;

    @Resource
    private IOwnerScoreService ownerScoreService;
    
    @Resource
    private ITeleComService teleComService;

    @Autowired
    private ApplicationContext applicationContext;

    public String getAlipayRSATrade(AlipayRSAPO alipayRSAPO) throws EdsiException {
        return alipayService.getClientMessage(alipayRSAPO);
    }

    public String getUniTn(UniPayPO uniPO) throws EdsiException {
        return uniPayService.getUniTn(uniPO);
    }

    public SellerPaymentPO setTradeFinish(long paymentId, long ownerId, long sellerId, Long communityId, Double amount)
            throws EdsiException {
        logger.info("PaymentService,方法：setTradeFinish,参数[paymentId:{},ownerId:{},sellerId:{},{}]", paymentId, ownerId,
                sellerId, amount);
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("paymentId", paymentId);
        sqlMap.put("communityId", communityId);
        OwnerPaymentPO oPO = paymentDao.getOwnerAccount(sqlMap);
        if (oPO.getOwnerId() != ownerId || oPO.getSellerId() != sellerId || oPO.getAmount() != amount) {
            throw new EdsiException("交易数据不一致");
        }

        SellerPaymentPO sellerPaymentPO = this.copyOwnerPaymentPO(oPO);
        sellerPaymentPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        sellerPaymentPO.setFlow(SellerPaymentPO.FLOW_SUCCESS);
        sellerPaymentPO.setApplySettlement(SellerPaymentPO.APPLYSETTLEMENT_INIT);
        sellerPaymentPO.setFinishSettlement(SellerPaymentPO.FINISHSETTLEMENT_INIT);
        sellerPaymentPO.setsPaymentId(systemService.getId());
        sellerPaymentPO.setIsCommPayee(isCommPayee(sellerPaymentPO.getCommunityId()));
        paymentDao.insertSellerPayment(sellerPaymentPO);
        applicationContext.publishEvent(new SellerPaymentAddEvent(sellerPaymentPO));
        return sellerPaymentPO;
    }

    private SellerPaymentPO copyOwnerPaymentPO(OwnerPaymentPO ownerPaymentPO) {
        SellerPaymentPO sellerPaymentPO = new SellerPaymentPO();
        sellerPaymentPO.setoPaymentId(ownerPaymentPO.getPaymentId());
        sellerPaymentPO.setCommunityId(ownerPaymentPO.getCommunityId());
        sellerPaymentPO.setPropertyId(ownerPaymentPO.getPropertyId());
        sellerPaymentPO.setOwnerId(ownerPaymentPO.getOwnerId());
        sellerPaymentPO.setOwnerUserId(ownerPaymentPO.getOwnerUserId());
        sellerPaymentPO.setOwnerUserNo(ownerPaymentPO.getOwnerUserNo());
        sellerPaymentPO.setOwnerUserName(ownerPaymentPO.getOwnerUserName());
        sellerPaymentPO.setOrderId(ownerPaymentPO.getOrderId());
        sellerPaymentPO.setSellerId(ownerPaymentPO.getSellerId());
        sellerPaymentPO.setSellerNo(ownerPaymentPO.getSellerNo());
        sellerPaymentPO.setSellerName(ownerPaymentPO.getSellerName());
        sellerPaymentPO.setAmount(Math.abs(ownerPaymentPO.getAmount()));
        sellerPaymentPO.setPayType(ownerPaymentPO.getPayType());
        // 设置所属分公司ID
        sellerPaymentPO.setSubcomId(ownerPaymentPO.getSubcomId());
        return sellerPaymentPO;
    }

    public OwnerPaymentPO ownerRecharge(OwnerPaymentPO paymentPO) throws EdsiException {
        logger.info("PaymentService,方法：ownerRecharge,参数paymentPO={}", paymentPO.toString());
        if (paymentPO.getAmount() <= 0) {
            throw new EdsiException("充值金额应该大于0");
        }
        if (!IPaymentService.PAY_FLOW_RECHARGE.equals(paymentPO.getFlow())) {
            throw new EdsiException("FLOW标志有误");
        }

        if (paymentPO.getOwnerId() <= 0) {
            throw new EdsiException("用户Id不能小于等于0");
        }

        OwnerPO ownerPO = ownerService.getByIdOnly(paymentPO.getOwnerId());
        if (ownerPO == null) {
            throw new EdsiException("用户不存在");
        }
        if (!Constant.STATUS_NORMAL.equals(ownerPO.getStatus())) {
            throw new EdsiException("用户不可用");
        }

        paymentPO.setCardNo(ownerPO.getCardNo());
        paymentPO.setOwnerName(ownerPO.getOwnerName());
        paymentPO.setPropertyId(ownerPO.getPropertyId());

        paymentPO.setPaymentId(systemService.getId());
        // 设置门牌号
        paymentPO.setHouseNumber(ownerPO.getObHouseNumber());
        paymentPO.setIsCommPayee(isCommPayee(paymentPO.getCommunityId()));
        // 设置所属分公司ID
        paymentPO.setSubcomId(ownerPO.getSubcomId());

        if (StringUtils.isBlank(paymentPO.getCreateTime())) {
            paymentPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
        }

        paymentDao.insertOwnerPayment(paymentPO);
        return paymentPO;
    }

    private String isCommPayee(long communityId) throws EdsiException {
        String advertAt = communityService.getById(communityId).getAdvertAt();
        if ("2".equals(advertAt)) {
            return "1";
        } else {
            return "0";
        }
    }

    @Transactional(rollbackForClassName = "EdsiException")
    public OwnerPaymentPO ownerClientConsume(Long ownerUserId, long sellerId, Double amount, long communityId,
            Long orderId, int type, Long cashId) throws EdsiException {
        logger.info(
                "PaymentService,方法：ownerClientConsume,参数[ownerUserId:{},sellerId:{},amount:{},communityId:{},orderId:{},type:{}]",
                ownerUserId, sellerId, amount, communityId, orderId, type);
        synchronized (LockMap.getLock(orderId)) {// 同一订单同步
            // 检查是否已经支付
            if (orderId != null && orderId.longValue() > 0) {
                if (paymentDao.checkIfAlreadyPay(orderId, communityId) >= 1) {
                    throw new EdsiException("订单已经支付成功，不能重复支付");
                }
            }
            // type暂时不需要
            OwnerPaymentPO ownerPaymentPO = new OwnerPaymentPO();
            OwnerUserPO ownerUserPO = ownerService.getOwnerUserById(ownerUserId, communityId);
            if (ownerUserPO == null || StringUtils.isBlank((ownerUserPO.getOwnerUserNo()))) {
                throw new EdsiException("用户不存在");
            }
            SellerPO sellerPO = sellerService.getBaseSellerPO(sellerId);
            if (sellerPO == null || StringUtils.isBlank((sellerPO.getSellerNo()))) {
                throw new EdsiException("商家不存在");
            }

            // 关于代金券的使用
            UserCashInfoPO ucash = null;
            if (cashId != null) {
                ucash = edsCashService.getUserCash(cashId);
                if (ucash == null) {
                    throw new EdsiException("未找到使用的代金券!");
                }
                if (UserCashInfoPO.CASH_STATUS_USED.equals(ucash.getCashStatus())) {
                    throw new EdsiException("代金券已经使用过了!");
                }
                if (UserCashInfoPO.CASH_STATUS_LOSE.equals(ucash.getCashStatus())) {
                    throw new EdsiException("代金券已经失效!");
                }
                if (ucash.getCashBeginTime().after(new Date())) {
                    throw new EdsiException("代金券还未到使用时间!");
                }
                // 过期时间转换为当天的23:59:59然后再比较
                Date endTime = TimeUtil.parse(TimeUtil.format(ucash.getCashEndTime(), TimeUtil.YYYY_MM_DD)
                        + " 23:59:59", TimeUtil.YYYY_MM_DD_HH_MM_SS);
                if (endTime.before(new Date())) {
                    throw new EdsiException("代金券已经超过使用时间!");
                }
            }
            ownerPaymentPO.setOwnerId(ownerUserPO.getOwnerId());
            ownerPaymentPO.setOwnerUserId(ownerUserPO.getOwnerUserId());
            ownerPaymentPO.setOwnerUserNo(ownerUserPO.getOwnerUserNo());
            ownerPaymentPO.setOwnerUserName(ownerUserPO.getOwnerUserName());
            ownerPaymentPO.setPropertyId(ownerUserPO.getPropertyId());
            ownerPaymentPO.setCardNo(ownerUserPO.getCardNo());

            ownerPaymentPO.setSellerId(sellerId);
            ownerPaymentPO.setSellerNo(sellerPO.getSellerNo());
            ownerPaymentPO.setSellerName(sellerPO.getSellerName());
            // 消费金额为负
            Double cashAmt = 0d;
            String uct = "";
            if (ucash != null) {
                ownerPaymentPO.setCashId(ucash.getCashId());
                ownerPaymentPO.setCashNo(ucash.getCashNo());
                // 实际使用代金券金额
                ownerPaymentPO.setCashAmt((amount > ucash.getCashAmt() ? ucash.getCashAmt() : amount));
                uct = ucash.getCashSendType();
                cashAmt = ucash.getCashAmt();
                if (cashAmt > 0 && UserCashInfoPO.CASH_SEND_TYPE_PSALES.equals(uct)
                        || UserCashInfoPO.CASH_SEND_TYPE_SCRATCH.equals(uct)) {
                    // 当代金券金额大于支付金额时候,反充代金券金额为支付金额
                    Double oldCashAmt = cashAmt;
                    Double subAmt = Tools.subtract(cashAmt, amount);
                    if (subAmt > 0) {// 代金券金额大于支付金额
                        cashAmt = amount;
                    }
                    // 1.0 翼社区平台代金券、或者刮刮卡
                    ownerPaymentPO.setCashSendType(OwnerPaymentPO.CASH_SEND_TYPE_PLFORM);
                    ownerPaymentPO.setCashSenderId(-1l);
                    ownerPaymentPO.setCashSendName("翼社区平台");
                    // 2.0 翼社区平台发放的代金券直接给用户充值优惠，然后消费,使用时无需减少扣费
                    OwnerPaymentPO ownerRecharge = new OwnerPaymentPO();
                    ownerRecharge.setOwnerId(ownerUserPO.getOwnerId());
                    ownerRecharge.setOwnerUserId(ownerUserPO.getOwnerUserId());
                    ownerRecharge.setOwnerUserNo(ownerUserPO.getOwnerUserNo());
                    ownerRecharge.setOwnerUserName(ownerUserPO.getOwnerUserName());
                    ownerRecharge.setPropertyId(ownerUserPO.getPropertyId());
                    ownerRecharge.setCommunityId(communityId);
                    ownerRecharge.setAmount(cashAmt);
                    ownerRecharge.setCash(0d);
                    ownerRecharge.setExtCash(cashAmt);
                    ownerRecharge.setCashSendType(OwnerPaymentPO.CASH_SEND_TYPE_PLFORM);
                    ownerRecharge.setCashSenderId(-1l);
                    ownerRecharge.setCashSendName("翼社区平台");
                    ownerRecharge.setDes("翼社区平台发放代金券消费充值");
                    ownerRecharge.setDisplayDes("使用代金券充值");
                    ownerRecharge.setCreateUser("系统管理员");
                    ownerRecharge.setSysUserOptFlag("1");
                    ownerRecharge.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
                    ownerRecharge.setPayType("代金券");
                    ownerRecharge.setFlow("1");
                    ownerRecharge.setCashId(ucash.getCashId());
                    ownerRecharge.setCashNo(ucash.getCashNo());
                    ownerRecharge.setCashAmt(oldCashAmt);
                    ownerRecharge(ownerRecharge);
                } else if (UserCashInfoPO.CASH_SEND_TYPE_SELLER.equals(uct)) {
                    // 商家发送代金券
                    ownerPaymentPO.setCashSendType(OwnerPaymentPO.CASH_SEND_TYPE_SELLER);
                    ownerPaymentPO.setCashSenderId(Long.valueOf(ucash.getCashEffectScope()));
                    ownerPaymentPO.setCashSendName(ucash.getCashEffectShopName());
                }
            }
            if (UserCashInfoPO.CASH_SEND_TYPE_PSALES.equals(uct) || UserCashInfoPO.CASH_SEND_TYPE_SCRATCH.equals(uct)) {
                // 翼社区平台发放的代金券,无需减少扣费
                ownerPaymentPO.setAmount(Tools.subtract(0, amount));
            } else {
                Double subAmt = Tools.subtract(cashAmt, amount);
                if (cashAmt > 0 && subAmt > 0) {
                    // 大于0说明代金券金额超出购物金额,按购买金额扣费
                    ownerPaymentPO.setAmount(Tools.subtract(0, amount));
                } else {
                    ownerPaymentPO.setAmount(subAmt);
                }
            }
            // 获取用户现金金额和返现金额,购买商品和服务（优先扣除代返现金额）
            OwnerAccount oa = getOwnerAccount(ownerUserPO.getOwnerId(), communityId);
            if (oa != null) {
                if (oa.getPreferentialAmt() > 0) {
                    Double amt = Math.abs(ownerPaymentPO.getAmount());// 获取消费金额绝对值（大于0）
                    if (oa.getPreferentialAmt() >= amt) {// 返现账户金额够使用
                        ownerPaymentPO.setExtCash(ownerPaymentPO.getAmount());
                    } else {// 返现账户不够，需要扣返现和现金
                        ownerPaymentPO.setExtCash(Tools.subtract(0, oa.getPreferentialAmt()));
                        Double lastAmt = amt - oa.getPreferentialAmt();// 需要扣除现金的部分
                        ownerPaymentPO.setCash(Tools.subtract(0, lastAmt));
                    }
                } else {
                    ownerPaymentPO.setCash(ownerPaymentPO.getAmount());
                }
            }
            ownerPaymentPO.setCommunityId(communityId);
            ownerPaymentPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
            ownerPaymentPO.setFlow(IPaymentService.PAY_FLOW_CONSUME);
            ownerPaymentPO.setOrderId(orderId);
            ownerPaymentPO.setSysUserOptFlag("0");
            try {
                ownerPaymentPO.setDisplayDes(IPaymentService.DISPLAY_DES_CLIENTCONSUME);
                ownerPaymentPO = this.ownerConsume(ownerPaymentPO);
            } catch (EdsiException e) {
                throw e;
            } catch (Exception e) {
                logger.error(MODULE_NAME, e);
                throw new EdsiException("系统出现错误");
            }
            try {
                orderMainService.buyGoods(ownerUserPO, orderId, amount, ucash);
                try{
                	logger.info("订单["+orderId+"]支付成功，发放免费通话时间:");
                	sendFreeCost(sellerId, amount, orderId,ownerUserPO);
                } catch(Exception e){
                	logger.error("订单ID："+orderId+"支付成功，发放免费通话时间出现异常",e);
                }
            } catch (EdsiException e) {
                // 有事物，不回滚
                logger.error("订单出现异常，支付ID:{},订单ID：{}", ownerPaymentPO.getPaymentId(), orderId);
                throw e;
            } catch (Exception e) {
                logger.error("订单出现异常，支付ID:{},订单ID：{}", ownerPaymentPO.getPaymentId(), orderId);
                logger.error(MODULE_NAME, e);
                throw new EdsiException("系统出现错误");
            }
            if (ucash != null) {
                ucash.setCashOrderId(orderId);
                ucash.setCashStatus(UserCashInfoPO.CASH_STATUS_USED);
                ucash.setCashUseTime(new Date());
                try {
                    edsCashService.updateUserCash(ucash);
                } catch (Exception e) {
                    // 代金券异常不影响订单
                    logger.error("订单出现异常，支付ID:{},订单ID：{},代金券ID:{}", ownerPaymentPO.getPaymentId(), orderId,
                            ucash.getCashId());
                    logger.error(MODULE_NAME, e);
                }
            }
            return ownerPaymentPO;
        }
    }

    /**
     * 检测并发放免费通话时间
     * @param sellerId
     * @param amount
     * @param orderId
     * @param ownerUser
     * @throws Exception 
     */
    private void sendFreeCost(Long sellerId, Double amount, Long orderId,
			OwnerUserPO user) throws Exception {
		//1.0 检测首次下单送话费情况
    	OwnerPO owner = ownerService.getByIdOnly(user.getOwnerId());
    	if(owner != null){
    		user.setPhone(owner.getPhone());
    	}
        SystemGiveConfigPO firstOrderCost = systemService.getPlaceFirstOrderCost(user.getOwnerId());
        if(firstOrderCost != null){
        	teleComService.rechargeTeleCost(user,firstOrderCost.getGiveAmount().intValue(),
        			TeleRechargeHistoryPO.RECHARGE_SOURCE_FIRST_ORDER,"首次下单订单Id:"+orderId);
        }
    	//2.0 检查下单送话费情况
        SystemGiveConfigPO orderCost = systemService.getPlaceOrderCost(sellerId, amount);
        if(orderCost != null){
        	teleComService.rechargeTeleCost(user, orderCost.getGiveAmount().intValue(),
        			TeleRechargeHistoryPO.RECHARGE_SOURCE_PLACE_ORDER,"订单Id:"+orderId);
        }
	}

	public OwnerPaymentPO getOwnerPaymentPO(Long ownerId, long communityId, Long paymentId) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("ownerId", ownerId);
        sqlMap.put("communityId", communityId);
        sqlMap.put("paymentId", paymentId);
        return paymentDao.getOwnerPaymentPO(sqlMap);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "EdsiException")
    public OwnerPaymentPO ownerConsume(OwnerPaymentPO paymentPO) throws EdsiException {
        logger.info("PaymentService,方法：ownerConsume,参数paymentPO={}", paymentPO.toString());
        // Modify by wenxw 20140428,目前数据生产环境中存在金额为零的服务
        if (paymentPO.getAmount() > 0) {
            throw new EdsiException("消费金额应该小于0");
        } else {// 票据不为空，金额小于0则为提现，更新amount到cash
            if (Utility.isNotEmpty(paymentPO.getBillNo())) {
                paymentPO.setCash(paymentPO.getAmount());
            }
        }
        if (!IPaymentService.PAY_FLOW_CONSUME.equals(paymentPO.getFlow())) {
            throw new EdsiException("FLOW标志有误");
        }

        if (paymentPO.getOwnerId() <= 0) {
            throw new EdsiException("用户Id不能小于等于0");
        }

        OwnerPO ownerPO = ownerService.getById(paymentPO.getOwnerId(), paymentPO.getCommunityId());
        if (ownerPO == null) {
            throw new EdsiException("用户不存在");
        }
        if (!Constant.STATUS_NORMAL.equals(ownerPO.getStatus())) {
            throw new EdsiException("用户不可用");
        }

        paymentPO.setCardNo(ownerPO.getCardNo());
        paymentPO.setOwnerName(ownerPO.getOwnerName());
        paymentPO.setPropertyId(ownerPO.getPropertyId());
        // 设置门牌号
        paymentPO.setHouseNumber(ownerPO.getObHouseNumber());
        // 设置所属分公司ID
        paymentPO.setSubcomId(ownerPO.getSubcomId());

        try {
            double balance = this.getOwnerAccountBalance(paymentPO.getOwnerId(), paymentPO.getCommunityId());
            if (Tools.add(balance, paymentPO.getAmount()) >= 0) {
                paymentPO.setPaymentId(systemService.getId());
                paymentPO.setIsCommPayee(isCommPayee(paymentPO.getCommunityId()));
                if (StringUtils.isBlank(paymentPO.getCreateTime())) {
                    paymentPO.setCreateTime(TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD_HH_MM_SS));
                }

                paymentDao.insertOwnerPayment(paymentPO);
            } else {
                throw new EdsiException("您的账户金额为" + balance + ",余额不足，消费失败！");
            }

            // 为了防止并发，重新计算余额
            balance = this.getOwnerAccountBalance(paymentPO.getOwnerId(), paymentPO.getCommunityId());

            if (balance >= 0) {
                // 消费成功
                return paymentPO;
            }

            throw new EdsiException("余额不足，消费失败！");
        } catch (EdsiException e) {
            logger.error(MODULE_NAME, e);
            throw e;
        } catch (Exception e) {
            logger.error(MODULE_NAME, e);
            throw new EdsiException("系统出现异常");
        }
    }

    public double getOwnerAccountBalance(long ownerId, long communityId) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("ownerId", ownerId);
        sqlMap.put("communityId", communityId);
        Double rs = paymentDao.getOwnerAccountBalance(sqlMap);
        return rs == null ? 0.0 : Tools.add(rs, 0.00);
    }

    public int getOwnerAccountListCount(long ownerId, long communityId, String rangePropertyIds, String startTime,
            String endTime) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("ownerId", ownerId);
        sqlMap.put("communityId", communityId);
        sqlMap.put("startTime", startTime);
        sqlMap.put("endTime", endTime);
        sqlMap.put("rangePropertyIds", rangePropertyIds);
        sqlMap.put("propertyIds", Utility.getSplitList(rangePropertyIds, ","));
        return paymentDao.getOwnerAccountListCount(sqlMap);
    }

    public List<OwnerPaymentPO> getOwnerAccountList(long ownerId, long communityId, String rangePropertyIds,
            int startIndex, int size, String startTime, String endTime) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("ownerId", ownerId);
        sqlMap.put("communityId", communityId);
        sqlMap.put("startTime", startTime);
        sqlMap.put("endTime", endTime);
        if (startIndex >= 0) {
            sqlMap.put("startIndex", String.valueOf(startIndex));
            sqlMap.put("pageSize", String.valueOf(size));
        }
        sqlMap.put("rangePropertyIds", rangePropertyIds);
        sqlMap.put("propertyIds", Utility.getSplitList(rangePropertyIds, ","));
        return paymentDao.getOwnerAccountList(sqlMap);
    }

    public List<OwnerPaymentPO> getListByBillNo(String billNo) {
        return paymentDao.getListByBillNo(billNo);
    }

    public int getAllOwnerAccountListCount(OwnerPaymentPO paymentPO, String rangePropertyIds) throws EdsiException {
        List<Long> propertyIds = Utility.getSplitList(rangePropertyIds, ",");
        return paymentDao.getAllOwnerAccountListCount(paymentPO, rangePropertyIds, propertyIds);
    }

    public List<OwnerPaymentPO> getAllOwnerAccountList(OwnerPaymentPO paymentPO, String rangePropertyIds)
            throws EdsiException {
        List<Long> propertyIds = Utility.getSplitList(rangePropertyIds, ",");
        return paymentDao.getAllOwnerAccountList(paymentPO, rangePropertyIds, propertyIds);
    }

    public List<OwnerPaymentStatisticsPO> getRechargeStatistics(OwnerPaymentPO paymentPO) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("createStartTime", paymentPO.getCreateStartTime());
        sqlMap.put("createEndTime", paymentPO.getCreateEndTime());
        sqlMap.put("communityId", paymentPO.getCommunityId());
        sqlMap.put("propertyId", paymentPO.getPropertyId());
        sqlMap.put("startIndex", paymentPO.getStartIndex());
        sqlMap.put("pageSize", paymentPO.getPageSize());
        return paymentDao.getRechargeStatistics(sqlMap);
    }

    public int getRechargeStatisticsCount(OwnerPaymentPO paymentPO) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("createStartTime", paymentPO.getCreateStartTime());
        sqlMap.put("createEndTime", paymentPO.getCreateEndTime());
        sqlMap.put("communityId", paymentPO.getCommunityId());
        sqlMap.put("propertyId", paymentPO.getPropertyId());
        sqlMap.put("startIndex", paymentPO.getStartIndex());
        sqlMap.put("pageSize", paymentPO.getPageSize());
        return paymentDao.getRechargeStatisticsCount(sqlMap);
    }

    /**
     * 上月统计明细
     * 
     * @param paymentPO
     * @return
     * @throws EdsiException
     */
    public List<OwnerPaymentStatisticsPO> getStatisticsDetail(OwnerPaymentPO paymentPO) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("communityId", paymentPO.getCommunityId());
        sqlMap.put("propertyId", paymentPO.getPropertyId());
        sqlMap.put("propertyIds", paymentPO.getPropertyIds());
        sqlMap.put("createEndTime", paymentPO.getCreateEndTime());
        return paymentDao.getStatisticsDetail(sqlMap);
    }

    /**
     * 充值提现明细
     * 
     * @param paymentPO
     * @return
     * @throws EdsiException
     */
    public List<OwnerPaymentStatisticsPO> getReceivableStatistics(OwnerPaymentPO paymentPO) throws EdsiException {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("createStartTime", paymentPO.getCreateStartTime());
        sqlMap.put("createEndTime", paymentPO.getCreateEndTime());
        sqlMap.put("communityId", paymentPO.getCommunityId());
        sqlMap.put("propertyId", paymentPO.getPropertyId());
        sqlMap.put("propertyIds", paymentPO.getPropertyIds());
        return paymentDao.getReceivableStatistics(sqlMap);
    }

    public OwnerPaymentPO getOwnerPaymentInfo(Long orderId, Long communityId) {
        return paymentDao.getOwnerPaymentInfo(orderId, communityId);
    }

    public void updateBillNo(String billNo, Long paymentId, Long ownerId, Long communityId, String des) {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("billNo", billNo);
        sqlMap.put("paymentId", paymentId);
        sqlMap.put("ownerId", ownerId);
        sqlMap.put("communityId", communityId);
        sqlMap.put("des", des);
        paymentDao.updateBillNo(sqlMap);
    }

    @Override
    public OwnerAccount getOwnerAccount(long ownerId, long communityId) throws EdsiException {
        OwnerAccount oa = paymentDao.getAccount(ownerId, communityId);
        long score = ownerScoreService.getOwnerScore(ownerId, communityId);
        int donteScore = ownerService.getOwnerDonteScore(ownerId);
        if (oa == null) {
            oa = new OwnerAccount();
        }
        oa.setOwnerId(ownerId);
        oa.setScore(score);
        oa.setDonteScore(donteScore);
        return oa;
    }

    @Override
    public SellerPaymentPO getSellerPaymentByOrderId(Long orderId) throws EdsiException {
        return paymentDao.getSellerPaymentByOrderId(orderId);
    }

    @Override
    @Transactional
    public void delOwnerSellerPayment(Long orderId) throws EdsiException {
        OwnerPaymentPO op = paymentDao.getOwnerPaymentInfo(orderId, null);
        if (op != null) {
            if (op.getCashId() != null && OwnerPaymentPO.CASH_SEND_TYPE_PLFORM.equals(op.getCashSendType())) {
                // 删除代金券及充值的代金券(删除大于0的)
                paymentDao.delCashPayment(op.getOwnerId(), op.getCashSendType(), op.getCashId(), op.getCommunityId());
            }
            // 删除支付信息
            paymentDao.delOwnerPayment(op.getPaymentId(), op.getCommunityId());
        }
        // 更新结算日表信息
        paymentDao.updateSettleByOpid(op.getPaymentId(), orderId);
        // 删除待结算信息
        paymentDao.delSellerPayment(op.getPaymentId(), orderId);
    }
}
