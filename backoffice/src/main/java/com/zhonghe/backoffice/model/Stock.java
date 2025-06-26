package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zhonghe.kernel.model.DateTimeDeserializer;
import lombok.Data;
import java.util.Date;

@Data
public class Stock {

    @JsonProperty("id")
    private Long id; // id
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
    private String parentIds; // 所有上级机构id(格式如: ,1,2,3,)

    @JsonProperty("create_date")
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createDate; // 创建时间

    @JsonProperty("update_date")
    @JsonDeserialize(using = DateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateDate; // 更新时间
}