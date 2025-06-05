package com.zhonghe.adapter.model;

import lombok.Data;

@Data
public class FOrderLine {
    private Long ID;
    private String FID;
    private String FEntryID;
    private String FMaterialID;
    private String FMaterialNumber;
    private String FMaterialName;
    private String FUnit;
    private Double FInQty;
    private Double FQty;
    private Double FGiftQty;
    private String FBatch;
    private String FStockID;
    private Object FStockNumber;
    private Object FStockName;
    private Double FRate;
    private Double FTaxPrice;
    private Double FAllAmount;
    private Object FNote;
    private String FOrderEntryID;


}