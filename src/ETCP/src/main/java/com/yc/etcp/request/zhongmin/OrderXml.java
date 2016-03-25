package com.yc.etcp.request.zhongmin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService.Operation;
import com.yc.edsi.owner.IOwnerService;
import com.yc.edsi.owner.OwnerUserPO;
import com.yc.etcp.request.AbstractBaseUploadReq;
import com.yc.etcp.request.IUploadOrder;
import com.yc.etcp.response.zhongmin.OrderOutputXml;

@XmlRootElement(name = "PackageList")
public class OrderXml extends AbstractBaseUploadReq implements IUploadOrder{

	private static Logger logger = LoggerFactory.getLogger(OrderXml.class);

	private final static String MODULE_NAME = "中民保险请求实体类";

	private final static String SELLER_NAME = "中民保险";
	
	List<Package> packages;

	@XmlElement(name ="Package")
	public List<Package> getPackages() {
		return packages;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}
	
	private Header getHeader() {
		Package pack = getPackage();
		if (pack == null) {
			return null;
		}
		return pack.getHeader();
	}

	private Response getResponse() {
		Package pack = getPackage();
		if (pack == null) {
			return null;
		}
		return pack.getResponse();
	}
	
	private Package getPackage() {
		if (this.packages == null) {
			return null;
		}
		return this.packages.get(0);
	}
	
	private PolicyInfo getPolicyInfo() {
		Response response = getResponse();
		if(response == null) {
			return null;
		}
		if(response.getPolicyList() == null || response.getPolicyList().size() == 0) {
			return null;
		}
		return response.getPolicyList().get(0);
	}

	public void copyHeaderTo(com.yc.etcp.response.zhongmin.Header header) {
		logger.debug("{},拷贝header到输出文件", MODULE_NAME);
		Header requestHeader = getHeader();
		if (requestHeader != null) {
			header.setComId(requestHeader.getComId());
			header.setComSerial(requestHeader.getComSerial());
			header.setFromSerial(requestHeader.getFromSerial());
			header.setProductCode(requestHeader.getProductCode());
			header.setProductName(requestHeader.getProductName());
			header.setRequestType(requestHeader.getRequestType());
			header.setSendTime(requestHeader.getSendTime());
			header.setSerialUserId(requestHeader.getSerialUserId());
			header.setUuid(requestHeader.getUuid());
		}
	}

	@Override
	public boolean validateRequestParam(ApplicationContext appContext) {
		//TODO 是否需要判断渠道标识
		return true;
	}

	@Override
	public Operation getOptByCondition(OrderThirdInfoPO thirdOrderInfo) {
        logger.debug("{},获取订单操作类型", MODULE_NAME);
		if (thirdOrderInfo.getId() != null) {
			return Operation.UPDATE;
		}
		return Operation.INSERT;
	}

	@Override
	protected OrderThirdInfoPO getOrderInfo(ApplicationContext appContext)
			throws Exception {
		logger.debug("{},创建第三方订单记录", MODULE_NAME);
		OrderThirdInfoPO orderThirdInfo = new OrderThirdInfoPO();
		try {
			Header header = getHeader();
			if (header != null) {
				Long ownerUserId = Long.valueOf(header.getSerialUserId());
				IOwnerService ownerService = (IOwnerService) appContext.getBean(IOwnerService.class);
				OwnerUserPO ownerUser = ownerService.getOwnerUserByIdOnly(ownerUserId);
				if (ownerUser == null) {
					logger.error(MODULE_NAME, "订单用户不存在");
					throw new EdsiException("订单用户不存在");
				}
				orderThirdInfo.setCommunityId(ownerUser.getCommunityId());
				orderThirdInfo.setPropertyId(ownerUser.getPropertyId());
				orderThirdInfo.setOwnerId(ownerUser.getOwnerId());
				orderThirdInfo.setOwnerUserId(ownerUserId);
				orderThirdInfo.setOwnerUserName(ownerUser.getOwnerUserName());
				orderThirdInfo.setSubcomId(ownerUser.getSubcomId());

				orderThirdInfo.setOrderName(header.getProductName());
				orderThirdInfo.setOrderNo(header.getComSerial());

				String orderState = header.getRequestType();
				orderThirdInfo.setOrderState(getOrderStatus(orderState));
				orderThirdInfo.setOrderStatusDesc(getOrderStatusDesc(orderState));
			}

			orderThirdInfo.setSellerName(SELLER_NAME);
			orderThirdInfo.setShopPhone("400-8822-300");

			Response response = getResponse();
			if (response != null) {
				Order order = response.getOrder();
				if (order != null) {
					Double price = new Double(order.getTotalPrice());
					orderThirdInfo.setPrice(price);
					orderThirdInfo.setActualPayment(new BigDecimal(price));
				}

				PolicyInfo policyInfo = getPolicyInfo();
				if (policyInfo != null) {
					orderThirdInfo.setOrderDetailUrl(policyInfo.getOrderViewUrl());
					orderThirdInfo.setCreateTime(policyInfo.getAccountDate());
				}
			}
			orderThirdInfo.setFreight((double) 0);
		} catch (EdsiException e) {
			logger.error(MODULE_NAME, e);
			throw new EdsiException(e.getMessage());
		} catch (Exception e) {
			logger.error(MODULE_NAME, e);
			throw new EdsiException("请求参数异常");
		}

		return orderThirdInfo;
	}
	
	private Integer getOrderStatus(String orderState) {
		if(orderState == null) {
			return null;
		}
		if(orderState.equals("01")) {
			return 100;
		}
		if(orderState.equals("02")) {
			return 600;
		}
		return Integer.valueOf(orderState);
	}
	
	private String getOrderStatusDesc(String orderState) {
		if(orderState == null) {
			return "订单状态不明";
		}
		if(orderState.equals("01")) {
			return "已核保";
		}
		if(orderState.equals("02")) {
			return "已承保";
		}
		return "订单状态不明";
	}

	@Override
	public Object createSuccessResponse(OrderThirdInfoPO order) {
		return OrderOutputXml.createSuccessByOrderXml(this);
	}

	@Override
	public Object createFailureResponse(String error) {
		return OrderOutputXml.createFailureByOrderXml(this, error);
	}

}

class Package implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8467847153256438580L;
	Header header;
	Response response;
	@XmlElement(name ="Header")
	public Header getHeader() {
		return header;
	}
	public void setHeader(Header header) {
		this.header = header;
	}
	@XmlElement(name ="Response")
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
}

class Header implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4164481705261582656L;
	String requestType;
	String uuid;
	String comId;
	String sendTime;
	String fromSerial;
	String comSerial;
	String productCode;
	String productName;
	String serialUserId;
	Integer responseCode;
	String responseInfo;
	@XmlElement(name ="RequestType")
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	@XmlElement(name ="UUID")
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	@XmlElement(name ="ComId")
	public String getComId() {
		return comId;
	}
	public void setComId(String comId) {
		this.comId = comId;
	}
	@XmlElement(name ="SendTime")
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	@XmlElement(name ="FromSerial")
	public String getFromSerial() {
		return fromSerial;
	}
	public void setFromSerial(String fromSerial) {
		this.fromSerial = fromSerial;
	}
	@XmlElement(name ="ComSerial")
	public String getComSerial() {
		return comSerial;
	}
	public void setComSerial(String comSerial) {
		this.comSerial = comSerial;
	}
	@XmlElement(name ="ProductCode")
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	@XmlElement(name ="ProductName")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@XmlElement(name ="SerialUserId")
	public String getSerialUserId() {
		return serialUserId;
	}
	public void setSerialUserId(String serialUserId) {
		this.serialUserId = serialUserId;
	}
	@XmlElement(name ="ResponseCode")
	public Integer getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	@XmlElement(name ="ResponseInfo")
	public String getResponseInfo() {
		return responseInfo;
	}
	public void setResponseInfo(String responseInfo) {
		this.responseInfo = responseInfo;
	}
}

class Response implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6871828550491884953L;
	private Order order;
	private List<PolicyInfo> policyList;
	@XmlElement(name ="Order")
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
    @XmlElementWrapper(name = "PolicyList")
	@XmlElement(name ="PolicyInfo")
	public List<PolicyInfo> getPolicyList() {
		return policyList;
	}
	public void setPolicyList(List<PolicyInfo> policyList) {
		this.policyList = policyList;
	}
}

class Order implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2558808918757959939L;
	private String orderId;
	private String totalPrice;
	private Long insuranceNum;
	private String groupType;
	@XmlElement(name ="OrderId")
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	@XmlElement(name ="TotalPrice")
	public String getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
	@XmlElement(name ="InsuranceNum")
	public Long getInsuranceNum() {
		return insuranceNum;
	}
	public void setInsuranceNum(Long insuranceNum) {
		this.insuranceNum = insuranceNum;
	}
	@XmlElement(name ="GroupType")
	public String getGroupType() {
		return groupType;
	}
	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}
}

class PolicyInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9220451995956671116L;
	private String proposalNo;
	private String policyNo;
	private String policyPrice;
	private String policyNum;
	private String policyUrl;
	private String orderViewUrl;
	private String policyDate;
	private Boolean isSuccess;
	private String accountDate;
	private List<InsuredInfo> insuredList;
	@XmlElement(name ="ProposalNo")
	public String getProposalNo() {
		return proposalNo;
	}
	public void setProposalNo(String proposalNo) {
		this.proposalNo = proposalNo;
	}
	@XmlElement(name ="PolicyNo")
	public String getPolicyNo() {
		return policyNo;
	}
	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}
	@XmlElement(name ="PolicyPrice")
	public String getPolicyPrice() {
		return policyPrice;
	}
	public void setPolicyPrice(String policyPrice) {
		this.policyPrice = policyPrice;
	}
	@XmlElement(name ="PolicyNum")
	public String getPolicyNum() {
		return policyNum;
	}
	public void setPolicyNum(String policyNum) {
		this.policyNum = policyNum;
	}
	@XmlElement(name ="OrderViewUrl")
	public String getOrderViewUrl() {
		return orderViewUrl;
	}
	public void setOrderViewUrl(String orderViewUrl) {
		this.orderViewUrl = orderViewUrl;
	}
	@XmlElement(name ="IsSuccess")
    public Boolean getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	@XmlElement(name ="PolicyUrl")
	public String getPolicyUrl() {
		return policyUrl;
	}
	public void setPolicyUrl(String policyUrl) {
		this.policyUrl = policyUrl;
	}
	@XmlElement(name ="PolicyDate")
	public String getPolicyDate() {
		return policyDate;
	}
	public void setPolicyDate(String policyDate) {
		this.policyDate = policyDate;
	}
	@XmlElement(name ="AccountDate")
	public String getAccountDate() {
		return accountDate;
	}
	public void setAccountDate(String accountDate) {
		this.accountDate = accountDate;
	}
	@XmlElementWrapper(name = "InsuredList")
	@XmlElement(name ="InsuredInfo")
	public List<InsuredInfo> getInsuredList() {
		return insuredList;
	}
	public void setInsuredList(List<InsuredInfo> insuredList) {
		this.insuredList = insuredList;
	}
}

class InsuredInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1220219637772219221L;
	private Integer insuredNo;
	private String insuredRelation;
	private Long insuredNum;
	private Boolean isSuccess;
	private String failReason;
	@XmlElement(name ="InsuredNo")
	public Integer getInsuredNo() {
		return insuredNo;
	}
	public void setInsuredNo(Integer insuredNo) {
		this.insuredNo = insuredNo;
	}
	@XmlElement(name ="InsuredRelation")
	public String getInsuredRelation() {
		return insuredRelation;
	}
	public void setInsuredRelation(String insuredRelation) {
		this.insuredRelation = insuredRelation;
	}
	@XmlElement(name ="InsuredNum")
	public Long getInsuredNum() {
		return insuredNum;
	}
	public void setInsuredNum(Long insuredNum) {
		this.insuredNum = insuredNum;
	}
	@XmlElement(name ="IsSuccess")
	public Boolean getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	@XmlElement(name ="FailReason")
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
}
