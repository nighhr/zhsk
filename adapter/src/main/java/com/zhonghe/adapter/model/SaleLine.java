package com.zhonghe.adapter.model;

import lombok.Data;

@Data
public class SaleLine {
    private Long id;
    private String FID;
    private Integer FEntryID;
    private String FMaterialID;
    private String FMaterialNumber;
    private String FMaterialName;
    private String FMaterialTypeNumber;
    private String FMaterialTypeName;
    private String FUnit;
    private Double FSaleNum;
    private Double FSalesPrice;
    private Double FDealPrice;
    private Double FRate;
    private Double FDiscount;
    private Double FAllAmount;
    private Double FStockPrice;
    private String FStockID;
    private String FStockNumber;
    private String FStockName;
    private String FBatch;
    private String FPayWay;
    private String FPayWayName;
    private Double FPayMoney;
}
