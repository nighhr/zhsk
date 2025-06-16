package com.zhonghe.crm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhonghe.kernel.model.BaseEditModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SalesOrder extends BaseEditModel {
    /**
     * 用户id
     */
    private String memberId;
    /**
     * 用户名称
     */
    private String memberName;
    /**
     * 客户id
     */
    private String customerId;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 客户地址
     */
    private String customerAddr;
    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date orderDate;
    /**
     * 订单状态（1.保存2.提交3.审核中4.审核通过）
     */
    private int orderStatus;
    /**
     * 订单金额总计
     */
    private String totalAmount;
    /**
     * 备注
     */
    private String memo;
    /**
     * 是否删除
     */
    private String deleted;
}
