package com.zhonghe.apporder.model;

import com.zhonghe.kernel.model.BaseEditModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class SalesOrderLine extends BaseEditModel {


    /**
     * 订单ID
     */
    private String orderId;


    /**
     * 产品图URL
     */
    private String imgUrl;

    /**
     * 商品编码
     */
    private String itemCode;

    /**
     * 商品全称
     */
    private String itemFullName;

    /**
     * 商品简称
     */
    private String itemAbbrName;

    /**
     * 含税销售价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private BigDecimal lineQty;

    /**
     * 含税小计
     */
    private BigDecimal lineAmount;

    /**
     * 备注
     */
    private String memo;

    /**
     * 是否已删除
     */
    private Boolean deleted;

}