package com.yc.eps.alipay;

import com.yc.edsi.payment.third.AlipayRSAPO;

public interface IAlipayDao {

    void insertTrade(AlipayRSAPO alipayRSAPO);

    int getFinshTrade(Long outTradeNo);
}
