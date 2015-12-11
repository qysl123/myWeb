package com.zk.controller;

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
    public
    @ResponseBody
    void testHello(@RequestParam Map<String, Object> paramMap){
        TestVO testVO;
        System.out.println("获取所有:");
        List<TestVO> allList = testService.getAllTest();
        for (TestVO vo : allList){
            System.out.println(vo);
        }

        int random = (int)(Math.random() * 20);
        System.out.println("保存:"+random);

        if(random < allList.size()){
            testVO = allList.get(random);
            testVO.setName("test" + testVO.getId() + ":" + random);
        }else{
            testVO = new TestVO();
            testVO.setName("test" + testVO.getId() + ":" + random);
        }
        testService.saveTest(testVO);

        System.out.println("获取单个:"+testVO.getId());
        testVO = testService.getTest(testVO.getId());
        System.out.println(testVO);


        System.out.println("获取没有的:"+1000);
        testVO = testService.getTest(""+1000);
        System.out.println(testVO);
    }
}
