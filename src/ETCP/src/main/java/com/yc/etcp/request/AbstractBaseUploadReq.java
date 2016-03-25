package com.yc.etcp.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.edsi.order.third.IThirdSellerService.Operation;

abstract public class AbstractBaseUploadReq implements IRequestParam, IUploadOrder {
	private static Logger logger = LoggerFactory.getLogger(AbstractBaseUploadReq.class);

	private final static String MODULE_NAME = "请求实体抽象类";
	
	protected Map<String, Object> beanMap = new HashMap<String, Object>();

	public void insertData(String key, Object value) {
		beanMap.put(key, value);
	}
	
	public void retrieveData(String key) {
		beanMap.get(key);
	}
	
	@Override
	abstract public boolean validateRequestParam(ApplicationContext appContext);
	
	public OrderThirdInfoPO createThirdOrderInfo(ApplicationContext appContext) throws EdsiException {
		logger.debug("{},创建第三方订单记录", MODULE_NAME);
		OrderThirdInfoPO orderThirdInfo;
		try {
			orderThirdInfo = getOrderInfo(appContext);
			Long id = getOrderId(orderThirdInfo, appContext);
			orderThirdInfo.setId(id);
		} catch (EdsiException e) {
			logger.error(MODULE_NAME, e);
			throw new EdsiException(e.getMessage());
		} catch (Exception e) {
			logger.error(MODULE_NAME, e);
			throw new EdsiException("请求参数异常");
		}
			
		return orderThirdInfo;
	}

	abstract protected OrderThirdInfoPO getOrderInfo(ApplicationContext appContext) throws Exception;
	
	abstract public Operation getOptByCondition(OrderThirdInfoPO thirdOrderInfo);

	protected final Long getOrderId(OrderThirdInfoPO thirdOrderInfo, ApplicationContext appContext) throws EdsiException {
        logger.debug("{},获取订单ID", MODULE_NAME);
		OrderThirdInfoPO queryCondtion = new OrderThirdInfoPO();
		queryCondtion.setSellerNo(thirdOrderInfo.getSellerNo());
		queryCondtion.setOrderNo(thirdOrderInfo.getOrderNo());
		IThirdSellerService thirdSellerService = (IThirdSellerService) appContext.getBean(IThirdSellerService.class);
		List<OrderThirdInfoPO> orders = thirdSellerService.findOrdersById(queryCondtion);
		int orderNum = orders.size();
		if (orderNum > 1) {
			throw new EdsiException("存在多条类似订单");
		}
		return orderNum == 0 ? null : orders.get(0).getId();
	}

}
