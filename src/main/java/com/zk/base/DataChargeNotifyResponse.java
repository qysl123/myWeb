package com.zk.base;

public class DataChargeNotifyResponse {

    private String intecmd = "A_TZCZ";
    private String apikey;
    private String tradeerror;
    private String orderid;
    private String pdtvalue;
    private String pdtactvalue;
    private String Package;

    private String tradestatus;
    public static String TRADESTATUS_SUCCESS = "1";
    public static String TRADESTATUS_ERROR = "0";

    public String getIntecmd() {
        return intecmd;
    }

    public void setIntecmd(String intecmd) {
        this.intecmd = intecmd;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getTradestatus() {
        return tradestatus;
    }

    public void setTradestatus(String tradestatus) {
        this.tradestatus = tradestatus;
    }

    public String getTradeerror() {
        return tradeerror;
    }

    public void setTradeerror(String tradeerror) {
        this.tradeerror = tradeerror;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getPdtvalue() {
        return pdtvalue;
    }

    public void setPdtvalue(String pdtvalue) {
        this.pdtvalue = pdtvalue;
    }

    public String getPdtactvalue() {
        return pdtactvalue;
    }

    public void setPdtactvalue(String pdtactvalue) {
        this.pdtactvalue = pdtactvalue;
    }

    public String getPackage() {
        return Package;
    }

    public void setPackage(String aPackage) {
        Package = aPackage;
    }

    @Override
    public String toString() {
        return "DataChargeNotifyResponse{" +
                "intecmd='" + intecmd + '\'' +
                ", apikey='" + apikey + '\'' +
                ", tradeerror='" + tradeerror + '\'' +
                ", orderid='" + orderid + '\'' +
                ", pdtvalue='" + pdtvalue + '\'' +
                ", pdtactvalue='" + pdtactvalue + '\'' +
                ", Package='" + Package + '\'' +
                ", tradestatus='" + tradestatus + '\'' +
                '}';
    }
}
