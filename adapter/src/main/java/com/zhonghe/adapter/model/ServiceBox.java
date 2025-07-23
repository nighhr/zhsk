package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;

@Data
public class ServiceBox {
    private Long ID;
    private String FID;
    private String FBillNo;
    private String FOrgID;
    private String FOrgNumber;
    private String FOrgName;
    private Date FDate;
    private String FBillNoOut;
    private Double FTisQty;
    private Double FQty;
    private Double FPrice;
    private Double FTotalPrice;
    private String FCard;
    private String FMaterialID;
    private String FMaterialNumber;
    private String FMaterialName;
    private String FMaterialTypeNumber;
    private String FMaterialTypeName;
    private String FUnit;
    private String FSaleBillNo;
    private String FCreateBy;
    private Date FCreateDate;
    private String FUpdateBy;
    private Date FUpdateDate;
}
