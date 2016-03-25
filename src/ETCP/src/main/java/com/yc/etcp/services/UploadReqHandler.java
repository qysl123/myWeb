package com.yc.etcp.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.edsi.order.third.SellerEtcpGetewayPO;
import com.yc.etcp.common.HttpHelper;
import com.yc.etcp.controller.OrderController;
import com.yc.etcp.request.AbstractBaseUploadReq;
import com.yc.etcp.request.zhongmin.OrderXml;
import com.yc.etcp.request.zhongmin.ZhongminOrderJson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * 上传请求处理类
 *
 * @author Diao Lei
 * @version 1.0
 * @since 2016年01月14日
 */
@Service
public class UploadReqHandler {

    private final static Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final static String MODULE_NAME = "上传请求处理类";

    private static Unmarshaller u;

    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(OrderXml.class);
            u = jc.createUnmarshaller();
        } catch (JAXBException e) {
            logger.error(MODULE_NAME, e);
        }
    }

    @Resource
    private IThirdSellerService thirdSellerService;

    public static Unmarshaller getUnmarshallerInstance() {
        return u;
    }

    public AbstractBaseUploadReq getUploadBean(HttpServletRequest request) throws Exception {
        logger.debug("{},获取上传实体类", MODULE_NAME);
        String requestBody = HttpHelper.getBodyString(request);
        logger.debug("{},上传参数:" + requestBody, MODULE_NAME);
        try {
            return getJsonBean(requestBody);
        } catch (Exception e) {
            return getXmlBean(requestBody);
        }
    }

    public AbstractBaseUploadReq getUploadBean(String requestBody){
        logger.debug("{},上传参数:" + requestBody, MODULE_NAME);
        try {
            return getJsonBean(requestBody);
        } catch (Exception e) {
            return getXmlBean(requestBody);
        }
    }

    private AbstractBaseUploadReq getJsonBean(String requestBody) throws Exception {
        logger.debug("{},获取上传JSON数据的实体类", MODULE_NAME);
        ObjectMapper mapper = new ObjectMapper();
        //获取商家编码
        JsonNode baseUploadReq = mapper.readTree(requestBody);
        JsonNode sellerNoNode = baseUploadReq.get("tc");
        if (sellerNoNode == null) {
            return getDataXmlBean(requestBody);
        }
        String sellerNo = sellerNoNode.asText();
        if (StringUtils.isBlank(sellerNo)) {
            throw new Exception("商家编码不存在");
        }

        //获取对应的实体类
        SellerEtcpGetewayPO sellerEtcpGeteway = thirdSellerService.findGetewayInfo(sellerNo);
        Class<?> clz = Class.forName(sellerEtcpGeteway.getClzName());
        return (AbstractBaseUploadReq) mapper.readValue(requestBody, clz);
    }

    private AbstractBaseUploadReq getDataXmlBean(String requestBody) throws Exception {
        logger.debug("{},获取中民JSON数据的实体类", MODULE_NAME);
        ObjectMapper mapper = new ObjectMapper();
        return (AbstractBaseUploadReq) mapper.readValue(requestBody, ZhongminOrderJson.class);
    }

    private AbstractBaseUploadReq getXmlBean(String requestBody){
        logger.debug("{},获取上传XML数据的实体类", MODULE_NAME);
        StringBuffer xmlStr = new StringBuffer(requestBody);
        OrderXml o = null;
        try {
            o = (OrderXml) u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
        } catch (JAXBException e) {
            logger.debug("{}解析XML格式数据失败!", MODULE_NAME);
        }
        return o;
    }
}
