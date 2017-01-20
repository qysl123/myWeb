package com.zk.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import sun.util.calendar.CalendarUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ken on 2016/11/18.
 */
public class ScoreExecute {


    /**
     * 执行单人事件 积分变更
     *
     * @param accountId       用户账户id
     * @param scoreExecuteDTO 变更参数
     * @return 结果
     */
    public ScoreResponseDTO execute(Long accountId, ScoreExecuteDTO scoreExecuteDTO) {
        return new ScoreResponseDTO(true);
    }


}
