package com.zk.dubbo.service.impl;

import com.zk.dubbo.service.TestRegistryService;

//@Service(version = "0.0.1")
public class TestRegistryServiceImpl implements TestRegistryService{
    public String hello(String name) {
        return "MMM"+name;
    }
}