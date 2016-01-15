package com.zk.service;

import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;

import java.util.List;

public interface TestService {

    TestVO getTest(String id);

    void saveTest(TestVO testVO);

    List<TestVO> getAllTest();

    void removeTest(TestVO testVO);

    void modifyTest(TestVO testVO);

    FatherTestVO getFather(String id);

    void saveFather(FatherTestVO fatherTestVO);

    List<FatherTestVO> getAllFather();

    void deleteFather(FatherTestVO fatherTestVO);

    void modifyFather(FatherTestVO fatherTestVO);
}
