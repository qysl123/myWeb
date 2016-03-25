package com.yc.eps.trustpay;

import java.io.Serializable;

public class TrustPayBaseResponse implements Serializable{

    private String code = CODE_SUCCESS;

    public static final String CODE_SUCCESS = "1";
    public static final String CODE_ERROR_PARAM = "83002";
    public static final String CODE_ERROR_SIGNATURE = "83018";

    private String message;
    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
