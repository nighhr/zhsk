package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zhonghe.kernel.model.DateTimeDeserializer;
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
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    private String updater;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    // 关联属性
    private TableMapping tableMapping;
    private List<ValueMapping> valueMappings;
}
