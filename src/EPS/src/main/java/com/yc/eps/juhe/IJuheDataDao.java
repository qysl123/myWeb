package com.yc.eps.juhe;

import java.util.List;
import java.util.Map;

import com.yc.edsi.juhe.JuheDataPO;
import com.yc.edsi.juhe.JuheSettlementPO;

public interface IJuheDataDao {

    void insertJuhePay(JuheDataPO juheDataPO);

    List<JuheDataPO> getJuhePayList(Map<String, Object> sqlMap);

    int getJuhePayListCount(Map<String, Object> sqlMap);

    void updateJuhePayStatus(JuheDataPO juheDataPO);

    void updateJuhePaySettleStatus(Map<String, Object> sqlMap);

    void settlement(JuheSettlementPO juheSettlementPO);

    List<JuheSettlementPO> getSettlementList(Map<String, Object> sqlMap);

    int getSettlementListCount(Map<String, Object> sqlMap);

    List<JuheSettlementPO> getNotSettlementList(Map<String, Object> sqlMap);

    int getNotSettlementListCount(Map<String, Object> sqlMap);

    void changeSettlementStatus(Map<String, Object> sqlMap);
}
