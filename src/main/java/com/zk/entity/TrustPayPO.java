package com.zk.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class TrustPayPO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long phone;
    private String outTradeNo;
    private BigDecimal amount;
    private Integer paySource;

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPaySource() {
        return paySource;
    }

    public void setPaySource(Integer paySource) {
        this.paySource = paySource;
    }
}
