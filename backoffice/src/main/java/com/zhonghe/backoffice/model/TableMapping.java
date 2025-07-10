package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zhonghe.kernel.model.DateTimeDeserializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

// 表映射实体
@Data
public class TableMapping {
    private Integer id;
    private String ruleName; //映射名称
    private String type; // 映射类型
    private Boolean isActive;
    private String description; // 映射描述
    private String creator;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    private String updater;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    // 关联属性
    private List<ColumnMapping> columnMappings;
}