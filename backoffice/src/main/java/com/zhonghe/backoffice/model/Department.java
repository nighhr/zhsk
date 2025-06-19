package com.zhonghe.backoffice.model;


import lombok.Data;

import java.util.Date;

@Data
public class Department {
    private Long HCid; // 部门id
    private String code; // 编码
    private String name; // 名称
    private Long areaId; // 区域
    private String type; // 类型
    private Long parentId; // 上级机构id
    private String parentIds; // 所有上级机构id
    private Date createDate; // 创建时间
    private Date updateDate; // 更新时间
}