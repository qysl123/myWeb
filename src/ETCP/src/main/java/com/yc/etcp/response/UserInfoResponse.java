package com.yc.etcp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.yc.edsi.owner.OwnerUserWithAddressPO;

@JsonInclude(Include.NON_NULL)
//public class UserInfoResponse implements IResponseResult {
public class UserInfoResponse {

	public UserInfoResponse() {
		
	}

	@JsonProperty("oId")
	private String id;
	
	@JsonProperty("oNo")
	private String ownerNo;

	@JsonProperty("oName")
	private String ownerUserName;
	
	@JsonProperty("oNick")
	private String nickname;

	@JsonProperty("oImg")
	private String photoUrl;

	@JsonProperty("oAddr")
	private String areaAddress;

	@JsonProperty("oHN")
	private String homeAddress;

	@JsonProperty("oPhone")
	private String phone;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerNo() {
		return ownerNo;
	}

	public void setOwnerNo(String ownerNo) {
		this.ownerNo = ownerNo;
	}

	public String getOwnerUserName() {
		return ownerUserName;
	}

	public void setOwnerUserName(String ownerUserName) {
		this.ownerUserName = ownerUserName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getAreaAddress() {
		return areaAddress;
	}

	public void setAreaAddress(String areaAddress) {
		this.areaAddress = areaAddress;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public UserInfoResponse addUserInfo(OwnerUserWithAddressPO ownerUser) {
		this.id = String.valueOf(ownerUser.getOwnerUserId());
		this.ownerNo = ownerUser.getOwnerUserNo();
		this.ownerUserName = ownerUser.getOwnerUserName();
		this.nickname = ownerUser.getNickname();
		this.photoUrl = ownerUser.getPhotoUrl();
		this.areaAddress = ownerUser.getAreaAddress();
		this.homeAddress = ownerUser.getHomeAddress();
		this.phone = ownerUser.getPhone();
		return this;
	}
}
