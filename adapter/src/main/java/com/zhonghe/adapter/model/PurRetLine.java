package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurRetLine {
        private Long id; // 自增主键
        private String FID; // 主表ID
        private String FEntryID; // 明细ID
        private String FMaterialID; // 物料ID
        private String FMaterialNumber; // 物料编码
        private String FMaterialName; // 物料名称
        private String FMaterialTypeNumber; // 物料分类编码
        private String FMaterialTypeName; // 物料分类名称
        private String FUnit; // 单位
        private BigDecimal FQty; // 退货数量
        private String FBatch; // 退货批号
        private String FStockID; // 退货仓库ID
        private String FStockNumber; // 退货仓库编码
        private String FStockName; // 退货仓库名称
        private BigDecimal FRate; // 税率(%)
        private BigDecimal FTaxPrice; // 含税单价
        private BigDecimal FAllAmount; // 含税金额
        private String FNote; // 备注
}
