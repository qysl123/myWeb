package com.yc.etcp.request;

import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.etcp.services.UploadReqHandler;
import org.springframework.context.ApplicationContext;

public class BaseProcessHandler {
    protected IThirdSellerService thirdSellerService;

    protected ApplicationContext appContext;

    protected UploadReqHandler uploadReqHandler;
}
