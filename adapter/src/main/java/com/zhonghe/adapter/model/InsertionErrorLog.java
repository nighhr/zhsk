package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;

@Data
public class InsertionErrorLog {
    private Long id;
    private String errorMessage;
    private String stackTrace;
    private Long taskId;
    private String fieldA;  // 特定字段A
    private Date errorTime;
    private String recordData;  // 可选：存储整个记录数据

    public String error(String fieldA,String errorMessage) {
        return String.format("未保存的错误日志: fieldA=%s, error=%s",
                fieldA != null ? fieldA : "null",
                errorMessage != null ? errorMessage : "null");
    }

}
