package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhonghe.backoffice.model.enums.ExecuteTypeEnum;
import com.zhonghe.backoffice.model.enums.TaskTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class Task {
    private Long id;
    private String taskName;  //任务名称
    private Long sourceDbId;  //源应用id(数据库id)
    private String sourceDb;   //源应用名称 (数据库名称)
    private String sourceTable; //源数据 (数据库指定表)
    private String detailTable; //明细表名称
    private Date startTime; //起始时间
    private Date endTime;  //结束时间
    private Long targetAppId;  //目标应用id// (数据库id)
    private String targetApp;  //目标应用 (数据库名称)
    private TaskTypeEnum taskType;    //任务类型
    private ExecuteTypeEnum executeType; // MANUAL/FIXED_TIME/FIXED_INTERVAL
    private String executeTime; // 执行时间或间隔
    private Boolean status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;
}