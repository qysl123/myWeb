package com.yc.eps.payment.score;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.score.OwnerScorePO;

interface IOwnerScoreDao {
    /**
     * 添加翼钻
     * 
     * @param score
     * @throws EdsiException
     */
    void addScore(OwnerScorePO score);

    /**
     * 获取业主翼钻
     * 
     * @param ownerId
     * @return
     * @throws EdsiException
     */
    long getOwnerScore(@Param("ownerId")Long ownerId, @Param("communityId")Long communityId);

    int checkIfAlreadyPay(@Param("orderId")Long orderId, @Param("communityId")Long communityId);

    int getTaskScoreCount(OwnerScorePO ownerScore);

    List<String> getTaskPrompt(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId,
            @Param("optUris") List<String> optUris);

    void clearTaskPrompt(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId,
            @Param("optUris") List<String> optUris);

    OwnerScorePO getOwnerScoreByDay(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId,
            @Param("taskId") Long taskId, @Param("day") String day);

    List<OwnerScorePO> getOwnerScoreByMon(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId,
            @Param("taskId") Long taskId, @Param("mon") String mon);

    long getActiveValue(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId);
}
