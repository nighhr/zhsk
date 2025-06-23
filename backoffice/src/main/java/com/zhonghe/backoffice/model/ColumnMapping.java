package com.zhonghe.backoffice.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

// 字段映射实体
@Data
public class ColumnMapping {
    private Integer id;
    private Integer tableMappingId;
    private String sourceColumnName;
    private String targetColumnName;
    private String description;  //字段描述
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;

    // 关联属性
    private TableMapping tableMapping;
    private List<ValueMapping> valueMappings;
}
