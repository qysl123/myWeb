package com.yc.eps.payment.score;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.yc.edsi.teleCom.TeleRechargeHistoryPO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yc.commons.TimeUtil;
import com.yc.commons.Utility;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.gpurchase.GpurchasePO;
import com.yc.edsi.gpurchase.IGpurchaseSrevice;
import com.yc.edsi.order.IOrderService;
import com.yc.edsi.order.OrderDetailPO;
import com.yc.edsi.order.OrderPO;
import com.yc.edsi.order.OrderStatePO;
import com.yc.edsi.ordermain.IOrderMainService;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerPO;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.edsi.payment.score.IOwnerScoreService;
import com.yc.edsi.payment.score.IScoreTaskService;
import com.yc.edsi.payment.score.OwnerScorePO;
import com.yc.edsi.payment.score.ScoreTaskPO;
import com.yc.edsi.system.ISystemService;
import com.yc.edsi.teleCom.ITeleComService;

@Service
public class OwnerScoreService implements IOwnerScoreService {
    private final static Logger LOGGER = LoggerFactory.getLogger(OwnerScoreService.class);

    @Resource
    private IOwnerScoreDao ownerScoreDao;

    @Resource
    private IOrderMainService orderMainService;

    @Resource
    private IScoreTaskService scoreTaskService;

    @Resource
    private IOwnerService ownerService;

    @Resource
    private ISystemService systemService;
    @Resource
    private IOrderService      orderService;
    @Resource
    private IGpurchaseSrevice  gpurchaseSrevice;
    @Resource
    private ITeleComService teleComService;

    @Override
    public void addScore(OwnerScorePO score) throws EdsiException {
        if (score != null && score.getScore() != 0) {
            if (score.getId() == null || score.getId() == 0) {
                score.setId(systemService.getId());
            }
            ownerScoreDao.addScore(score);
        }
    }

    @Override
    public long getOwnerScore(Long ownerId, Long communityId) {
        return ownerScoreDao.getOwnerScore(ownerId, communityId);
    }

    @Override
    @Transactional
    public void ownerClientConsume(OwnerScorePO ownerScorePO) throws EdsiException {
        if (ownerScorePO == null || ownerScorePO.getOwnerId() <= 0 || ownerScorePO.getCommunityId() <= 0
                || ownerScorePO.getPropertyId() <= 0 || ownerScorePO.getScore() <= 0) {
            throw new EdsiException("传入的参数有误！");
        }

        Long score = ownerScorePO.getScore();

        try {
            if (ownerScorePO.getOrderId() > 0
                    && ownerScoreDao.checkIfAlreadyPay(ownerScorePO.getOrderId(), ownerScorePO.getCommunityId()) > 0) {
                throw new EdsiException("订单已经支付！");
            }

            if (this.getEffectiveScore(ownerScorePO.getOwnerId(), ownerScorePO.getCommunityId()) < ownerScorePO
                    .getScore()) {
                throw new EdsiException("您的翼钻不足，请继续加油获取翼钻！");
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("翼钻消耗模块出现异常", e);
            throw new EdsiException("系统出现异常，请稍后再试！");
        }
        ownerScorePO.setScore(0 - ownerScorePO.getScore());
        try {
            // 消耗翼钻
            this.addScore(ownerScorePO);

            if (this.getEffectiveScore(ownerScorePO.getOwnerId(), ownerScorePO.getCommunityId()) < 0) {
                ownerScorePO.setScore(score);
                ownerScorePO.setDesc(ownerScorePO.getDesc() + ",支付失败，冲正！");
                this.addScore(ownerScorePO);
                throw new EdsiException("支付出现异常，请重新下单并支付！");
            }

            if (ownerScorePO.getOrderId() > 0) {
                OwnerUserPO ownerUserPO = ownerService.getOwnerUserById(ownerScorePO.getOwnerUserId(),
                        ownerScorePO.getCommunityId());
                orderMainService.buyGoods(ownerUserPO, ownerScorePO.getOrderId(), 0, null);
            }
            //此时翼钻支付，则说明是翼钻兑换，则可能是虚拟话费商品兑换
            OrderPO order = orderService.getById(ownerScorePO.getOrderId());
            if(order != null && Utility.isNotEmpty(order.getOrderDetailPOs()) && order.getOrderDetailPOs().size() ==1){
            	OrderDetailPO detail  = order.getOrderDetailPOs().get(0);
            	GpurchasePO gpurchase = gpurchaseSrevice.getGpurchase(detail.getGpId());
            	if(gpurchase != null && gpurchase.getVirtualType() == GpurchasePO.VIRTUAL_TYPE_PHONE_BILL){//满天飞话费
            		prepaidPhoneBill(order,detail,gpurchase);
            	}
            }
        } catch (EdsiException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("翼钻消耗模块出现异常", e);
            throw new EdsiException("系统出现异常，请核对翼钻，如有问题请与客服联系！");
        }
    }

    /**
     * 充值满天飞话费并改变订单状态
     * @param  order
     * @param  detail
     * @param  gpurchase
     * @throws Exception 
     */
    private void prepaidPhoneBill(OrderPO order,OrderDetailPO detail, GpurchasePO gpurchase) throws Exception {
		int     duration = detail.getBoughtAmount() * Integer.valueOf(gpurchase.getVirtualValue());
		OwnerPO    owner = ownerService.getById(order.getOwnerId(),order.getCommunityId());
		OwnerUserPO user = ownerService.getOwnerUserById(order.getOwnerUserId(),order.getCommunityId());
		user.setPhone(owner.getPhone());
		if(StringUtils.isNoneBlank(owner.getPhone())){
			//0.0 充值满天飞话费
			try{				
				teleComService.rechargeTeleCost(user, duration, TeleRechargeHistoryPO.RECHARGE_SOURCE_SCORE_GOODS, "翼钻订单id:"+order.getId());
			} catch(Exception e){
				LOGGER.error("满天飞充值话费失败",e);
			}
			//1.0 改变订单状态,受理
			String optTime = TimeUtil.getNowDateYYYY_MM_DD_HH_MM_SS();
			OrderStatePO orderState = new OrderStatePO();
			orderState.setCreateTime(optTime);
			orderState.setOrderId(order.getId());
			orderState.setOrderState(OrderStatePO.ACCEPTED);
			orderState.setCreateUser("满天飞自动充值专员");
			orderService.changeOrderState(orderState,"","","");
			//2.0 改变订单状态,发货
			orderState.setOrderState(OrderStatePO.DELIVERED);
			orderService.changeOrderState(orderState,"","","");
		} else {
			throw new EdsiException("用户没有手机号码，兑换失败");
		}
	}

	private long getEffectiveScore(Long ownerId, Long communityId) {
        // TODO 暂时没有考虑有效期
        return this.getOwnerScore(ownerId, communityId);
    }

    @Override
    public ScoreTaskPO calAndSaveTaskScore(OwnerUserPO ownerUser, String optUri, boolean savePrompt) throws EdsiException {
        List<ScoreTaskPO> scoreTasks = scoreTaskService.getScoreTaskByOpt(optUri);
        if (CollectionUtils.isEmpty(scoreTasks))
            return null;
        for (ScoreTaskPO task : scoreTasks) {
            if (task.getIsRepeatable() == 0) {
                if (ownerScoreDao.getTaskScoreCount(toOwnerScoreQCond(ownerUser.getOwnerId(), task.getTaskId(), 0L)) < 1) {// 判断首次任务是否已完成
                    // 保存任务翼钻
                    ownerScoreDao.addScore(toOwnerScore(ownerUser, task, 0L, savePrompt));
                    return task;// 如果同时是首次任务和可重复任务，完成首次任务后，则忽略此次的可重复任务
                }
            }
        }
        for (ScoreTaskPO task : scoreTasks) {
            if (task.getIsRepeatable() == 1) {// 提出一条建议、邀请一个朋友、发布一个周边等
                // 保存任务翼钻
                ownerScoreDao.addScore(toOwnerScore(ownerUser, task, 0L, savePrompt));
                return task;
            }
        }
        return null;
    }

    @Override
    public ScoreTaskPO calAndSaveTaskScore(OwnerUserPO ownerUser, String optUri, Long orderId, boolean savePrompt)
            throws EdsiException {
        List<ScoreTaskPO> scoreTasks = scoreTaskService.getScoreTaskByOpt(optUri);
        if (CollectionUtils.isEmpty(scoreTasks))
            return null;
        Long firstTimeTaskId = 0L;
        for (ScoreTaskPO task : scoreTasks) {
            if (task.getIsRepeatable() == 0) {
                firstTimeTaskId = task.getTaskId();
                if (ownerScoreDao.getTaskScoreCount(toOwnerScoreQCond(ownerUser.getOwnerId(), task.getTaskId(), 0L)) < 1) {// 判断首次任务是否已完成
                    // 保存任务翼钻
                    ownerScoreDao.addScore(toOwnerScore(ownerUser, task, orderId, savePrompt));
                    return task;// 如果同时是首次任务和可重复任务，完成首次任务后，则忽略此次的可重复任务
                }
            }
        }
        for (ScoreTaskPO task : scoreTasks) {
            if (task.getIsRepeatable() == 1) {// 完成一次评价、完成一次晒单等
                // 相同订单只获得1次翼钻
                if (ownerScoreDao.getTaskScoreCount(toOwnerScoreQCond(ownerUser.getOwnerId(), firstTimeTaskId, orderId)) < 1
                        && ownerScoreDao.getTaskScoreCount(toOwnerScoreQCond(ownerUser.getOwnerId(), task.getTaskId(), orderId)) < 1) {
                    // 保存任务翼钻
                    ownerScoreDao.addScore(toOwnerScore(ownerUser, task, orderId, savePrompt));
                    return task;
                }
            }
        }
        return null;
    }

    private OwnerScorePO toOwnerScoreQCond(Long ownerId, Long taskId, Long orderId) {
        OwnerScorePO ownerScore = new OwnerScorePO();
        ownerScore.setOwnerId(ownerId);
        ownerScore.setTaskId(taskId);
        ownerScore.setOrderId(orderId);
        return ownerScore;
    }

    private OwnerScorePO toOwnerScore(OwnerUserPO ownerUser, ScoreTaskPO task, Long orderId, boolean savePrompt)
            throws EdsiException {
        OwnerScorePO ownerScorePO = new OwnerScorePO();
        ownerScorePO.setId(systemService.getId());
        ownerScorePO.setOwnerId(ownerUser.getOwnerId());
        ownerScorePO.setOwnerUserId(ownerUser.getOwnerUserId());
        ownerScorePO.setCommunityId(ownerUser.getCommunityId());
        ownerScorePO.setPropertyId(ownerUser.getPropertyId());
        ownerScorePO.setCreateTime(new Date());
        ownerScorePO.setLoseTime(TimeUtil.AddMonth(new Date(), 120));
        ownerScorePO.setScore(task.getTaskScore());
        ownerScorePO.setOpaymentId(0L);
        ownerScorePO.setDesc(task.getTaskDesc());
        ownerScorePO.setOrderId(orderId);
        ownerScorePO.setTaskId(task.getTaskId());
        if (savePrompt) {
            ownerScorePO.setTaskPrompt(task.getTaskPrompt());
        }
        return ownerScorePO;
    }

    @Override
    public List<String> getAndClearTaskPrompt(Long ownerId, Long communityId, List<String> optUris)
            throws EdsiException {
        List<String> taskPrompts = ownerScoreDao.getTaskPrompt(ownerId, communityId, optUris);
        if (CollectionUtils.isNotEmpty(taskPrompts)) {
            ownerScoreDao.clearTaskPrompt(ownerId, communityId, optUris);
        }
        return taskPrompts;
    }

    @Override
    public OwnerScorePO getOwnerScoreByDay(Long ownerId, Long communityId, Long taskId, String day)
            throws EdsiException {
        return ownerScoreDao.getOwnerScoreByDay(ownerId, communityId, taskId, day);
    }

    @Override
    public List<OwnerScorePO> getOwnerScoreByMon(Long ownerId, Long communityId, Long taskId, String mon)
            throws EdsiException {
        return ownerScoreDao.getOwnerScoreByMon(ownerId, communityId, taskId, mon);
    }

    @Override
    public OwnerScorePO addScore(OwnerUserPO ownerUser, ScoreTaskPO scoreTask) throws EdsiException {
        OwnerScorePO ownerScore = toOwnerScore(ownerUser, scoreTask, 0L, false);
        ownerScoreDao.addScore(ownerScore);
        return ownerScore;
    }

    @Override
    public long getActiveValue(Long ownerId, Long communityId) throws EdsiException {
        return ownerScoreDao.getActiveValue(ownerId, communityId);
    }
}
