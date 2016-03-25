package com.yc.eps.payment;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.IPayablesHistoryService;
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
@Service
public class PayablesHistoryService implements IPayablesHistoryService {
    @Resource
    private IPayablesHistoryDao payablesHistoryDao;

    public void insertPayablesHistory(PayablesHistoryPO historyPO) {
        payablesHistoryDao.insertPayablesHistory(historyPO);

    }

    public List<PayablesHistoryPO> getPayablesHistory(PayablesHistoryPO historyPO) {
        return payablesHistoryDao.getPayablesHistory(historyPO);
    }

    public String getLastPayableTime(long communityId, long propertyId) {
        PayablesHistoryPO historyPO = new PayablesHistoryPO();
        historyPO.setCommunityId(communityId);
        historyPO.setPropertyId(propertyId);
        return payablesHistoryDao.getLastPayableTime(historyPO);
    }

    public List<OwnerPaymentStatisticsPO> getHistoryStatisticsDetail(Long listId, Long communityId) {
        return payablesHistoryDao.getHistoryStatisticsDetail(listId, communityId);
    }

    public void confirmPayable(PayablesHistoryPO historyPO) {
        payablesHistoryDao.confirmPayable(historyPO);
    }

	@Override
	public int cancelPayable(Long listId) throws EdsiException {
		PayablesHistoryPO ph = payablesHistoryDao.getPayablesHistoryById(listId);
		if(ph != null){
			if(ph.getPayableStatus()==1){
				PayablesHistoryPO phs = new PayablesHistoryPO();
				phs.setCommunityId(ph.getCommunityId());
				phs.setPropertyId(ph.getPropertyId());
				PayablesHistoryPO lastPay = payablesHistoryDao.getLastPayable(phs);
				if(lastPay != null && String.valueOf(lastPay.getListId()).equals(String.valueOf(ph.getListId()))){
					//修改
					payablesHistoryDao.deletetPayablesHistoryById(listId);
					return 1;
				} else {
					return -1;
				}
			} else {//已扎帐无法撤销
				return -3;
			}
		}
		return -2;
	}

}
