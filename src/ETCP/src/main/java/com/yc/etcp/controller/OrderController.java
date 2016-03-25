package com.yc.etcp.controller;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.edsi.order.third.IThirdSellerService.Operation;
import com.yc.etcp.common.Constants;
import com.yc.etcp.request.AbstractBaseUploadReq;
import com.yc.etcp.services.UploadReqHandler;
import com.yc.mq.util.MQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单对外接口控制类
 *
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月28日
 */

@Controller
public class OrderController {
    @Resource
    private IThirdSellerService thirdSellerService;

    @Resource
    private UploadReqHandler uploadReqHandler;

    @Autowired
    private ApplicationContext appContext;

    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final static String MODULE_NAME = "订单对外接口控制类";

    @ModelAttribute("bean")
    public AbstractBaseUploadReq getUploadReq(HttpServletRequest request) throws Exception {
        AbstractBaseUploadReq uploadReq = uploadReqHandler.getUploadBean(request);
        request.setAttribute(Constants.BEAN_ATTRIBUTE, uploadReq);
        return uploadReq;
    }

    @RequestMapping(value = "/sysOrder.do", method = RequestMethod.POST)
    @ResponseBody
    public Object uploadJson(@ModelAttribute("bean") AbstractBaseUploadReq request) {
        logger.debug("{},JSON格式订单信息获取", MODULE_NAME);
        try {
            OrderThirdInfoPO updateOrderThirdInfo = request.createThirdOrderInfo(appContext);
            Operation opt = request.getOptByCondition(updateOrderThirdInfo);
			updateOrderThirdInfo = thirdSellerService.uploadThirdOrder(updateOrderThirdInfo, opt);
            return request.createSuccessResponse(updateOrderThirdInfo);
        } catch (EdsiException e) {
            logger.error(MODULE_NAME, e);
            return request.createFailureResponse(e.getMessage());
        } catch (Exception e) {
            logger.error(MODULE_NAME, e);
            return request.createFailureResponse("未知异常");
        }
    }

    @RequestMapping(value = "/sysOrder.do", method = RequestMethod.POST)
    @ResponseBody
    public void uploadOrder(@ModelAttribute("bean") AbstractBaseUploadReq request) {
        logger.debug("{},JSON格式订单信息获取", MODULE_NAME);
        try {
            MQUtil.sendMsg("HJSJ", request.toString());
        } catch (EdsiException e) {
            logger.error(MODULE_NAME, e);
//            return request.createFailureResponse(e.getMessage());
        } catch (Exception e) {
            logger.error(MODULE_NAME, e);
//            return request.createFailureResponse("未知异常");
        }
    }
}
