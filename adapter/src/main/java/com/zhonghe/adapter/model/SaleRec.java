package com.zhonghe.adapter.model;

import lombok.Data;
import java.util.Date;

@Data
public class SaleRec {
    private Long id;
    private Long fid; // 主表ID
    private String fBillNo; // 单据编码
    private String fSalesNo; // 销售单号
    private Long fOrgId; // 回款门店ID
    private String fOrgNumber; // 回款门店编码
    private String fOrgName; // 回款门店名称
    private Date fDate; // 业务日期
    private String fSaleType; // 销售类型编码
    private String fSaleTypeName; // 销售类型名称
    private Long fSrcEntryId; // 销售明细内码
    private String fSetType; // 结算方式编码
    private String fSetTypeName; // 结算方式名称
    private String fMemberId; // 会员编码
    private Double fPayMoney; // 回款金额
    private String fRemark; // 备注
    private String fCreateBy; // 创建人
    private Date fCreateDate; // 创建时间
    private String fUpdateBy; // 修改人
    private Date fUpdateDate; // 修改时间
}