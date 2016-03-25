package com.zk.base;

import java.io.Serializable;
import java.util.Date;

public class TeleCardsPO implements Serializable {
    private Integer id;

	private String cardNo;

	private String passwd;

	private Long amount;

	private Long useFlag;
	
	private Long oldThreadId;

	private Date createTime;

	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Long getUseFlag() {
		return useFlag;
	}

	public void setUseFlag(Long useFlag) {
		this.useFlag = useFlag;
	}

	public Long getOldThreadId() {
		return oldThreadId;
	}

	public void setOldThreadId(Long oldThreadId) {
		this.oldThreadId = oldThreadId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2545760886299754470L;

	public TeleCardsPO(Long amount) {
		this.amount = amount;
	}
}