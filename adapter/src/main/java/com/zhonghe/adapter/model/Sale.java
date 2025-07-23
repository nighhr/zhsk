package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Sale {
    private Long id;
    private String FID;
    private String FBillNo;
    private Integer FSalesType;
    private Date FDate;
    // 1:直营门店, 2:加盟门店, 6:测试门店, 9:物流中心
    private String FOrgType;
    private String FOrgID;
    private String FOrgNumber;
    private String FOrgName;
    private String FGuideID;
    private String FGuideNumber;
    private String FGuideName;
    private String FRemark;
    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;
    private Boolean mark = false;
    private Boolean syncFlag;
    private Date syncTime;

    private List<SaleLine> FEntry;
}
