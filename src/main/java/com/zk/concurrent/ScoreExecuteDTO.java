package com.zk.concurrent;

import java.util.List;

/**
 * 积分系统执行DTO
 * Created by Ken on 2016/11/11.
 */
public class ScoreExecuteDTO {

    private String eventSid;//事件标识
    private Long param;//执行因素
    private List<Long> accountId;//受影响的账户id
    private Long systemId;//所属系统id

    public String getEventSid() {
        return eventSid;
    }

    public void setEventSid(String eventSid) {
        this.eventSid = eventSid;
    }

    public Long getParam() {
        return param;
    }

    public void setParam(Long param) {
        this.param = param;
    }

    public List<Long> getAccountId() {
        return accountId;
    }

    public void setAccountId(List<Long> accountId) {
        this.accountId = accountId;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }
}
