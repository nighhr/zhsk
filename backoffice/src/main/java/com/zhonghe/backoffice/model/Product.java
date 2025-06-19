package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Product {

    @JsonProperty("hcid")
    private String HCid; // 商品id

    @JsonProperty("code")
    private String code; // 编号

    @JsonProperty("name")
    private String name; // 名称

    @JsonProperty("barCode")
    private String barCode; // 条码

    @JsonProperty("providerNo")
    private String providerNo; // 供应商

    @JsonProperty("providerNumber")
    private String providerNumber; // 供应商编码

    @JsonProperty("providerName")
    private String providerName; // 供应商名称

    @JsonProperty("brandId")
    private String brandId; // 品牌

    @JsonProperty("brandNumber")
    private String brandNumber; // 品牌编码

    @JsonProperty("brandName")
    private String brandName; // 品牌名称

    @JsonProperty("producer")
    private String producer; // 产地

    @JsonProperty("modalId")
    private String modalId; // 规格

    @JsonProperty("kindId")
    private String kindId; // 分类

    @JsonProperty("kindName")
    private String kindName; // 分类名称

    @JsonProperty("kindNumber")
    private String kindNumber; // 分类编码

    @JsonProperty("unit")
    private String unit; // 单位

    @JsonProperty("mediumUnit")
    private String mediumUnit; // 中等单位

    @JsonProperty("maxUnit")
    private String maxUnit; // 最大单位

    @JsonProperty("mediumUnitNum")
    private Integer mediumUnitNum; // 商品中等单位件含量

    @JsonProperty("maxUnitNum")
    private Integer maxUnitNum; // 商品最大单位件含量

    @JsonProperty("company")
    private String company; // 所属公司

    @JsonProperty("companyName")
    private String companyName; // 所属公司名称

    @JsonProperty("companyNumber")
    private String companyNumber; // 所属公司编码

    @JsonProperty("createDate")
    private Date createDate; // 创建时间

    @JsonProperty("updateDate")
    private Date updateDate; // 更新时间

    @JsonProperty("deleted")
    private Boolean deleted;
}
