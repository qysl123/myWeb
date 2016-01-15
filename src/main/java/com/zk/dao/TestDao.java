package com.zk.dao;

import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;

import java.util.List;

public interface TestDao {

    void saveTest(TestVO testVO);

    TestVO getTest(String id);

    List<TestVO> getAll();

    void updateTest(TestVO testVO);

    void deleteTest(TestVO testVO);

    void saveFather(FatherTestVO fatherTestVO);

    FatherTestVO getFather(String id);

    List<FatherTestVO> getAllFather();

    void updateFather(FatherTestVO fatherTestVO);

    void deleteFather(FatherTestVO fatherTestVO);
}
