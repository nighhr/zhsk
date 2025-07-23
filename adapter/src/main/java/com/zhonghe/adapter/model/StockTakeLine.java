package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockTakeLine {
    private Long ID;
    private String FID;
    private String FEntryID;
    private String FMaterialID;
    private String FMaterialNumber;
    private String FMaterialName;
    private String FMaterialTypeNumber;
    private String FMaterialTypeName;
    private String FUnit;
    private Double FYkQty;
    private Double FYkPrice;
    private Double FYkAmount;
    private Double FTzQty;
    private Double FTzPrice;
    private Double FTzAmount;
    private Double FCGPrice;
    private String FStockID;
    private String FStockNumber;
    private String FStockName;
    private String FBatch;
    private String FNote;
    private Double FZFQty;
    private Double FQty;
}