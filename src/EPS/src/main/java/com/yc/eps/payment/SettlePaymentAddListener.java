/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import javax.annotation.Resource;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.yc.edsi.payment.SellerAccountPO;
import com.yc.edsi.payment.SellerPaymentPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2016年1月11日
 */
@Component
public class SettlePaymentAddListener implements ApplicationListener<SellerPaymentAddEvent> {
    
    @Resource
    private ISellerAccountDao sellerAccountDao;

    @Async
    @Override
    public void onApplicationEvent(final SellerPaymentAddEvent event) {
        SellerPaymentPO sellerPayment = (SellerPaymentPO) event.getSource();

        if (sellerPayment != null) {
            SellerAccountPO sellerAccount = sellerAccountDao.getBySpaymentId(sellerPayment.getsPaymentId());
            sellerAccountDao.insert(sellerAccount);
        }
    }

}
