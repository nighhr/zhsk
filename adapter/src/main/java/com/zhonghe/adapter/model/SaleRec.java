package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;

@Data
public class SaleRec {
    private Integer ID;
    private String FID;
    private String FBillNo;
    private String FSalesNo;
    private String FOrgID;
    private String FOrgNumber;
    private String FOrgName;
    private Date FDate;
    private String FSaleType;
    private String FSaleTypeName;
    private String FSrcEntryID;
    private String FSetType;
    private String FSetTypeName;
    private String FMemberID;
    private Double FPayMoney;
    private String FRemark;
    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;
    private String sync_flag;
    private Date sync_time;
}