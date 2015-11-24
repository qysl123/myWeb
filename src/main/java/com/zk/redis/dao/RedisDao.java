package com.zk.redis.dao;

import com.zk.entity.TestVO;

import java.util.List;

public interface RedisDao {

    void saveString();
    String getString();
    void saveTestList(List<TestVO> testList);
    TestVO gettestList(String id);

}
