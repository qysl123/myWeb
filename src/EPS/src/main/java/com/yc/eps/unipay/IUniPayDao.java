package com.yc.eps.unipay;

import com.yc.edsi.payment.third.UniPayPO;

public interface IUniPayDao {

    void insertUniTrade(UniPayPO uniPayPO);

    int getUniFinishTrade(String outTradeNo);
}
