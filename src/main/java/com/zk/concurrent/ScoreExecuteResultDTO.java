package com.zk.concurrent;

import java.util.Map;

/**
 * 积分执行结果DTO
 * Created by Ken on 2016/11/14.
 */
public class ScoreExecuteResultDTO {

    private Map<String, String> failMessageMap;
    private Long changeTotalScore = 0L;//改变的总积分
    private Long changeScore = 0L;//变换的可用积分
    private Long oldLevelId;//旧等级id
    private String oldLevelName;//旧等级名称
    private Long newLevelId;//新等级id
    private String newLevelName;//新等级名称

    public Map<String, String> getFailMessageMap() {
        return failMessageMap;
    }

    public void setFailMessageMap(Map<String, String> failMessageMap) {
        this.failMessageMap = failMessageMap;
    }

    public Long getChangeTotalScore() {
        return changeTotalScore;
    }

    public void setChangeTotalScore(Long changeTotalScore) {
        this.changeTotalScore = changeTotalScore;
    }

    public Long getChangeScore() {
        return changeScore;
    }

    public void setChangeScore(Long changeScore) {
        this.changeScore = changeScore;
    }

    public Long getOldLevelId() {
        return oldLevelId;
    }

    public void setOldLevelId(Long oldLevelId) {
        this.oldLevelId = oldLevelId;
    }

    public String getOldLevelName() {
        return oldLevelName;
    }

    public void setOldLevelName(String oldLevelName) {
        this.oldLevelName = oldLevelName;
    }

    public Long getNewLevelId() {
        return newLevelId;
    }

    public void setNewLevelId(Long newLevelId) {
        this.newLevelId = newLevelId;
    }

    public String getNewLevelName() {
        return newLevelName;
    }

    public void setNewLevelName(String newLevelName) {
        this.newLevelName = newLevelName;
    }
}
