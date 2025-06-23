package com.zhonghe.backoffice.model;

import lombok.Data;

import java.util.Date;

// 数据源实体
@Data
public class DataSource {
    private Integer id;
    private String name;
    private String type;
    private String url;
    private String username;
    private String password;
    private String driverClass;
    private Date createTime;
    private Date updateTime;
}