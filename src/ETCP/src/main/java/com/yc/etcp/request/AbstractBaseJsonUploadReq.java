package com.yc.etcp.request;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService.Operation;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.etcp.response.Response;

public class AbstractBaseJsonUploadReq extends AbstractBaseUploadReq {
	private static Logger logger = LoggerFactory.getLogger(AbstractBaseJsonUploadReq.class);
	
	private final static String MODULE_NAME = "请求Json实体抽象类";
	
	@JsonProperty("tc")
	protected String sellerNo;

	@JsonProperty("param")
	protected OrderRecord orderRecord;

	public String getSellerNo() {
		return sellerNo;
	}

	public void setSellerNo(String sellerNo) {
		this.sellerNo = sellerNo;
	}

	public OrderRecord getOrderRecord() {
		return orderRecord;
	}

	public void setOrderRecord(OrderRecord orderRecord) {
		this.orderRecord = orderRecord;
	}

	@Override
	public boolean validateRequestParam(ApplicationContext appContext){return true;}

	@Override
	protected OrderThirdInfoPO getOrderInfo(ApplicationContext appContext) throws Exception {
		OrderThirdInfoPO orderThirdInfo = new OrderThirdInfoPO();
		Long ownerUserId = orderRecord.getOwnerUserId();
		IOwnerService ownerService = (IOwnerService) appContext.getBean(IOwnerService.class);
		OwnerUserPO ownerUser = ownerService.getOwnerUserByIdOnly(ownerUserId);
		if (ownerUser == null) {
			logger.error(MODULE_NAME, "订单用户不存在");
			throw new EdsiException("订单用户不存在");
		}
		orderThirdInfo.setOwnerId(ownerUser.getOwnerId());
		orderThirdInfo.setCommunityId(ownerUser.getCommunityId());
		orderThirdInfo.setPropertyId(ownerUser.getPropertyId());
		orderThirdInfo.setOwnerUserId(ownerUserId);
		orderThirdInfo.setSubcomId(ownerUser.getSubcomId());

		if (orderRecord == null) {
			logger.error(MODULE_NAME, "订单不存在");
			throw new EdsiException("订单不存在");
		}
		orderThirdInfo.setSellerName(orderRecord.getSellerName());
		orderThirdInfo.setSellerNo(getSellerNo());

		orderThirdInfo.setPrice(orderRecord.getPrice());
		orderThirdInfo.setActualPayment(new BigDecimal(orderRecord.getActualPrice()));
		orderThirdInfo.setFreight(orderRecord.getFreight());
		orderThirdInfo.setShopPhone(orderRecord.getSellerPhone());

		orderThirdInfo.setOwnerUserName(orderRecord.getOwnerName());
		orderThirdInfo.setOwnerPhone(orderRecord.getOwnerPhone());

		orderThirdInfo.setOrderState(Integer.valueOf(orderRecord.getOrderStatusNo()));
		orderThirdInfo.setOrderStatusDesc(orderRecord.getOrderStatus());
		orderThirdInfo.setOrderName(orderRecord.getOrderName());
		orderThirdInfo.setOrderNo(orderRecord.getOrderNo());
		orderThirdInfo.setOrderImg(orderRecord.getOrderImg());
		orderThirdInfo.setOrderAddr(orderRecord.getOrderAddr());
		orderThirdInfo.setOrderDetailUrl(orderRecord.getOrderUrl());
		orderThirdInfo.setCreateTime(orderRecord.getBuyTime());

		return orderThirdInfo;
	}
	
	@Override
	public Operation getOptByCondition(OrderThirdInfoPO thirdOrderInfo) {
		logger.debug("{},获取操作类型", MODULE_NAME);
		if(orderRecord == null) {
			return Operation.UNKOWN;
		}
		
		String opt = orderRecord.getOperation();
		if(opt == null) {
			return Operation.UNKOWN;
		}
		
		if(opt.equals("1")) {
			return Operation.INSERT;
		}
		if(opt.equals("2")) {
			return Operation.UPDATE;
		}
		if(opt.equals("3")) {
			return Operation.DELETE;
		}
		return Operation.UNKOWN;
	}

	@Override
	public Object createSuccessResponse(OrderThirdInfoPO order) {
		return Response.createSuccessResponse(order.getId());
	}

	@Override
	public Object createFailureResponse(String error) {
		return Response.createFailureResponse(error);
	}	
}
