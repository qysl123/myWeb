package com.zk.redis.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.zk.entity.TestVO;
import com.zk.redis.dao.RedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public class RedisDaoimpl implements RedisDao {

//    @Autowired
    protected RedisTemplate<Serializable, Serializable> redisTemplate;

    @Override
    public void saveTest(final TestVO testVO) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.set(redisTemplate.getStringSerializer().serialize(testVO.getId()),
                        redisTemplate.getStringSerializer().serialize(JSONObject.toJSONString(testVO)));
                return null;
            }
        });
    }

    @Override
    public TestVO getTest(final String id) {
        return redisTemplate.execute(new RedisCallback<TestVO>() {
            @Override
            public TestVO doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bb = redisTemplate.getStringSerializer().serialize(id);
                TestVO vo = null;
                if (connection.exists(bb)) {
                    String str = redisTemplate.getStringSerializer().deserialize(connection.get(bb));
                    vo = JSONObject.parseObject(str, TestVO.class);
                }
                return vo;
            }
        });
    }
}
