package com.yc.etcp.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yc.commons.Tools;
import com.yc.edsi.commons.ITokenRecordsService;
import com.yc.edsi.seller.ISellerService;
import com.yc.edsi.seller.SellerPO;

/**
 * 认证信息实体类
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月25日
 *
 */
public class Auth implements IRequestParam {
    private static Logger logger = LoggerFactory.getLogger(Auth.class);

	@JsonProperty("ti")
	private String timeStamp;
	
	@JsonProperty("tc")
	private String sellerNo;
	
	@JsonProperty("ts")
	private String sign;
	
	@JsonProperty("param")
	private String token;

	public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSellerNo() {
        return sellerNo;
    }

    public void setSellerNo(String sellerNo) {
        this.sellerNo = sellerNo;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
	public final boolean validateRequestParam(ApplicationContext appContext) {
	    if (!validateRequest(appContext)) {
	        return false;
	    }
		if (!validateToken(appContext)) {
			return false;
		}
		return true;
	}
	
	private boolean validateRequest(ApplicationContext appContext) {
	    try {
	        ISellerService sellerService = (ISellerService) appContext.getBean("edsSellerService");
	        SellerPO seller = sellerService.getBaseSellerByNo(sellerNo);
            String encryptKey = "";
	        if (seller != null) {
	            encryptKey = seller.getSecretKey();
	        }
	        String caledSign = Tools.encodeBase64AES(timeStamp + sellerNo, encryptKey);
	        if (!caledSign.equals(sign)) {
	            return false;
	        }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected final boolean validateToken(ApplicationContext appContext) {
		if (token == null || token.trim().equals("")) {
			logger.debug("Client request token is null");
			return false;
		}
		ITokenRecordsService tokenRecordsService = appContext.getBean(ITokenRecordsService.class);
		if (!tokenRecordsService.isTokenValid(token)) {
			logger.debug("Client request token is invalid");
			return false;
		}
		return true;
	}
}
