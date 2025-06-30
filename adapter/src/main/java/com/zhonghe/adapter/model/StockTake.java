package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StockTake {
    private Long id; // 自增主键
    private String FID; // 盘点单ID
    private Integer FBillType; // 盘点类型
    private Date FDate; // 日期
    private String FOrgID; // 盘点门店ID
    private String FOrgNumber; // 盘点门店编码
    private String FOrgName; // 盘点门店名称
    private String FRemark; // 备注
    private String FCreateBy; // 创建人
    private Date FCreateDate; // 创建时间
    private String FUpdateBy; // 修改人
    private Date FUpdateDate; // 修改时间

    private List<StockTakeEntry> stockTakeEntryList;
}