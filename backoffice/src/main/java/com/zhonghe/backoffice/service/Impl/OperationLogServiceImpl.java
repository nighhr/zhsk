package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.adapter.mapper.AT.OperationLogMapper;
import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.backoffice.service.OperationLogService;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
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
}
