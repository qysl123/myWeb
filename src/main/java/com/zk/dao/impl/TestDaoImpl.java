package com.zk.dao.impl;

import com.zk.dao.TestDao;
import com.zk.entity.FatherTestVO;
import com.zk.entity.TestVO;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


public class TestDaoImpl implements TestDao {


    private SessionFactory sessionFactory;

    @Override
    public void saveTest(TestVO testVO) {
        Session session = sessionFactory.getCurrentSession();
        session.save(testVO);
    }

    @Override
    public TestVO getTest(String id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(TestVO.class);
        criteria.add(Restrictions.eq("id", id)).list();
        List<TestVO> list = criteria.list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<TestVO> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(TestVO.class);
        return criteria.list();
    }

    @Override
    public void updateTest(TestVO testVO) {
        Session session = sessionFactory.getCurrentSession();
        session.update(testVO);
    }

    @Override
    public void deleteTest(TestVO testVO) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(testVO);
    }

    @Override
    public void saveFather(FatherTestVO fatherTestVO) {
        Session session = sessionFactory.getCurrentSession();
        session.save(fatherTestVO);
    }

    @Override
    public FatherTestVO getFather(String id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(TestVO.class);
        criteria.add(Restrictions.eq("id", id)).list();
        List<FatherTestVO> list = criteria.list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<FatherTestVO> getAllFather() {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(FatherTestVO.class);
        return criteria.list();
    }

    @Override
    public void updateFather(FatherTestVO fatherTestVO) {
        Session session = sessionFactory.getCurrentSession();
        session.update(fatherTestVO);
    }

    @Override
    public void deleteFather(FatherTestVO fatherTestVO) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(fatherTestVO);
    }
}
