package com.yc.eps.weixin;

import com.yc.edsi.payment.third.WeixinPayPO;

public interface IWeixinPayDao {

    void insertTrade(WeixinPayPO weixinPayPO);

    int getFinishTrade(String outTradeNo);
}
