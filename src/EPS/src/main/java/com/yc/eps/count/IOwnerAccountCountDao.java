/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.count;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.yc.commons.mybatis.pagination.vo.Pagination;
import com.yc.edsi.count.OwnerAccountCountPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2014年8月12日
 */
public interface IOwnerAccountCountDao {
	List<OwnerAccountCountPO> getOwnerAccountList(@Param("oac") OwnerAccountCountPO oac,
	        @Param("rangePropertyIds") String rangePropertyIds, @Param("propertyIds") List<Long> propertyIds,
	        Pagination pagination, RowBounds rb);

	List<OwnerAccountCountPO> getOwnerAccountList(@Param("oac") OwnerAccountCountPO oac,
	        @Param("rangePropertyIds") String rangePropertyIds, @Param("propertyIds") List<Long> propertyIds);
}
