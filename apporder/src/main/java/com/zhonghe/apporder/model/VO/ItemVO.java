package com.zhonghe.apporder.model.VO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemVO {
    private String id;
    private String itemCode;
    private String itemFullName;
    private String itemAbbreviation;
    private String itemImg;
    private String uomName;
    private BigDecimal salesPrice;
    private String icon = "goods-collect-o";
    private String quantity = "0";
    private String total = "0";
    private String salePrice = "0";
}
