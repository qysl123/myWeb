package com.yc.eps.payment.score;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yc.edsi.payment.score.ScoreTaskPO;

interface IScoreTaskDao {
    List<ScoreTaskPO> getScoreTaskList();

    void changeTaskScore(ScoreTaskPO scoreTaskPO);
    
    /**
     * 获取客户端用户的翼钻任务完成情况
     * 
     * @param ownerId
     * @return
     */
    List<ScoreTaskPO> getOwnerScoreTasks(@Param("ownerId") Long ownerId, @Param("communityId") Long communityId,
            @Param("doneStatus") Integer doneStatus);
}
