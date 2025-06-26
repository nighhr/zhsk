package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zhonghe.kernel.model.DateTimeDeserializer;
import lombok.Data;

import java.util.Date;

// 数据源实体
@Data
public class DbConnection {
    private Long id;
    private String connectionName; //数据源名称
    private String connectionType; //数据源类型
    private String dbHost;  //数据源路径
    private Integer dbPort; //数据源端口
    private String dbName;  //数据库名称
    private String username; // 用户名
    private String password; // 密码
    private String charset; //字符集
    private String creator;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    private String updater;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
    private Boolean isDeleted;

}