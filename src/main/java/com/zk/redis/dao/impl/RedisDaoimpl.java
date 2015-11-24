package com.zk.redis.dao.impl;

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
}
