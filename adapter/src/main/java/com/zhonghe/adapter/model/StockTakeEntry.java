package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockTakeEntry {
    private Long id; // 自增主键
    private String FID; // 主表ID
    private String FEntryID; // 盘点明细ID
    private String FMaterialID; // 物料ID
    private String FMaterialNumber; // 物料编码
    private String FMaterialName; // 物料名称
    private String FMaterialTypeNumber; // 物料分类编码
    private String FMaterialTypeName; // 物料分类名称
    private String FUnit; // 单位
    private BigDecimal FZFQty; // 盈亏数量
    private BigDecimal FQty; // 实际数量
    private BigDecimal FYkQty; // 盘点差异数
    private BigDecimal FYkPrice; // 盘盈盘亏成本
    private BigDecimal FYkAmount; // 盘点差异成本金额
    private BigDecimal FTzQty; // 调整数量
    private BigDecimal FTzPrice; // 调整成本单价
    private BigDecimal FTzAmount; // 调整成本金额
    private String FStockID; // 仓库ID
    private String FStockNumber; // 仓库编码
    private String FStockName; // 仓库名称
    private String FBatch; // 批号
    private String FNote; // 备注
}