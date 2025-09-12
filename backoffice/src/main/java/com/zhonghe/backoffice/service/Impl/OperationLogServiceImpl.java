package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.adapter.mapper.AT.OperationLogMapper;
import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.adapter.model.U8.GLAccvouch;
import com.zhonghe.backoffice.service.OperationLogService;
import com.zhonghe.kernel.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;


    @Override
    public PageResult<OperationLog> getOperationLogList(Map<String, Object> params) {
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());
        int offset = (page - 1) * pageSize;

        params.put("offset", offset);
        params.put("pageSize", pageSize);

        List<OperationLog> logList = operationLogMapper.selectOperationLogList(params);
        long total = operationLogMapper.selectOperationLogCount(params);

        return new PageResult<>(
                logList,
                total,
                page,
                pageSize
        );
    }

    /**
     * 异步记录成功日志 - 批次成功（异步+独立事务）
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void asyncRecordSuccessLog(Long taskId, String taskName, String voucherKey,
                                      List<GLAccvouch> batch, String message) {
        try {
            OperationLog logs = buildOperationLog(taskId, taskName, voucherKey, "成功",
                    "批次大小: " + (batch != null ? batch.size() : 0), message);
            operationLogMapper.insert(logs);
            log.info("异步日志记录成功: {}", message);
        } catch (Exception e) {
            // 异步日志记录失败，只记录错误，不影响主流程
            log.error("异步记录日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 独立事务记录日志 - 失败状态
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void recordFailureLog(Long taskId, String taskName, String voucherKey,
                                 Map<String, Object> params, String errorDetail) {
        OperationLog log = new OperationLog();
        log.setTaskId(taskId);
        log.setTaskName(taskName);
        log.setPrimaryKeyValue(voucherKey);
        log.setStatus("失败");
        log.setInputDetail(params != null ? params.toString() : "无参数");
        log.setLogDetail(errorDetail.length()>60000?errorDetail.substring(0,60000):errorDetail);
        log.setLogTime(new Date());
        operationLogMapper.insert(log);
    }
    /**
     * 构建日志对象的辅助方法
     */
    private OperationLog buildOperationLog(Long taskId, String taskName, String voucherKey,
                                           String status, String inputDetail, String logDetail) {
        OperationLog log = new OperationLog();
        log.setTaskId(taskId);
        log.setTaskName(taskName);
        log.setPrimaryKeyValue(voucherKey);
        log.setStatus(status);
        log.setInputDetail(inputDetail);
        log.setLogDetail(logDetail.length()>60000?logDetail.substring(0,60000):logDetail);
        log.setLogTime(new Date()); // 手动设置时间，确保准确性
        return log;
    }
}
