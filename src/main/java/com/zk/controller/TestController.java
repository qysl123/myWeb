package com.zk.controller;

import com.zk.base.DataChargeNotifyResponse;
import com.zk.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Map;

@Controller
@RequestMapping("testController")
public class TestController {

    @Resource
    private TestService testService;

    @RequestMapping("/testHello.do")
    @ResponseBody
    public void testHello(@RequestParam Map<String, Object> paramMap) {
    }

    @RequestMapping("/login.do")
    @ResponseBody
    public String login(@RequestParam Map<String, Object> paramMap){
        return "";
    }


    public static void main(String[] args){
        System.out.println(URLEncoder.encode("http://m.qqxmall.com/product.do?pid=212"));
    }
}
