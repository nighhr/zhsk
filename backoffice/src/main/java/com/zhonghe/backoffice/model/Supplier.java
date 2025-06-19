package com.zhonghe.backoffice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class Supplier {
    @JsonProperty("id")
    private Long id; // id

    @JsonProperty("hc_id")
    private Long HCid; // 供应商id

    @JsonProperty("no")
    private String no; // 编码

    @JsonProperty("name")
    private String name; // 名称

    @JsonProperty("status")
    private Integer status; // 状态:1->启用,2->暂停

    @JsonProperty("data_type")
    private String dataType; // 类型:internal 内部, external:外部

    @JsonProperty("business_category")
    private String businessCategory; // 经营类别:SELL->经销,AGENCY->代销,POOL->联营

    @JsonProperty("create_date")
    private Date createDate; // 创建时间

    @JsonProperty("update_date")
    private Date updateDate; // 更新时间
}
