/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment.score;

import com.yc.edsi.payment.score.OwnerSigninPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2015年3月5日
 */
public interface IOwnerSigninDao {
    void add(OwnerSigninPO ownerSignin);
    
    OwnerSigninPO getByOwnerId(Long ownerId);

    void update(OwnerSigninPO ownerSignin);
}
