/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.count;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.yc.commons.mybatis.pagination.utils.RowBoundsUtil;
import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.count.IRechargeStatService;
import com.yc.edsi.count.RechargeInfoPO;
import com.yc.edsi.count.RechargeStatPO;
import com.yc.edsi.count.RechargeStatQuery;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 
 */
@Service
public class RechargeStatService implements IRechargeStatService {
    @Resource
    private IRechargeStatDao rechargeStatDao;
    
    /** 按条件分页查询数据 */
    public Pagination find(RechargeStatQuery query, Pagination pagination, RechargeStatPO entity) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<RechargeStatPO> list = rechargeStatDao.find(query, pagination, rb);
        pagination.setList(list);
        return pagination;
    }

    @Override
    public Pagination getRechargeList(RechargeStatQuery query, Pagination pagination, RechargeInfoPO entity) throws EdsiException {
        RowBounds rb = RowBoundsUtil.getRowBoundsByPage(pagination);
        List<RechargeInfoPO> list = rechargeStatDao.getRechargeList(query, pagination, rb);
        pagination.setList(list);
        return pagination;
    }
}
