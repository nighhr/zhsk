package com.zhonghe.apporder.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Item {
    private String id;
    private Long tenantId;
    private String itemCode;
    private String itemFullName;
    private String itemAbbreviation;
    private String itemImg;
    private String uomId;
    private BigDecimal salesPrice;
    private Boolean deleted;
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;
}