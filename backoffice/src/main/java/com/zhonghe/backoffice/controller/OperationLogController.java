package com.zhonghe.backoffice.controller;

import com.zhonghe.adapter.model.OperationLog;
import com.zhonghe.backoffice.service.OperationLogService;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/operationLog")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public PageResult<OperationLog> getOperationLogList(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("taskName", taskName);
        params.put("status", status);
        params.put("start", start);
        params.put("end", end);
        params.put("page", page);
        params.put("pageSize", pageSize);

        return operationLogService.getOperationLogList(params);
    }
}