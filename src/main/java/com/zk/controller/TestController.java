package com.zk.controller;

import com.zk.base.DataChargeNotifyResponse;
import com.zk.base.DataUsageBaseResponse;
import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
import com.zk.service.DataUsageService;
import com.zk.service.DataUsageService2;
import com.zk.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.applet.Main;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("testController")
public class TestController {

    @Resource
    private TestService testService;

    @Resource
    private DataUsageService dataUsageService;

    @Resource
    private DataUsageService2 dataUsageService2;

    @RequestMapping("/testHello.do")
    @ResponseBody
    public void testHello(@RequestParam Map<String, Object> paramMap) {
    }

    @RequestMapping("/login.do")
    @ResponseBody
    public String login(@RequestParam Map<String, Object> paramMap){
        return "";
    }

    @RequestMapping("/getPackage.do")
    @ResponseBody
    public String getPackage(@RequestParam Map<String, Object> paramMap){
        dataUsageService.getPackage("scysq", "0");
        return "";
    }

    @RequestMapping("/doCharge.do")
    @ResponseBody
    public String doCharge(@RequestParam Map<String, Object> paramMap){
        dataUsageService2.doCharge();
        return "";
    }

    @RequestMapping("/doNotify.do")
    @ResponseBody
    public String doNotify(DataChargeNotifyResponse dataChargeNotifyResponse){
        if(DataChargeNotifyResponse.TRADESTATUS_SUCCESS.equals(dataChargeNotifyResponse.getTradestatus())){
            System.out.println(dataChargeNotifyResponse);
        }
        dataUsageService2.doNotify();
        return "";
    }

    public static void main(String[] args){
        System.out.println(URLEncoder.encode("http://m.qqxmall.com/product.do?pid=212"));
    }
}
