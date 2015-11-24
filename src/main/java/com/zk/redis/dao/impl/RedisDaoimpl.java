package com.zk.redis.dao.impl;

import com.zk.entity.TestVO;
import com.zk.redis.dao.RedisDao;
import net.sf.json.JSONObject;
import org.codehaus.jackson.map.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RedisDaoimpl implements RedisDao {

    @Autowired
    protected RedisTemplate<Serializable, Serializable> redisTemplate;

    @Override
    public void saveString() {
        redisTemplate.execute(new RedisCallback<Object>() {

            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(redisTemplate.getStringSerializer().serialize("Test1"),
                        redisTemplate.getStringSerializer().serialize("Value1"));
                return null;
            }
        });
    }

    @Override
    public String getString() {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bb = redisTemplate.getStringSerializer().serialize("Test1");
                if(connection.exists(bb)){
                    return redisTemplate.getStringSerializer().deserialize(connection.get(bb));
                }
                return null;
            }
        });
    }

    @Override
    public void saveTestList(final List<TestVO> testList) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
            for (TestVO vo : testList){
                redisConnection.set(redisTemplate.getStringSerializer().serialize(vo.getId()),
                        redisTemplate.getStringSerializer().serialize(JSONObject.fromObject(vo).toString()));
            }
            return null;
            }
        });
    }

    @Override
    public TestVO gettestList(final String id) {
        return redisTemplate.execute(new RedisCallback<TestVO>() {
            @Override
            public TestVO doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bb = redisTemplate.getStringSerializer().serialize(id);
                TestVO vo = null;
                if(connection.exists(bb)){
                    String str = redisTemplate.getStringSerializer().deserialize(connection.get(bb));
                    vo = (TestVO) JSONObject.toBean(JSONObject.fromObject(str), TestVO.class);
                }
                return vo;
            }
        });
    }
}
