package com.zk.service.impl;

import com.zk.dao.TestMapper;
import com.zk.entity.TestVO;
import com.zk.service.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("testService")
public class TestServiceImpl implements TestService{

    @Resource
    private TestMapper testMapper;

    @Override
    public TestVO getTest(String id) {
        return testMapper.getTest(id);
    }

    @Override
    public void saveTest(TestVO testVO) {
        if(testVO.getId() == null || this.getTest(testVO.getId()) == null){
            testMapper.saveTest(testVO);
        }else{
            testMapper.updateTest(testVO);
        }
    }

    @Override
    public List<TestVO> getAllTest() {
        return testMapper.getAllTest();
    }
}
