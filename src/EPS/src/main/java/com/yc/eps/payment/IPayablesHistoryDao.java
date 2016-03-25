package com.yc.eps.payment;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yc.edsi.payment.OwnerPaymentStatisticsPO;
import com.yc.edsi.payment.PayablesHistoryPO;

/**
 * 
 * 扎帐历史记录
 * 
 * @author <a href="mailto:wangx@yichenghome.com">Wang x</a>
 * @version 1.0
 * @since 2014年4月25日
 */
public interface IPayablesHistoryDao {

    void insertPayablesHistory(PayablesHistoryPO historyPO);

    List<PayablesHistoryPO> getPayablesHistory(PayablesHistoryPO historyPO);

    String getLastPayableTime(PayablesHistoryPO historyPO);

    List<OwnerPaymentStatisticsPO> getHistoryStatisticsDetail(@Param("listId")Long listId, @Param("communityId")Long communityId);
    
    void confirmPayable(PayablesHistoryPO historyPO);
    
    /**
     * 通过listId获取扎帐申请
     * @param listId
     * @return
     */
	PayablesHistoryPO getPayablesHistoryById(@Param("listId")Long listId);

	/**
	 * 通过listId删除扎帐申请
	 * @param listId
	 */
	void deletetPayablesHistoryById(@Param("listId")Long listId);
    
	/**
	 * 获取传入物业Id和小区Id最后一次扎帐的记录信息
	 * @param phs
	 * @return
	 */
	PayablesHistoryPO getLastPayable(@Param("phs")PayablesHistoryPO phs);
}
