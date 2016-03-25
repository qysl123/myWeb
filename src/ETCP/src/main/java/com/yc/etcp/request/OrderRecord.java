package com.yc.etcp.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderRecord implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1582308245270265051L;
	@JsonProperty("opt")
	private String operation;
	@JsonProperty("oId")
	private Long ownerUserId;
	@JsonProperty("orderStatusNo")
	private String orderStatusNo;
	@JsonProperty("orderStatus")
	private String orderStatus;
	@JsonProperty("price")
	private Double price;
	@JsonProperty("actualPrice")
	private Double actualPrice;
	@JsonProperty("freight")
	private Double freight;
	@JsonProperty("sellerName")
	private String sellerName;
	@JsonProperty("sellerPhone")
	private String sellerPhone;
	@JsonProperty("orderName")
	private String orderName;
	@JsonProperty("orderNo")
	private String orderNo;
	@JsonProperty("orderImg")
	private String orderImg;
	@JsonProperty("orderAddr")
	private String orderAddr;
	@JsonProperty("ownerName")
	private String ownerName;
	@JsonProperty("ownerPhone")
	private String ownerPhone;
	@JsonProperty("buyTime")
	private String buyTime;
	@JsonProperty("orderUrl")
	private String orderUrl;
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public Long getOwnerUserId() {
		return ownerUserId;
	}
	public void setOwnerUserId(Long ownerUserId) {
		this.ownerUserId = ownerUserId;
	}
	public String getOrderStatusNo() {
		return orderStatusNo;
	}
	public void setOrderStatusNo(String orderStatusNo) {
		this.orderStatusNo = orderStatusNo;
	}
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getActualPrice() {
		return actualPrice;
	}
	public void setActualPrice(Double actualPrice) {
		this.actualPrice = actualPrice;
	}
	public Double getFreight() {
		return freight;
	}
	public void setFreight(Double freight) {
		this.freight = freight;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getSellerPhone() {
		return sellerPhone;
	}
	public void setSellerPhone(String sellerPhone) {
		this.sellerPhone = sellerPhone;
	}
	public String getOrderName() {
		return orderName;
	}
	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getOrderImg() {
		return orderImg;
	}
	public void setOrderImg(String orderImg) {
		this.orderImg = orderImg;
	}
	public String getOrderAddr() {
		return orderAddr;
	}
	public void setOrderAddr(String orderAddr) {
		this.orderAddr = orderAddr;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getOwnerPhone() {
		return ownerPhone;
	}
	public void setOwnerPhone(String ownerPhone) {
		this.ownerPhone = ownerPhone;
	}
	public String getBuyTime() {
		return buyTime;
	}
	public void setBuyTime(String buyTime) {
		this.buyTime = buyTime;
	}
	public String getOrderUrl() {
		return orderUrl;
	}
	public void setOrderUrl(String orderUrl) {
		this.orderUrl = orderUrl;
	}

}
