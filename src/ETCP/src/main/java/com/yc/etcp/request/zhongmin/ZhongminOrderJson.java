package com.yc.etcp.request.zhongmin;

import com.yc.commons.Tools;
import com.yc.edsi.order.OrderThirdInfoPO;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.etcp.request.AbstractBaseUploadReq;
import com.yc.etcp.request.IUploadOrder;
import com.yc.etcp.services.UploadReqHandler;
import jodd.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.Serializable;
import java.io.StringReader;

public class ZhongminOrderJson  extends AbstractBaseUploadReq implements IUploadOrder, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 6693369118798275812L;

    private String comid;

    private String sign;

    private String data;

    private OrderXml xmlBody;

    private static final String KEY = "zmtest@good$";

    @Override
    public boolean validateRequestParam(ApplicationContext appContext) {
        return !StringUtils.isBlank(sign) && Tools.getMd5UpperString(data + KEY).equals(sign.toUpperCase());
    }

    @Override
    protected OrderThirdInfoPO getOrderInfo(ApplicationContext appContext) throws Exception {
        return xmlBody.getOrderInfo(appContext);
    }

    @Override
    public IThirdSellerService.Operation getOptByCondition(OrderThirdInfoPO thirdOrderInfo) {
        return xmlBody.getOptByCondition(thirdOrderInfo);
    }

    @Override
    public Object createSuccessResponse(OrderThirdInfoPO order) {
        return xmlBody.createSuccessResponse(order);
    }

    @Override
    public Object createFailureResponse(String error) {
        return xmlBody.createFailureResponse(error);
    }

    private OrderXml parseData2OrderXml(String data) throws JAXBException {
        String xmlStr = Base64.decodeToString(data);
        return (OrderXml)UploadReqHandler.getUnmarshallerInstance().unmarshal(new StreamSource(new StringReader(xmlStr)));
    }

    public String getComid() {
        return comid;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) throws JAXBException {
        this.data = data;
        this.xmlBody = parseData2OrderXml(data);
    }

    public OrderXml getXmlBody() {
        return xmlBody;
    }

    public void setXmlBody(OrderXml xmlBody) {
        this.xmlBody = xmlBody;
    }
}
