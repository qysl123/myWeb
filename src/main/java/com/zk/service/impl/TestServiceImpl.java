package com.zk.service.impl;

import com.zk.dao.TestMapper;
import com.zk.entity.TestVO;
import com.zk.redis.dao.RedisDao;
import com.zk.service.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("testService")
public class TestServiceImpl implements TestService{

    @Resource
    private TestMapper testMapper;

    @Resource
    private RedisDao redisDao;

    @Override
    public void testHello() {
        List<TestVO> testList = testMapper.testHello();
        for(TestVO vo : testList){
            System.out.println(vo);
        }
        System.out.println("hehe");
        redisDao.saveTestList(testList);
        for(TestVO vo : testList){
            System.out.println(redisDao.gettestList(vo.getId()));
        }

    }
}
