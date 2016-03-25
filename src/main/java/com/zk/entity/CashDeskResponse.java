package com.zk.entity;

import java.io.Serializable;

public class CashDeskResponse implements Serializable{

    private String widgetPageUrl;
    private String payTransCode;

    public String getWidgetPageUrl() {
        return widgetPageUrl;
    }

    public void setWidgetPageUrl(String widgetPageUrl) {
        this.widgetPageUrl = widgetPageUrl;
    }

    public String getPayTransCode() {
        return payTransCode;
    }

    public void setPayTransCode(String payTransCode) {
        this.payTransCode = payTransCode;
    }
}
