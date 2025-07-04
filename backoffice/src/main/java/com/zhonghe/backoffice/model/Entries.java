package com.zhonghe.backoffice.model;

import lombok.Data;

@Data
public class Entries {
    private Long id; // 自增主键
    private String summary; // 摘要
    private String direction; // 借贷方向
    private String amount; // 本位币金额
    private Boolean supplierRelated; // 供应商往来
    private Boolean departmentAccounting; // 部门核算
}