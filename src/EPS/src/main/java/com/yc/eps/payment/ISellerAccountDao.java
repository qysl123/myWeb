/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.payment.SellerAccountPO;
import com.yc.edsi.payment.SellerAccountQuery;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 
 */
public interface ISellerAccountDao {
    /** 按主键ID查询数据 */
    SellerAccountPO getById(java.lang.Long id);
    
    /** 按主键ID删除数据 */
    void deleteById(java.lang.Long id);
    
    /** 插入数据 */
    void insert(@Param("entity")SellerAccountPO entity);
    
    /** 更新数据 */
    void update(@Param("entity")SellerAccountPO entity);
    
    /** 按条件查询数据 */
    List<SellerAccountPO> find(@Param("entity")SellerAccountQuery query);
    
    /** 按条件分页查询数据 */
    List<SellerAccountPO> find(@Param("entity")SellerAccountQuery query, Pagination pagination, RowBounds rb);
    
    /** 按条件分页查询数据2 */
    List<SellerAccountPO> getListForClient(@Param("entity")SellerAccountQuery query);
    
    /** 按条件分页查询数据数量 */
    int getCount(@Param("entity")SellerAccountQuery query);

    SellerAccountPO getBySpaymentId(@Param("sPaymentId")Long sPaymentId);

    SellerAccountPO getRestAccount(@Param("entity")SellerAccountQuery query);

    void updateSellerApply(@Param("entity")SellerAccountQuery query);

    void insertByIncome(SellerAccountQuery query);

    List<SellerAccountPO> getWithdrawList(@Param("entity")SellerAccountQuery query, Pagination pagination, RowBounds rb);

    void updatePlatformApply(@Param("settleId")Long settleId);
}
