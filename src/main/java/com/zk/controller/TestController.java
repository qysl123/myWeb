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
        try {
            TestVO test = new TestVO();
            FatherTestVO father = new FatherTestVO();

            father.setName("father");

            test.setName("test");
            test.setFather(father);
//            testService.saveTest(test);

            List<TestVO> testVOList = testService.getAllTest();
            for (TestVO v : testVOList){
                test = v;
                System.out.println(v);
                v.setName(v.getName()+":123");
                testService.modifyTest(v);
            }

            testService.removeTest(test);

            List<FatherTestVO> fatherTestList = testService.getAllFather();
            for (FatherTestVO v : fatherTestList){
                System.out.println(v);
                v.setName(v.getName()+":123");
                testService.modifyFather(v);
            }
            testService.deleteFather(fatherTestList.get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/testHello.do")
    @ResponseBody
    public String login(@RequestParam Map<String, Object> paramMap){
        return "";
    }
}
