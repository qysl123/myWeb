package com.zk.service.impl;

import com.zk.dao.TestMapper;
import com.zk.service.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("testService")
public class TestServiceImpl implements TestService{

    @Resource
    private TestMapper testMapper;

    @Override
    public void testHello() {
        testMapper.testHello();
        System.out.println(2222);
    }
}
