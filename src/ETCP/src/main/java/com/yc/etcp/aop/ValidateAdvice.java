package com.yc.etcp.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.yc.etcp.request.IRequestParam;
import com.yc.etcp.response.Response;


/**
 * 请求参数验证切面
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月26日
 *
 */
@Aspect
@Order(1)
@Service
public class ValidateAdvice {
	private final static Logger logger = LoggerFactory.getLogger(ValidateAdvice.class);
	
	private final static String MODULE_NAME = "验证切面类";

    @Autowired
    private ApplicationContext appContext;
    
    @Pointcut("execution(* com.yc.etcp.controller..*.*(com.yc.etcp.request.AbstractBaseUploadReq+ || com.yc.etcp.request.IRequestParam+,..)) && args(request,..)")
    public void validate(IRequestParam request) {
    }

    @Around(value = "validate(request)")
    public Object doValidate(ProceedingJoinPoint thisJoinPoint, IRequestParam request) throws Throwable {
		logger.debug("{},验证请求信息", MODULE_NAME);
		try {
			if (!request.validateRequestParam(appContext)) {
				return Response.createFailureResponse();
			}
		} catch (Exception e) {
			logger.error(MODULE_NAME, e.getMessage());
			return Response.createFailureResponse("请求参数验证失败");
		}
    	return thisJoinPoint.proceed(thisJoinPoint.getArgs());
    }    
}
