package com.yc.etcp.response.zhongmin;

import javax.xml.bind.annotation.XmlElement;

import com.yc.etcp.request.zhongmin.OrderXml;

public class Header {
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
	
	public void copyRequestHeader(OrderXml request) {
		request.copyHeaderTo(this);
	}
	
	public String getRequestType() {
		return requestType;
	}
	@XmlElement(name ="RequestType")
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getUuid() {
		return uuid;
	}
	@XmlElement(name ="UUID")
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getComId() {
		return comId;
	}
	@XmlElement(name ="ComId")
	public void setComId(String comId) {
		this.comId = comId;
	}
	public String getSendTime() {
		return sendTime;
	}
	@XmlElement(name ="SendTime")
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	public String getFromSerial() {
		return fromSerial;
	}
	@XmlElement(name ="FromSerial")
	public void setFromSerial(String fromSerial) {
		this.fromSerial = fromSerial;
	}
	public String getComSerial() {
		return comSerial;
	}
	@XmlElement(name ="ComSerial")
	public void setComSerial(String comSerial) {
		this.comSerial = comSerial;
	}
	public String getProductCode() {
		return productCode;
	}
	@XmlElement(name ="ProductCode")
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductName() {
		return productName;
	}
	@XmlElement(name ="ProductName")
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getSerialUserId() {
		return serialUserId;
	}
	@XmlElement(name ="SerialUserId")
	public void setSerialUserId(String serialUserId) {
		this.serialUserId = serialUserId;
	}
	public Integer getResponseCode() {
		return responseCode;
	}
	@XmlElement(name ="ResponseCode")
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseInfo() {
		return responseInfo;
	}
	@XmlElement(name ="ResponseInfo")
	public void setResponseInfo(String responseInfo) {
		this.responseInfo = responseInfo;
	}
}