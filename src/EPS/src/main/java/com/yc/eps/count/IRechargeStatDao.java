/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.count;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.count.RechargeInfoPO;
import com.yc.edsi.count.RechargeStatPO;
import com.yc.edsi.count.RechargeStatQuery;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 
 */
public interface IRechargeStatDao {
    /** 按条件分页查询数据 */
    List<RechargeStatPO> find(@Param("entity")RechargeStatQuery query, Pagination pagination, RowBounds rb);
    
    /** 按充值人账号查询充值记录 */
    List<RechargeInfoPO> getRechargeList(@Param("entity")RechargeStatQuery query, Pagination pagination, RowBounds rb);
}
