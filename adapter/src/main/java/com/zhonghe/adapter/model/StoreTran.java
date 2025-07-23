package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StoreTran {
    private Long ID;
    private String FID;
    private Date FDate;

    private String FOutOrgID;
    private String FOutOrgNumber;
    private String FOutOrgName;

    private String FInOrgID;
    private String FInOrgNumber;
    private String FInOrgName;

    private String FRemark;

    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;

    private String syncFlag;
    private Date syncTime;

    private List<StoreTranLine> FEntry;
}