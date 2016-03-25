package com.yc.etcp.request.hongjiushijie;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.model.Message;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.etcp.request.AbstractBaseUploadReq;
import com.yc.etcp.request.BaseProcessHandler;
import com.yc.etcp.services.UploadReqHandler;
import com.yc.mq.IMsgHandler;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class HongjiuOrderProcessHandler extends BaseProcessHandler implements IMsgHandler {

    public HongjiuOrderProcessHandler(IThirdSellerService thirdSellerService, ApplicationContext appContext, UploadReqHandler uploadReqHandler) {
        this.thirdSellerService = thirdSellerService;
        this.appContext = appContext;
        this.uploadReqHandler = uploadReqHandler;
    }

    @Override
    public void doThings(CloudQueue cloudQueue, List<Message> list) {
        for (Message message : list){
            AbstractBaseUploadReq request = uploadReqHandler.getUploadBean(message.getMessageBodyAsString());
            OrderThirdInfoPO updateOrderThirdInfo = request.createThirdOrderInfo(appContext);
            IThirdSellerService.Operation opt = request.getOptByCondition(updateOrderThirdInfo);
           thirdSellerService.uploadThirdOrder(updateOrderThirdInfo, opt);
            message.getMessageBodyAsString();
        }
    }

    @Override
    public void error(String s, Exception e) {

    }
}
