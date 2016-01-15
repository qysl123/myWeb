package com.calm.crm.weixin.controller;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.calm.crm.base.controller.BaseController;
import com.calm.crm.common.utils.ApiUrl;
import com.calm.crm.common.utils.Constant;
import com.calm.crm.common.utils.HttpUtils;
import com.calm.crm.weixin.aes.AesException;
import com.calm.crm.weixin.aes.WXBizMsgCrypt;
import com.calm.crm.weixin.entity.Button;
import com.calm.crm.weixin.entity.Menu;
import com.calm.crm.weixin.service.WeixinServerService;
import com.calm.erp.common.utils.HttpURLConnectionUtils;

/**
 * 微信服务端接口
 * 
 * @author zhanglg 2015年7月7日 上午11:27:35
 */
@Controller
@RequestMapping("weixinServerApi")
public class WeixinServerApiController extends BaseController {

	private Logger log = org.slf4j.LoggerFactory.getLogger(WeixinServerApiController.class);
	
	@Value("${weixin.corpid}")
	private String corpid;
	
	@Value("${weixin.crm_agentid}")
	private int crmAgentId;
	
	@Value("${weixin.gly_secret}")
	private String glySecret;
	
	@Value("${weixin.domain}")
	private String domain;
	
	@Value("${weixin.report_agentid}")
	private int reportAgentId;
	
	@Value("${weixin.operation_agentid}")
	private int operationAgentId;
	
	@Resource
	private WeixinServerService wxService;

	/**
	 * 获取企业号后台 token
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "getAccessToken", method = RequestMethod.GET)
	public void getAccessToken(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		try {
			String url = ApiUrl.GET_ACCESSTOKEN.replace("${corpid}", corpid).replace("${corpsecret}", glySecret);
			URL realUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			// 连接超时
			conn.setConnectTimeout(25000);
			// 读取超时 --服务器响应比较慢,增大时间
			conn.setReadTimeout(25000);
			HttpURLConnection.setFollowRedirects(true);
			// 请求方式
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0");
			conn.setRequestProperty("Referer", "https://api.weixin.qq.com/");
			conn.connect();
			// 获取URLConnection对象对应的输出流
			String result = HttpURLConnectionUtils.parseResultString(conn.getInputStream());
			JSONObject json = JSONObject.fromObject(result);
			String accessToken = json.getString("access_token");
			String expiresIn = json.getString("expires_in");
			if (!StringUtils.isEmpty(accessToken)) {
				wxService.setAccessToken(accessToken,expiresIn,glySecret);
				return;
			}
			// log.error(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("获取accessToken出错!");
		}
	}
	
	
	@RequestMapping(value = "/queryAccessToken", method = RequestMethod.GET)
	public @ResponseBody String queryAccessToken(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		try {
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			return tokenValue;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("获取accessToken出错!");
		}
		return null;
	}
	
	/**
	 * 获取企业号js sdk ticket
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "getJsTicket", method = RequestMethod.GET)
	public @ResponseBody String getJsTicket(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		try {
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			String ticket = wxService.getJsTicket(tokenValue);
			
			return ticket;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("获取accessToken出错!");
		}
		return null;
	}
	
	
	
	/**
	 * 获取签名
	 * @param jsapi_ticket
	 * @param timestamp
	 * @param nonce
	 * @param jsurl
	 * @return
	 */
	@RequestMapping(value = "/getSignatures", method = RequestMethod.POST)
	public @ResponseBody Map<String,String> getSignature(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getParameter("url");
		Map<String,String> map = new HashMap<String, String>();
		try{
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			String jsapi_ticket = wxService.getJsTicket(tokenValue);
			long timestamp = System.currentTimeMillis()/1000;
			
			map.put("appId", corpid);
			map.put("timestamp", timestamp+"");
			map.put("nonceStr", Constant.NONCESTR);
			
			
			/****
		     * 对 jsapi_ticket、 timestamp 和 nonce 按字典排序 对所有待签名参数按照字段名的 ASCII
		     * 码从小到大排序（字典序）后，使用 URL 键值对的格式（即key1=value1&key2=value2…）拼接成字符串
		     * string1。这里需要注意的是所有参数名均为小写字符。 接下来对 string1 作 sha1 加密，字段名和字段值都采用原始值，不进行
		     * URL 转义。即 signature=sha1(string1)。
		     * **如果没有按照生成的key1=value&key2=value拼接的话会报错
		     * A24ACA955B7D5B7F95F7C7F83363C07C0168556A
		     */
		    String[] paramArr = new String[] { "jsapi_ticket=" + jsapi_ticket,
		            "timestamp=" + timestamp, "noncestr=" + Constant.NONCESTR, "url=" + url };
		    Arrays.sort(paramArr);
		    // 将排序后的结果拼接成一个字符串
		    String content = paramArr[0].concat("&"+paramArr[1]).concat("&"+paramArr[2])
		            .concat("&"+paramArr[3]);
		    System.out.println("拼接之后的content为:"+content);
		    MessageDigest md = MessageDigest.getInstance("SHA-1");
		    // 对拼接后的字符串进行 sha1 加密
		    byte[] digest = md.digest(content.toString().getBytes());
		    String  gensignature = this.byteToStr(digest);
		    map.put("signature", gensignature);
		}catch(Exception e){
			e.printStackTrace();
			log.error("获取JSAPI签名信息失败,错误信息："+e);
		}
			
		return map;    
	}
	 
	
	/**
	 * 各种消息的推送方法
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "core", method = RequestMethod.POST)
	public void applyPost(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		String respMessage = wxService.processRequest(request);
		this.printStr(response, respMessage);

	}

	/**
	 * 企业号验证签名方法
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "core", method = RequestMethod.GET)
	public void applyGet(HttpServletRequest request, HttpServletResponse response) {
		 
		Map<String,String[]> map = request.getParameterMap(); 
		 for(String key:map.keySet()){
			 String[] str = map.get(key);
			 for(int i=0;i<str.length;i++){
				 System.out.println(key+"="+str[i]);
			 }
 		 }
		// 微信加密签名
		String signature = URLDecoder.decode(request.getParameter(Constant.SIGNATURE));
		// 时间戳
		String timestamp = URLDecoder.decode(request.getParameter(Constant.TIMESTAMP));
		// 随机数/
		String nonce = URLDecoder.decode(request.getParameter(Constant.NONCE));
		// 随机字符串
		String echostr = URLDecoder.decode(request.getParameter(Constant.ECHOSTR));
		
		// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
		
		/*if (SignUtil.checkSignature(signature, timestamp, nonce,echostr)) {
			this.printStr(response, echostr);
		}订阅号验证方法*/
		
		WXBizMsgCrypt wxcpt;
        try {
        	System.out.println("token:"+Constant.TOKEN+"\n;  encodekey:"+Constant.ENCODINGAESKEY+" \n; corpid:"+corpid);
            wxcpt = new WXBizMsgCrypt(Constant.TOKEN, Constant.ENCODINGAESKEY, corpid);
            String sEchoStr = wxcpt.VerifyURL(signature, timestamp,nonce, echostr);
            // 验证URL成功，将sEchoStr返回
            this.printStr(response, sEchoStr);
        } catch (AesException e1) {
            e1.printStackTrace();
            log.error("微信验证回调URL出错，错误信息："+e1);
        }
	}
	
	
	/**
	 * push主菜单
	 */
	@RequestMapping(value = "/applyMainMenu", method = RequestMethod.GET)
	public void applyMenu(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer bufferRes = new StringBuffer();
		try {
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			String url = ApiUrl.CREATE_MENU.replace("${tokenValue}", tokenValue).replace("${agentId}", crmAgentId+"");
			
			//商户公海池菜单
//			Button btn = new Button();
//			btn.setName("商户公海池");
//			btn.setType(Constant.BTYPE_VIEW);
//			String commercialV = domain+"/myWxCommercialController/getCommercialPool";
//			String commercialVurl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(commercialV));
//			btn.setUrl(commercialVurl);
			//商户来源菜单
			Button btn = new Button();
			btn.setName("商户来源");
			
			Button btn01 = new Button();
			btn01.setName("商户公海池");
			btn01.setType(Constant.BTYPE_VIEW);
			String seaComm = domain+"/myWxCommercialController/getCommercialPool";
			String seaCommUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(seaComm));
			btn01.setUrl(seaCommUrl);
			
			Button btn02 = new Button();
			btn02.setName("大众点评");
			btn02.setType(Constant.BTYPE_VIEW);
			String comment = domain+"/myWxCommercialController/getPeopleComments";
			String commentUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(comment));
			btn02.setUrl(commentUrl);
			
			Button[] b0 = new Button[]{btn01,btn02};
			btn.setSub_button(b0);
			
			//商户菜单
			Button btn1 = new Button();
			btn1.setName("商户");

			Button btn11 = new Button();
			btn11.setName("我的商户");
			btn11.setKey("2");
			btn11.setType(Constant.BTYPE_VIEW);
			String myCommercialUrl = domain+"/myWxCommercialController/getMyCommercial";
			String murl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(myCommercialUrl));
			btn11.setUrl(murl);
			
			Button subCommercial = new Button();
			subCommercial.setName("商户查询");
			subCommercial.setType(Constant.BTYPE_VIEW);
			String subCommercialUrl = domain+"/myWxCommercialController/getSubCommercial";
			String subUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(subCommercialUrl));
			subCommercial.setUrl(subUrl);
			
			Button newCommercial = new Button();
			newCommercial.setName("新建商户");
			newCommercial.setType(Constant.BTYPE_VIEW);
			String newCommercialUrl = domain+"/myWxCommercialController/newCommercial";
			String newUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(newCommercialUrl));
			newCommercial.setUrl(newUrl);
			
			Button newContract = new Button();
			newContract.setName("新建联系人");
			newContract.setType(Constant.BTYPE_VIEW);
			String newContractUrl = domain+"/myWxCommercialController/newContract";
			String conUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(newContractUrl));
			newContract.setUrl(conUrl);
			
			Button visitPlanBtn = new Button();
			visitPlanBtn.setName("拜访计划");
			visitPlanBtn.setType(Constant.BTYPE_VIEW);
			String visitPlanBtnUrl = domain+"/myWxCommercialController/initVisitPlan";
			String visitPlanUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(visitPlanBtnUrl));
			visitPlanBtn.setUrl(visitPlanUrl);
			
			
			Button[] b1 = new Button[]{btn11,subCommercial,newCommercial,newContract,visitPlanBtn};
			btn1.setSub_button(b1);
			
			//活动记录菜单
			Button btn2 = new Button();
			btn2.setName("活动记录");

//			Button btn222 = new Button();
//			btn222.setName("新建活动记录");
//			btn222.setType(Constant.BTYPE_VIEW);
//			String redirectUrl2 = domain+"/activeRecodeController/createRecord";
//			String turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
//			btn222.setUrl(turl2);

			Button btn222 = new Button();
			btn222.setName("电话拜访");
			btn222.setType(Constant.BTYPE_VIEW);
			String redirectUrl2 = domain+"/activeRecodeController/telephoneVisit";
			String turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
			btn222.setUrl(turl2);
			
			Button btn333 = new Button();
			btn333.setName("拜访签到");
			btn333.setType(Constant.BTYPE_VIEW);
			redirectUrl2 = domain+"/activeRecodeController/visitToSign";
			turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
			btn333.setUrl(turl2);
			

			Button btn444 = new Button();
			btn444.setName("陪访签到");
			btn444.setType(Constant.BTYPE_VIEW);
			redirectUrl2 = domain+"/activeRecodeController/accompanyVisitToSign";
			turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
			btn444.setUrl(turl2);

			Button btn22 = new Button();
			btn22.setName("我的活动记录");
			btn22.setType(Constant.BTYPE_VIEW);
			String redirectUrl = domain+"/activeRecodeController/myRecord";
			String turl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl));
			btn22.setUrl(turl);

			Button btn23 = new Button();
			btn23.setName("下属活动记录");
			btn23.setType(Constant.BTYPE_VIEW);
			redirectUrl = domain+"/activeRecodeController/subRecord";
			turl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl));
			btn23.setUrl(turl);

			
			Button[] b2 = new Button[]{btn22,btn23,btn222,btn333,btn444};
			btn2.setSub_button(b2);
			
			Menu menu = new Menu();
			menu.setButton(new Button[]{btn,btn1,btn2});
			
			String btnStr = JSONObject.fromObject(menu).toString();
			
			JSONObject json = HttpUtils.httpRequest(url, "POST", btnStr);
			
			Iterator it = json.keys();
			while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = json.getString(key);  
                System.out.println(key+"   =====  "+value);
            }  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * push工作报告主菜单
	 */
	@RequestMapping(value = "/applyReportMainMenu", method = RequestMethod.GET)
	public void applyReportMenu(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer bufferRes = new StringBuffer();
		try {
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			String url = ApiUrl.CREATE_MENU.replace("${tokenValue}", tokenValue).replace("${agentId}", reportAgentId+"");
			
			
			//写报告菜单
			Button btn1 = new Button();
			btn1.setName("写报告");

			Button btn11 = new Button();
			btn11.setName("写日报");
			btn11.setKey("1");
			btn11.setType(Constant.BTYPE_VIEW);
			String createDailyReportUrl = domain+"/activeReportController/getCreateDailyReport";
			String durl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(createDailyReportUrl));
			btn11.setUrl(durl);
			
			Button btn12 = new Button();
			btn12.setName("写周报");
			btn12.setType(Constant.BTYPE_VIEW);
			String createWeeklyReportUrl = domain+"/activeReportController/getCreateWeeklyReport";
			String wUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(createWeeklyReportUrl));
			btn12.setUrl(wUrl);
			
			Button btn13 = new Button();
			btn13.setName("写月报");
			btn13.setType(Constant.BTYPE_VIEW);
			String createMonthlyReportUrl = domain+"/activeReportController/getCreateMonthlyReport";
			String mUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(createMonthlyReportUrl));
			btn13.setUrl(mUrl);
			
			Button[] b1 = new Button[]{btn11,btn12,btn13};
			btn1.setSub_button(b1);
			
			//我的报告菜单
			Button btn2 = new Button();
			btn2.setName("我的报告");

			Button btn21 = new Button();
			btn21.setName("历史报告");
			btn21.setType(Constant.BTYPE_VIEW);
			String historyUrl = domain+"/activeReportController/getHistoryReport";
			String histUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(historyUrl));
			btn21.setUrl(histUrl);
			

			Button btn22 = new Button();
			btn22.setName("草稿箱");
			btn22.setType(Constant.BTYPE_VIEW);
			String draftUrl = domain+"/activeReportController/getDraftReport";
			String drafUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(draftUrl));
			btn22.setUrl(drafUrl);

			Button[] b2 = new Button[]{btn21,btn22};
			btn2.setSub_button(b2);
			
			//下属的报告菜单
			Button btn3 = new Button();
			btn3.setName("下属的报告");

			Button btn31 = new Button();
			btn31.setName("抄送给我的报告");
			btn31.setType(Constant.BTYPE_VIEW);
			String ccUrl = domain+"/activeReportController/getCCToMeReport";
			String ccMeUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(ccUrl));
			btn31.setUrl(ccMeUrl);
			

			Button btn32 = new Button();
			btn32.setName("提交给我的报告");
			btn32.setType(Constant.BTYPE_VIEW);
			String submitUrl = domain+"/activeReportController/getSubmitToMeReport";
			String submUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(submitUrl));
			btn32.setUrl(submUrl);

			Button[] b3 = new Button[]{btn31,btn32};
			btn3.setSub_button(b3);
			
			Menu menu = new Menu();
			menu.setButton(new Button[]{btn1,btn2,btn3});
			
			String btnStr = JSONObject.fromObject(menu).toString();
			
			JSONObject json = HttpUtils.httpRequest(url, "POST", btnStr);
			
			Iterator it = json.keys();
			while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = json.getString(key);  
                System.out.println(key+"   =====  "+value);
            }  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 运维联盟主菜单
	 */
	@RequestMapping(value = "/applyOperationMainMenu", method = RequestMethod.GET)
	public void applyOperationMainMenu(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer bufferRes = new StringBuffer();
		try {
			String tokenValue = wxService.getAccessTokenValue(glySecret,corpid);
			String url = ApiUrl.CREATE_MENU.replace("${tokenValue}", tokenValue).replace("${agentId}", operationAgentId+"");
			
//			s商户菜单
			Button btn1 = new Button();
			btn1.setName("商户");
//
//			Button btn11 = new Button();
//			btn11.setName("我的商户");
//			btn11.setKey("2");
//			btn11.setType(Constant.BTYPE_VIEW);
//			String myCommercialUrl = domain+"/myWxCommercialController/getMyCommercial";
//			String murl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(myCommercialUrl));
//			btn11.setUrl(murl);
//			
//			Button subCommercial = new Button();
//			subCommercial.setName("商户查询");
//			subCommercial.setType(Constant.BTYPE_VIEW);
//			String subCommercialUrl = domain+"/myWxCommercialController/getSubCommercial";
//			String subUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(subCommercialUrl));
//			subCommercial.setUrl(subUrl);
//			
//			Button newCommercial = new Button();
//			newCommercial.setName("新建商户");
//			newCommercial.setType(Constant.BTYPE_VIEW);
//			String newCommercialUrl = domain+"/myWxCommercialController/newCommercial";
//			String newUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(newCommercialUrl));
//			newCommercial.setUrl(newUrl);
//			
//			Button newContract = new Button();
//			newContract.setName("新建联系人");
//			newContract.setType(Constant.BTYPE_VIEW);
//			String newContractUrl = domain+"/myWxCommercialController/newContract";
//			String conUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(newContractUrl));
//			newContract.setUrl(conUrl);
//			
//			Button visitPlanBtn = new Button();
//			visitPlanBtn.setName("拜访计划");
//			visitPlanBtn.setType(Constant.BTYPE_VIEW);
//			String visitPlanBtnUrl = domain+"/myWxCommercialController/initVisitPlan";
//			String visitPlanUrl = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(visitPlanBtnUrl));
//			visitPlanBtn.setUrl(visitPlanUrl);
//			
//			
			Button[] b1 = new Button[]{};
			btn1.setSub_button(b1);
			
			//活动记录菜单
			Button btn2 = new Button();
			btn2.setName("活动记录");


			Button btn222 = new Button();
			btn222.setName("电话拜访");
			btn222.setType(Constant.BTYPE_VIEW);
			String redirectUrl2 = domain+"/activeRecodeController/telephoneVisitOperation";
			String turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
			btn222.setUrl(turl2);
			
			Button btn333 = new Button();
			btn333.setName("拜访签到");
			btn333.setType(Constant.BTYPE_VIEW);
			redirectUrl2 = domain+"/activeRecodeController/visitToSignOperation";
			turl2 = ApiUrl.AUTH2.replace("${corpid}", corpid).replace("${redirectUrl}", URLEncoder.encode(redirectUrl2));
			btn333.setUrl(turl2);

			Button[] b2 = new Button[]{btn222,btn333};
			btn2.setSub_button(b2);
			
			Menu menu = new Menu();
			menu.setButton(new Button[]{btn2});
			
			String btnStr = JSONObject.fromObject(menu).toString();
			
			JSONObject json = HttpUtils.httpRequest(url, "POST", btnStr);
			
			Iterator it = json.keys();
			while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = json.getString(key);  
                System.out.println(key+"   =====  "+value);
            }  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
