package com.zk.concurrent;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 积分系统执行相关服务
 * Created by Ken on 2016/11/14.
 */
public class ScoreService {

    private final ExecutorService executor;

    public ScoreService(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * 执行事件 积分变更
     *
     * @param scoreExecuteDTO 积分变更参数
     * @return Map<Long.ScoreResponseDTO> 用户id, 结果
     */
    public Map<Long, ScoreResponseDTO> execute(final ScoreExecuteDTO scoreExecuteDTO) {
        List<Long> accountList = scoreExecuteDTO.getAccountId();
        if (CollectionUtils.isEmpty(accountList)) {
            return new HashMap<>();
        }

        Map<Long, ScoreResponseDTO> resultMap = new HashMap<>();
        CompletionService<ScoreResponseDTO> completionService = new ExecutorCompletionService<>(executor);
        for (final Long accountId : accountList) {
            final ScoreExecute scoreExecute = new ScoreExecute();
            completionService.submit(new Callable<ScoreResponseDTO>() {
                @Override
                public ScoreResponseDTO call() throws Exception {
                    return scoreExecute.execute(accountId, scoreExecuteDTO);
                }
            });
        }

        try {
            for (Long accountId : accountList) {
                Future<ScoreResponseDTO> f = completionService.take();
                ScoreResponseDTO response = f.get();
                resultMap.put(accountId, response);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }

        return resultMap;
    }

}
