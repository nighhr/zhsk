package com.zhonghe.adapter.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceCardEntry {
    private Long id; // 自增主键
    private String FID; // 主表ID
    private String FEntryID; // 服务卡销售明细ID
    private String FCardTypeID; // 服务卡号
    private String FCardTypeName; // 服务卡名称
    private BigDecimal FPrice; // 成交价
    private Integer FQty; // 数量
    private BigDecimal FTotalPrice; // 总额
}