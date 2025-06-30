package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class PurRet {
        private Long id; // 自增主键
        private String FID; // 主表ID
        private String FBillType; // 单据类型
        private Integer FFactoryType; // 返厂单类型
        private String FDepID; // 门店ID
        private String FDepNumber; // 门店编码
        private String FDepName; // 门店名称
        private Date FDate; // 日期
        private String FSupplierID; // 供应商ID
        private String FSupplierNumber; // 供应商编码
        private String FSupplierName; // 供应商名称
        private Integer FReason; // 退料原因
        private String FRemark; // 备注
        private String FCreateBy; // 创建人
        private Date FCreateDate; // 创建时间
        private String FUpdateBy; // 修改人
        private Date FUpdateDate; // 修改时间

        private List<PurRetLine> entries; // 明细列表
}
