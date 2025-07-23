package com.zhonghe.adapter.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceCardLine {
    private Long ID;
    private String FID;
    private String FEntryID;
    private String FCardTypeID;
    private String FCardTypeName;
    private Double FPrice;
    private Double FQty;
    private Double FTotalPrice;
}