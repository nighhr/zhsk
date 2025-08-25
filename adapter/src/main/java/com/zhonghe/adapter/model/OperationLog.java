package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;

@Data
public class OperationLog {
    private Long id;
    private Long taskId;
    private String taskName;
    private String primaryKeyValue;
    private Date logTime;
    private String status; // "成功" or "失败"
    private String inputDetail;
    private String logDetail;
}
