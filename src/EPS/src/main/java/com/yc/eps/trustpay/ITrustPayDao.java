package com.yc.eps.trustpay;

import com.yc.edsi.payment.third.TrustPayPO;

public interface ITrustPayDao {

    void insertTrustPayTrade(TrustPayPO trustPayPO);

    int getTradeCountByLoginName(String loginName);

    int getTrustFinishTrade(String outTradeNo);
}
