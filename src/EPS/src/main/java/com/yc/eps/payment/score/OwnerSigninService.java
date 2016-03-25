/*
 * E社区
 * Copyright (c) 2014 成都翼承科技 All Rights Reserved.
 */
package com.yc.eps.payment.score;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yc.commons.TimeUtil;
import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.score.IOwnerSigninService;
import com.yc.edsi.payment.score.IScoreTaskService;
import com.yc.edsi.payment.score.OwnerSigninPO;
import com.yc.edsi.payment.score.ScoreTaskPO;

/**
 * @author <a href="mailto:zhouc@yichenghome.com">Zhou Chao</a>
 * @version 2.0
 * @since 2015年3月5日
 */
@Service
public class OwnerSigninService implements IOwnerSigninService {
    @Resource
    private IOwnerSigninDao ownerSigninDao;
    
    @Resource
    private IScoreTaskService scoreTaskService;

    @Override
    public OwnerSigninPO getByOwnerId(Long ownerId) throws EdsiException {
        return ownerSigninDao.getByOwnerId(ownerId);
    }

    /**
     * 获取用户连续签到情况
     * @param ownerId
     * @return optUri + "," + contSignedTimes
     * @throws EdsiException
     */
    @Override
    public OwnerSigninPO getSigninOptUri(Long ownerId) throws EdsiException {
        OwnerSigninPO ownerSignin = ownerSigninDao.getByOwnerId(ownerId);
        if (ownerSignin == null) {// 第1次签到
            ownerSignin = toOwnerSigin(ownerId, 1);
            ownerSigninDao.add(ownerSignin);
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_ORDINARY);
            return ownerSignin;
        }
        
        if (TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD).equals(ownerSignin.getSigninTime().substring(0, 10))) {
            throw new EdsiException("今日已签到");
        }
        
        if (!TimeUtil.getNowDate(TimeUtil.YYYY_MM_DD).equals(ownerSignin.getNextSigninDate())) {// 不连续签到
            ownerSignin.setContSignedTimes(1);
            ownerSigninDao.update(ownerSignin);
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_ORDINARY);
            return ownerSignin;
        }

        ownerSignin.setContSignedTimes(ownerSignin.getContSignedTimes() + 1);
        ownerSigninDao.update(ownerSignin);
        if (ownerSignin.getContSignedTimes() <= 2) {// 包括本次签到，连续签到不超过2次
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_ORDINARY);
            return ownerSignin;
        }
        
        if (ownerSignin.getContSignedTimes() <= 7) {// 包括本次签到，连续签到不超过7次
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_GREATER_THAN_2);
            return ownerSignin;
        }
        
        if (ownerSignin.getContSignedTimes() <= 14) {// 包括本次签到，连续签到不超过14次
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_GREATER_THAN_7);
            return ownerSignin;
        }
        
        if (ownerSignin.getContSignedTimes() == 15) {// 包括本次签到，连续签到15次
            ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_EQUALS_15);
            return ownerSignin;
        }
        
        ownerSignin.setOptUri(ScoreTaskPO.OPT_URI_SIGNIN_GREATER_THAN_14);
        return ownerSignin;
    }

    private OwnerSigninPO toOwnerSigin(Long ownerId, int contSignedTimes) {
        OwnerSigninPO ownerSignin = new OwnerSigninPO();
        ownerSignin.setOwnerId(ownerId);
        ownerSignin.setContSignedTimes(contSignedTimes);
        return ownerSignin;
    }

}
