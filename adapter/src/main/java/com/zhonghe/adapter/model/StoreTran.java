package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StoreTran {
    private Long id; // 自增ID
    private String FID; // 调拨单ID
    private Date FDate; // 日期
    private String FOutOrgID; // 调出门店ID
    private String FOutOrgNumber; // 调出门店编码
    private String FOutOrgName; // 调出门店名称
    private String FInOrgID; // 调入门店ID
    private String FInOrgNumber; // 调入门店编码
    private String FInOrgName; // 调入门店名称
    private String FRemark; // 备注
    private String FCreateBy; // 创建人
    private Date FCreateDate; // 创建时间
    private String FUpdateBy; // 修改人
    private Date FUpdateDate; // 修改时间

    private List<StoreTranEntry> storeTranEntryList;
}