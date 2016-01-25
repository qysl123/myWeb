package com.zk.controller;

import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
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

    @RequestMapping("/testHello.do")
    @ResponseBody
    public void testHello(@RequestParam Map<String, Object> paramMap) {
        final TestVO t = new TestVO();
        t.setName("1");

        final TestVO tt = new TestVO();
        tt.setName("2");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                testService.saveTest(t);
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                testService.removeTest(tt);
            }
        };

        for(int i = 0;i<1;i++){
            Thread thread = new Thread(r);
            Thread thread2 = new Thread(r2);
            thread.start();
            thread2.start();
        }



    }

    @RequestMapping("/login.do")
    @ResponseBody
    public String login(@RequestParam Map<String, Object> paramMap){
        return "";
    }
}
