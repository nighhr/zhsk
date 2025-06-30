package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreTranEntry {
    private Long id; // 自增ID
    private String FID; // 主表ID
    private String FEntryID; // 明细ID
    private String FDBType; // 调拨类型
    private String FMaterialID; // 物料编码ID
    private String FMaterialNumber; // 物料编码
    private String FMaterialName; // 物料名称
    private String FMaterialTypeNumber; // 物料分类编码
    private String FMaterialTypeName; // 物料分类名称
    private String FUnit; // 单位
    private BigDecimal FQty; // 调拨数量
    private String FOutStockID; // 调出仓库ID
    private String FOutStockNumber; // 调出仓库编码
    private String FOutStockName; // 调出仓库名称
    private String FInStockID; // 调入仓库ID
    private String FInStockNumber; // 调入仓库编码
    private String FInStockName; // 调入仓库名称
    private String FBatch; // 批号
    private BigDecimal FRate; // 税率
    private BigDecimal FInPrice; // 调入单价
    private BigDecimal FInAmount; // 调入金额
    private String FNote; // 备注
}