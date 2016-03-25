/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import org.springframework.context.ApplicationEvent;

import com.yc.edsi.payment.SellerPaymentPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2016年1月11日
 */
public class SellerPaymentAddEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public SellerPaymentAddEvent(SellerPaymentPO sellerPayment) {
        super(sellerPayment);
    }

}
