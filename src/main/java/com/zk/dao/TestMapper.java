package com.zk.dao;

import com.zk.entity.TestVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestMapper {

    List<TestVO> testHello();

}
