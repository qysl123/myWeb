package com.zk.controller;

import com.zk.base.DataChargeNotifyResponse;
import com.zk.entity.DateTestVO;
import com.zk.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Date;
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

    @RequestMapping(value = "/test.do")
    @ResponseBody
    public Object test(@RequestBody String id){
        System.out.println(id);
        return id;
    }

    @RequestMapping(value = "/test2.do")
    @ResponseBody
    public Object test2(@ModelAttribute DateTestVO dateTestVO){
        System.out.println(dateTestVO);
        return dateTestVO;
    }
}
