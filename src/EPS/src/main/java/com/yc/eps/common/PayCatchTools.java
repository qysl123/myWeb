package com.yc.eps.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yc.edsi.payment.third.IPayCatchService;
import com.yc.edsi.payment.third.RechargePayPO;

@Service
public class PayCatchTools implements IPayCatchService {
    private static Map<String, RechargePayPO> PAY_CATCH = new HashMap<String, RechargePayPO>();

    public void put(String key, RechargePayPO value) {
        PayCatchTools.putRPP(key, value);
    }

    public RechargePayPO get(String key) {
        return PAY_CATCH.get(key);
    }

    public static void putRPP(String key, RechargePayPO value) {
        synchronized (PAY_CATCH) {
            if (PAY_CATCH.size() > 1000) {
                cleanOldCatch();
            }
            PAY_CATCH.put(key, value);
        }
    }

    public static RechargePayPO getRPP(String key) {
        return PAY_CATCH.get(key);
    }

    private static long CATCH_TIME = 1000 * 60 * 60 * 10;

    private static void cleanOldCatch() {
        long lastTime = System.currentTimeMillis() - CATCH_TIME;
        Iterator<String> iterator = PAY_CATCH.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (PAY_CATCH.get(key).getLastUpdateTime() < lastTime) {
                iterator.remove();
                PAY_CATCH.remove(key);
            }
        }

    }
}
