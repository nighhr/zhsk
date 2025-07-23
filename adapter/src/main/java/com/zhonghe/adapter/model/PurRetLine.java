package com.zhonghe.adapter.model;

import lombok.Data;

@Data
public class PurRetLine {
        private Long id; // 明细ID
        private String FID; // 主表ID
        private String FEntryID; // 明细行ID
        private String FMaterialID; // 物料ID
        private String FMaterialNumber; // 物料编码
        private String FMaterialName; // 物料名称
        private String FMaterialTypeNumber; // 物料分类编码
        private String FMaterialTypeName; // 物料分类名称
        private String FUnit; // 单位
        private String FQty; // 数量
        private String FBatch; // 批号
        private String FStockID; // 库存ID
        private String FStockNumber; // 库存编码
        private String FStockName; // 库存名称
        private Double FRate; // 税率
        private Double FTaxPrice; // 含税单价
        private Double FAllAmount; // 含税金额
        private String FNote; // 备注
}
