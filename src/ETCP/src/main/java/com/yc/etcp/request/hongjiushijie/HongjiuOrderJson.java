package com.yc.etcp.request.hongjiushijie;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.yc.etcp.request.AbstractBaseJsonUploadReq;

/**
 * 订单上传实体类
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月28日
 *
 */
public class HongjiuOrderJson extends AbstractBaseJsonUploadReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1916775578987751079L;

	private static Logger logger = LoggerFactory.getLogger(HongjiuOrderJson.class);
	
	private final static String MODULE_NAME = "红酒世界请求实体类";

	@Override
	public boolean validateRequestParam(ApplicationContext appContext) {
		// TODO 此处判断第三方编码是否合法
		logger.debug("{},请求参数验证", MODULE_NAME);
		return true;
	}

}
