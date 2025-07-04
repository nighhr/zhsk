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

    private List<SaleLine> entries;
}