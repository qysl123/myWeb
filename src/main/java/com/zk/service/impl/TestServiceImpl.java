package com.zk.service.impl;

import com.zk.dao.TestDao;
import com.zk.dao.TestMapper;
import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
import com.zk.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service("testService")
@Transactional
public class TestServiceImpl implements TestService{

//    @Resource
//    private TestMapper testMapper;

    @Resource
    private TestDao testDao;

    @Override
    public TestVO getTest(String id) {
        return testDao.getTest(id);
    }

    @Override
    public void saveTest(TestVO testVO) {
        testDao.saveTest(testVO);
    }

    @Override
    public List<TestVO> getAllTest() {
        return testDao.getAll();
    }

    @Override
    public void removeTest(TestVO testVO) {
        testDao.deleteTest(testVO);
    }

    @Override
    public void modifyTest(TestVO testVO) {
        testDao.updateTest(testVO);
    }

    @Override
    public FatherTestVO getFather(String id) {
        return testDao.getFather(id);
    }

    @Override
    public void saveFather(FatherTestVO fatherTestVO) {
        testDao.saveFather(fatherTestVO);
    }

    @Override
    public List<FatherTestVO> getAllFather() {
        return testDao.getAllFather();
    }

    @Override
    public void deleteFather(FatherTestVO fatherTestVO) {
        testDao.deleteFather(fatherTestVO);
    }

    @Override
    public void modifyFather(FatherTestVO fatherTestVO) {
        testDao.updateFather(fatherTestVO);
    }
}
