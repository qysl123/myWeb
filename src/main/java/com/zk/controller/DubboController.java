package com.zk.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zk.service.ProducerService;
import com.zk.service.TestRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.jms.Destination;

@Controller
public class DubboController {

//    @Reference(version = "0.0.1")
    private TestRegistryService testRegistryService;

    @Resource
    private ProducerService producerService;

    @Autowired
    @Qualifier("queueDestination")
    private Destination destination;

    @RequestMapping("/hello")
    public String index(Model model){
        String name=testRegistryService.hello("zz");
        System.out.println("xx=="+name);
        return "";
    }

    @RequestMapping("/mq")
    public String mq(Model model){
        producerService.sendMessage(destination, "hello mq");
        return "";
    }
}
