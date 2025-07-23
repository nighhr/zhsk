package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StockTake {
    private Long ID;
    private String FID;
    private String FBillType;
    private Date FDate;
    private String FOrgID;
    private String FOrgNumber;
    private String FOrgName;
    private String FRemark;
    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;
    private String syncFlag;
    private Date syncTime;

    private List<StockTakeLine> FEntry;
}