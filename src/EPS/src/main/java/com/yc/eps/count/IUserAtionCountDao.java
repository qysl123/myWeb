/*
 * E社区
 * Copyright (c) 2013 YT All Rights Reserved.
 */
package com.yc.eps.count;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.count.UserActionCountPO;

public interface IUserAtionCountDao {

    List<UserActionCountPO> getUserActionList(@Param("uac")UserActionCountPO userActionCountPO,@Param("rangePropertyIds")String rangePropertyIds,
    		@Param("propertyIds")List<Long> propertyIds) throws EdsiException;

    List<UserActionCountPO> getUserInfoList(@Param("uac")UserActionCountPO userActionCountPO,@Param("rangePropertyIds")String rangePropertyIds,
    		@Param("propertyIds")List<Long> propertyIds) throws EdsiException;
}
