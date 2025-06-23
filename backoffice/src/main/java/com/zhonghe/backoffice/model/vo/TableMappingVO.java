package com.zhonghe.backoffice.model.vo;

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
    private Date createTime;
    private String updater;
    private Date updateTime;

}