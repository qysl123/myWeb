package com.zk.controller;

import com.zk.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("testController")
public class TestController {

    @Resource
    private TestService testService;

    @RequestMapping("/testHello.do")
    public
    @ResponseBody
    void testHello(@RequestParam Map<String, Object> paramMap){
        testService.testHello();
    }
}
