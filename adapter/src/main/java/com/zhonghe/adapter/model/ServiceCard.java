package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ServiceCard {
    private Long id; // 自增主键
    private String FID; // 服务卡销售ID
    private String FBillNo; // 单据编号
    private String FBillType; // 销售类型（售卡、退卡）
    private Date FDate; // 业务日期
    private String FOrgID; // 门店ID
    private String FOrgNumber; // 门店编码
    private String FOrgName; // 门店名称
    private String FRemark; // 单据备注
    private String FCreateBy; // 创建人
    private Date FCreateDate; // 创建时间
    private String FUpdateBy; // 修改人
    private Date FUpdateDate; // 修改时间
    private String FPayID; // 支付明细ID
    private String FPayWay; // 支付方式
    private Date FPayDate; // 支付时间
    private BigDecimal FPayMoney; // 支付金额
    private String FCycleID; // 缴款单号

    private List<ServiceCardEntry> serviceCardEntrylist;
}