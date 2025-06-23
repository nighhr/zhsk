package com.zhonghe.backoffice.model;

import lombok.Data;

import java.util.Date;

// 值映射实体
@Data
public class ValueMapping {
    private Integer id;
    private Integer columnMappingId;
    private String sourceValue;
    private String targetValue;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;

    // 关联属性
    private ColumnMapping columnMapping;
}