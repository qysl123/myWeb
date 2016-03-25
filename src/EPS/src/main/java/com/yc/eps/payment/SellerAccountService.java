/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yc.commons.mybatis.pagination.utils.RowBoundsUtil;
import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.ISellerAccountService;
import com.yc.edsi.payment.SellerAccountPO;
import com.yc.edsi.payment.SellerAccountQuery;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 
 */
@Service
public class SellerAccountService implements ISellerAccountService {
    @Resource
    private ISellerAccountDao sellerAccountDao;
    
    /** 按主键ID查询数据 */
    public SellerAccountPO getById(java.lang.Long id) throws EdsiException {
        return sellerAccountDao.getById(id);
    }
    
    /** 删除数据 */
    public void deleteById(java.lang.Long id) throws EdsiException {
        sellerAccountDao.deleteById(id);
    }
    
    /** 插入数据 */
    public void insert(SellerAccountPO entity) throws EdsiException {
        sellerAccountDao.insert(entity);
    }
    
    /** 更新数据 */
    public void update(SellerAccountPO entity) throws EdsiException {
        sellerAccountDao.update(entity);
    }
    
    @Override
    public List<SellerAccountPO> find(SellerAccountQuery query) throws EdsiException {
        return sellerAccountDao.find(query);
    }

    /** 按条件分页查询数据 */
    public Pagination find(SellerAccountQuery query, Pagination pagination, SellerAccountPO entity) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerAccountPO> list = sellerAccountDao.find(query, pagination, rb);
        pagination.setList(list);
        return pagination;
    }

    /** 按条件分页查询数据2 */
    public List<SellerAccountPO> getListForClient(SellerAccountQuery query) throws EdsiException {
        return sellerAccountDao.getListForClient(query);
    }

    /** 按条件分页查询数据数量 */
    public int getCount(SellerAccountQuery query) throws EdsiException {
        return sellerAccountDao.getCount(query);
    }

    @Override
    public SellerAccountPO getRestAccount(SellerAccountQuery query) throws EdsiException {
        return sellerAccountDao.getRestAccount(query);
    }

    @Transactional("transactionManager")
    @Override
    public void withDraw(SellerAccountQuery query) {
        sellerAccountDao.insertByIncome(query);
        query.setsSettleId(query.getSettleId());
        sellerAccountDao.updateSellerApply(query);
    }

    @Override
    public Pagination getWithdrawList(SellerAccountQuery query, Pagination pagination, SellerAccountPO entity)
            throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<SellerAccountPO> list = sellerAccountDao.getWithdrawList(query, pagination, rb);
        pagination.setList(list);
        return pagination;
    }

    @Override
    public void updatePlatformApply(Long settleId) throws EdsiException {
        sellerAccountDao.updatePlatformApply(settleId);
    }
}
