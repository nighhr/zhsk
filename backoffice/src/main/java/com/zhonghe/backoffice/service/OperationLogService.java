package com.zhonghe.backoffice.service;

import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.kernel.vo.PageResult;

import java.util.Map;

public interface OperationLogService {
    PageResult<OperationLog> getOperationLogList(Map<String, Object> params);
}
