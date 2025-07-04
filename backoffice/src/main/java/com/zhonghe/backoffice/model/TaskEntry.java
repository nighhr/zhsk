package com.zhonghe.backoffice.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TaskEntry {
    private Long id;
    private Long taskId;
    private Integer entryIndex;
    private String summary;
    private String subjectCode;
    private String direction; // DEBIT/CREDIT
    private BigDecimal amount;
    private Boolean supplierRelated;
    private Boolean departmentRelated;
    private Date createTime;
    private Date updateTime;
}