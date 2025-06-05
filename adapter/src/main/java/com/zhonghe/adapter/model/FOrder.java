package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.List;

@Data
public class FOrder {
    private Long ID;
    private String FID;
    private String FOrderNo;
    private String FDate;
    private String FDepID;
    private String FDepNumber;
    private String FDepName;
    private String FSupplierID;
    private String FSupplierNumber;
    private String FSupplierName;
    private String FRemark;
    private String FCreateBy;
    private String FCreateDate;
    private String FUpdateBy;
    private String FUpdateDate;
    private Object FBillType;
    private Object sync_flag;
    private Object sync_time;
    private List<FOrderLine> FEntry;

}