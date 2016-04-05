package com.zk.controller;

import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
import com.zk.service.DataUsageService;
import com.zk.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("testController")
public class TestController {

    @Resource
    private TestService testService;

    @Resource
    private DataUsageService dataUsageService;

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
}
