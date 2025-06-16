package com.zhonghe.crm.model;

import com.zhonghe.kernel.model.BaseEditModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends BaseEditModel {
    /**
     * 业务员id
     */
    private String memberId;
    /**
     * 客户编码
     */
    private String customerCode;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 客户类型
     */
    private String customerType;
    /**
     * 手机
     */
    private String mobile;
    /**
     *  客户地址
     */
    private String customerAddress;
}
