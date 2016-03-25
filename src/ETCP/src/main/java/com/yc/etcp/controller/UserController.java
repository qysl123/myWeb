package com.yc.etcp.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.utils.ServiceSettings;
import com.aliyun.mns.model.Message;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.commons.ITokenRecordsService;
import com.yc.edsi.owner.OwnerUserWithAddressPO;
import com.yc.etcp.common.HttpHelper;
import com.yc.etcp.request.Auth;
import com.yc.etcp.response.Response;
import com.yc.etcp.response.UserInfoResponse;

/**
 * 用户对外接口控制类
 * 
 * @author Diao Lei
 * @version 1.0
 * @since 2015年12月25日
 *
 */

@Controller
public class UserController {
	@Resource
	private ITokenRecordsService tokenRecordsService;
	
	private final static Logger logger = LoggerFactory.getLogger(UserController.class);
	
	private final static String MODULE_NAME = "用户对外接口控制类";
	
	private MNSClient client = null;
    private static String QUEUE_NAME = "yishequ";
    
	private MNSClient getClient() {
		if (client == null) {
			CloudAccount account = new CloudAccount(
					ServiceSettings.getMNSAccessKeyId(),
					ServiceSettings.getMNSAccessKeySecret(),
					ServiceSettings.getMNSAccountEndpoint());
			client = account.getMNSClient();
		}
		return client;
	}

	@RequestMapping(value = "/checkToken.do", method = RequestMethod.POST, headers="Accept=application/json")
	@ResponseBody
	public Response getUserInfo(@RequestBody Auth request) {
		logger.debug("{},用户信息获取", MODULE_NAME);
		UserInfoResponse userInfo = new UserInfoResponse();
		try {
			OwnerUserWithAddressPO ownerUserWithAddress = tokenRecordsService.findOwnerUser(request.getToken());
			return Response.createSuccessResponse(userInfo.addUserInfo(ownerUserWithAddress));
		} catch (EdsiException e) {
			logger.error(MODULE_NAME, e);
			return Response.createFailureResponse(e.getMessage());
		} catch (Exception e) {
			logger.error(MODULE_NAME, e);
			return Response.createFailureResponse("未知异常");
		}
	}
	
	@RequestMapping(value = "/push.do")
	public @ResponseBody String pushMsg(HttpServletRequest request) {
		// 获取队列
		String requestBody = HttpHelper.getBodyString(request);
        CloudQueue queue = getClient().getQueueRef(QUEUE_NAME);

        // 发送消息
        Message message = new Message();
        message.setMessageBody(requestBody);
        Message putMsg = queue.putMessage(message);
        return putMsg.getMessageId();
		//http://1378095664271027.mns.cn-hangzhou.aliyuncs.com/queues/yishequ
	}
}
