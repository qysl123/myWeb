package com.zk.dubbo.controller;

import com.zk.dubbo.service.TestRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.jms.Destination;

@Controller
public class DubboController {

//    @Reference(version = "0.0.1")
    private TestRegistryService testRegistryService;

    @RequestMapping("/hello")
    public String index(Model model){
        String name=testRegistryService.hello("zz");
        System.out.println("xx==" + name);
        return "";
    }
}
