package com.yc.eps.payment.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yc.edsi.commons.EdsiException;
import com.yc.edsi.payment.score.IScoreTaskService;
import com.yc.edsi.payment.score.ScoreTaskPO;

@Service
public class ScoreTaskService implements IScoreTaskService {
    private static final Logger logger = LoggerFactory.getLogger(ScoreTaskService.class);
    
    @Resource
    private IScoreTaskDao scoreTaskDao;

    private Map<String, ScoreTaskPO> taskPOMap = new HashMap<String, ScoreTaskPO>();
    
    private Map<String, List<ScoreTaskPO>> taskPOMapByOpt = new HashMap<String, List<ScoreTaskPO>>();

    @PostConstruct
    private synchronized void init() {
        logger.info("开始加载翼钻任务缓存信息");
        List<ScoreTaskPO> scoreTaskList = scoreTaskDao.getScoreTaskList();
        logger.info("从数据库内取出翼钻任务共计{}条", scoreTaskList.size());
        taskPOMap.clear();
        taskPOMapByOpt.clear();
        for (ScoreTaskPO po : scoreTaskList) {
            taskPOMap.put(po.getTaskName(), po);
            if (taskPOMapByOpt.containsKey(po.getOptUri())) {
                taskPOMapByOpt.get(po.getOptUri()).add(po);
            } else {
                List<ScoreTaskPO> list = new ArrayList<ScoreTaskPO>();
                list.add(po);
                taskPOMapByOpt.put(po.getOptUri(), list);
            }
        }
        logger.info("加载翼钻任务缓存信息成功");
    }

    @Override
    public List<ScoreTaskPO> getScoreTaskList() throws EdsiException {
        return scoreTaskDao.getScoreTaskList();
    }

    @Override
    public void changeTaskScore(ScoreTaskPO scoreTaskPO) throws EdsiException {
        scoreTaskDao.changeTaskScore(scoreTaskPO);
        this.init();
    }

    @Override
    public ScoreTaskPO getScoreTask(String taskName) throws EdsiException {
        return taskPOMap.get(taskName);
    }

    @Override
    public List<ScoreTaskPO> getScoreTaskByOpt(String optUri) throws EdsiException {
        return taskPOMapByOpt.get(optUri);
    }

    @Override
    public List<ScoreTaskPO> getOwnerScoreTasks(Long ownerId, Long communityId, Integer doneStatus) throws EdsiException {
        return scoreTaskDao.getOwnerScoreTasks(ownerId, communityId, doneStatus);
    }
}
