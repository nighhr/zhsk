package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ServiceCard {
    private Long ID;
    private String FID;
    private String FBillNo;
    private String FBillType;
    private Date FDate;
    private String FOrgID;
    private String FOrgNumber;
    private String FOrgName;
    private String FRemark;
    private String FPayID;
    private String FPayWay;
    private Date FPayDate;
    private Double FPayMoney;
    private String FCycleID;
    private String syncFlag;
    private Date syncTime;
    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;
    private List<ServiceCardLine> FEntry;
}