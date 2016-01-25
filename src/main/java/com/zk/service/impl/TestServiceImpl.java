package com.zk.service.impl;

import com.zk.dao.TestMapper;
import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
import com.zk.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service("testService")
public class TestServiceImpl implements TestService{

    @Resource
    private TestMapper testMapper;

//    @Resource
//    private TestDao testDao;

    @Override
    public TestVO getTest(String id) {
        return null;//testDao.getTest(id);
    }

    @Override
    @Transactional
    public void saveTest(TestVO testVO) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testMapper.saveTest(testVO);
    }

    @Override
    public List<TestVO> getAllTest() {
        return null;//testDao.getAll();
    }

    @Override
    @Transactional
    public void removeTest(TestVO testVO) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testMapper.removeTest(testVO);
    }

    @Override
    public void modifyTest(TestVO testVO) {
//        testDao.updateTest(testVO);
    }

    @Override
    public FatherTestVO getFather(String id) {
        return null;//testDao.getFather(id);
    }

    @Override
    public void saveFather(FatherTestVO fatherTestVO) {
//        testDao.saveFather(fatherTestVO);
    }

    @Override
    public List<FatherTestVO> getAllFather() {
        return null;//testDao.getAllFather();
    }

    @Override
    public void deleteFather(FatherTestVO fatherTestVO) {
//        testDao.deleteFather(fatherTestVO);
    }

    @Override
    public void modifyFather(FatherTestVO fatherTestVO) {
//        testDao.updateFather(fatherTestVO);
    }
}
