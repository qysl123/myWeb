package com.zk.dao;

import com.zk.entity.TestVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestMapper {

    TestVO getTest(String id);
    void saveTest(TestVO testVO);
    void updateTest(TestVO testVO);
    List<TestVO> getAllTest();
}
