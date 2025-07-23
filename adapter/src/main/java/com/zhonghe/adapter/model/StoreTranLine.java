package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreTranLine {
    private Long ID;
    private String FID;
    private String FEntryID;
    private String FDBType; // 调出 / 调入

    private String FMaterialID;
    private String FMaterialNumber;
    private String FMaterialName;

    private String FMaterialTypeNumber;
    private String FMaterialTypeName;

    private String FUnit;
    private Double FQty;

    private String FOutStockID;
    private String FOutStockNumber;
    private String FOutStockName;

    private String FInStockID;
    private String FInStockNumber;
    private String FInStockName;

    private String FBatch;

    private Double FRate;
    private Double FInPrice;
    private Double FInAmount;

    private String FNote;
}