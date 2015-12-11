package com.zk.redis.dao;

import com.zk.entity.TestVO;

import java.util.List;

public interface RedisDao {

    void saveTest(TestVO testVO);
    TestVO getTest(String id);

}
