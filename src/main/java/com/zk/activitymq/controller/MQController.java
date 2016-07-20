package com.zk.activitymq.controller;

import com.zk.activitymq.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.jms.Destination;

@Controller
public class MQController {

//    @Resource
//    private ProducerService producerService;
//
//    @Autowired
//    @Qualifier("queueDestination")
//    private Destination destination;
//
//    @Autowired
//    @Qualifier("sessionAwareQueue")
//    private Destination sessionAwareQueue;
//
//    @Autowired
//    @Qualifier("adapterQueue")
//    private Destination adapterQueue;
//
//    @RequestMapping("/mq")
//    public String mq(Model model){
//        producerService.sendMessage(destination, "hello mq");
//        return "";
//    }
//
//    @RequestMapping("/mq2")
//    public String mq2(Model model){
//        producerService.sendMessage(sessionAwareQueue, "测试SessionAwareMessageListener");
//        return "";
//    }
//
//    @RequestMapping("/mq3")
//    public String mq3(Model model){
//        producerService.sendMessage(adapterQueue, "测试messageListenerAdapter");
//        return "";
//    }
}
