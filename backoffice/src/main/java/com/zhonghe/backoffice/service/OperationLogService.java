package com.zhonghe.backoffice.service;

import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OperationLogService {
    PageResult<OperationLog> getOperationLogList(Map<String, Object> params);

    @Async // 关键注解：使方法异步执行
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void asyncRecordSuccessLog(Long taskId, String taskName, String voucherKey,
                               List<GLAccvouch> batch, String message);

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void recordFailureLog(Long taskId, String taskName, String voucherKey,
                          Map<String, Object> params, String errorDetail);

}
