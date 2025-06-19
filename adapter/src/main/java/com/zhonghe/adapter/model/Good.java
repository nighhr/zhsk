package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;

@Data
public class Good {
    private String HCid;          // 商品id
    private String code;          // 编号
    private String name;          // 名称
    private String barCode;       // 条码
    private String providerNo;    // 供应商
    private String providerNumber; // 供应商编码
    private String providerName;  // 供应商名称
    private String brandId;       // 品牌
    private String brandNumber;   // 品牌编码
    private String brandName;     // 品牌名称
    private String producer;      // 产地
    private String modalId;       // 规格
    private String kindId;        // 分类
    private String kindName;      // 分类名称
    private String kindNumber;    // 分类编码
    private String unit;          // 单位
    private String mediumUnit;    // 中等单位
    private String maxUnit;       // 最大单位
    private Integer mediumUnitNum; // 商品中等单位件含量
    private Integer maxUnitNum;    // 商品最大单位件含量
    private String company;       // 所属公司
    private String companyName;   // 所属公司名称
    private String companyNumber; // 所属公司编码
    private Date createDate;      // 创建时间
    private Date updateDate;      // 更新时间


}
