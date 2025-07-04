package com.zhonghe.backoffice.model;

import lombok.Data;

import java.util.Date;

@Data
public class TaskVoucherHead {
    private Long id;
    private Long taskId;
    private String dbName;  //目标数据库名称
    private String accountName; //账套名称
    private String voucherKey;  //凭证识别主键
    private String voucherWord;  //凭证字
    private String voucherDate;  // 凭证日期
    private String businessDate;  //业务日期
    private Integer attachmentCount;  //附件张数
    private String creator;
    private Date createTime;
    private Date updateTime;
}