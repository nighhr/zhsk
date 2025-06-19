package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class Department {
    @JsonProperty("hc_id")
    private Long HCid; // 部门id

    @JsonProperty("code")
    private String code; // 编码

    @JsonProperty("name")
    private String name; // 名称

    @JsonProperty("area_id")
    private Long areaId; // 区域

    @JsonProperty("type")
    private String type; // 类型

    @JsonProperty("parent_id")
    private Long parentId; // 上级机构id

    @JsonProperty("parent_ids")
    private String parentIds; // 所有上级机构id

    @JsonProperty("create_date")
    private Date createDate; // 创建时间

    @JsonProperty("update_date")
    private Date updateDate; // 更新时间
}