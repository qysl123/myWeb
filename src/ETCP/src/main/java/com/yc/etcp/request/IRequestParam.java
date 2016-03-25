package com.yc.etcp.request;

import org.springframework.context.ApplicationContext;

/**
 * 请求数据接口
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月26日
 *
 */
public interface IRequestParam {

	boolean validateRequestParam(ApplicationContext appContext);
}
