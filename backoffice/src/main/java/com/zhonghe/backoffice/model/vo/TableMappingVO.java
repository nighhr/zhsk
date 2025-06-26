package com.zhonghe.backoffice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zhonghe.kernel.model.DateTimeDeserializer;
import lombok.Data;

import java.util.Date;

@Data
public class TableMappingVO {
    private Integer id;
    private String ruleName;
    private String type;
    private Boolean isActive;
    private String description;
    private String creator;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    private String updater;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}