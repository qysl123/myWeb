package com.yc.etcp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Response {
    private static final int SUCCESS = 0;

    private static final int FAIL = 1;

    @JsonProperty("r")
    private int result;

    @JsonProperty("m")
    private String reason;
    
    @JsonProperty("result")
    private Object responseResult;

	private Response(int result, String reason) {
		this.result = result;
		this.reason = reason;
	}
	
	private Response(int result, Object responseResult) {
		this.result = result;
		this.responseResult = responseResult;
	}

	public static Response createFailureResponse() {
		return new Response(FAIL, "非法的请求参数");
	}
	
	public static Response createFailureResponse(String errMsg) {
		return new Response(FAIL, errMsg);
	}

	public static Response createSuccessResponse(Object responseResult) {
		return new Response(SUCCESS, responseResult);
	}
	
	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Object getResponseResult() {
		return responseResult;
	}

	public void setResponseResult(Object responseResult) {
		this.responseResult = responseResult;
	}

}
