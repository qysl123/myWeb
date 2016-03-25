package com.yc.etcp.request;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yc.commons.Tools;
import com.yc.edsi.seller.ISellerService;
import com.yc.edsi.seller.SellerPO;

/**
 * 订单上传实体类
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月28日
 *
 */
public class OrderJson extends AbstractBaseJsonUploadReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1916775578987751079L;

	private static Logger logger = LoggerFactory.getLogger(OrderJson.class);

	private final static String MODULE_NAME = "第三方订单请求实体类";
	
	@JsonProperty("ti")
	private String timeStamp;
	
	@JsonProperty("ts")
	private String sign;
	
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public boolean validateRequestParam(ApplicationContext appContext) {
		logger.debug("{},请求参数验证", MODULE_NAME);
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

}
