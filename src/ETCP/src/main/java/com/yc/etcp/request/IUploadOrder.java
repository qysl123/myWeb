package com.yc.etcp.request;

import org.springframework.context.ApplicationContext;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService.Operation;

public interface IUploadOrder {
	OrderThirdInfoPO createThirdOrderInfo(ApplicationContext appContext) throws EdsiException;
	
	Operation getOptByCondition(OrderThirdInfoPO thirdOrderInfo);
	
	Object createSuccessResponse(OrderThirdInfoPO order);
	
	Object createFailureResponse(String error);
}
