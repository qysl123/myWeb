package com.zk.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zk.service.TestRegistryService;

//@Service(version = "0.0.1")
public class TestRegistryServiceImpl implements TestRegistryService{
    public String hello(String name) {
        return "MMM"+name;
    }
}