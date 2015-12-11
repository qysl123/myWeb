package com.zk.service;

import com.zk.entity.TestVO;

import java.util.List;

public interface TestService {

    TestVO getTest(String id);
    void saveTest(TestVO testVO);
    List<TestVO> getAllTest();
}
