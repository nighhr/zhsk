package com.zhonghe.crm.model.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderCreateDTO {

    @JsonProperty("id")
    private String id;
    @NotNull(message = "客户ID不能为空")
    @JsonProperty("customerId")
    @NotNull(message = "商品编码不能为空")
    private String customerId;
    @JsonProperty("customerName")
    private String customerName;
    @JsonProperty("customerAddr")
    private String customerAddr;

    @NotEmpty(message = "订单行不能为空")
    @Valid
    @JsonProperty("orderLines")
    private List<OrderLineDTO> orderLines;
    @JsonProperty("memo")
    private String memo;

    // 1,保存 2.提交
    @NotEmpty(message = "订单类型不能为空")
    @Valid
    @JsonProperty("orderStatus")
    private String orderStatus;
    @JsonProperty("orderDate")
    private Date orderDate;

    @Data
    public static class OrderLineDTO {
        @JsonProperty("id")
        private String id;
        @NotNull(message = "商品编码不能为空")
        @JsonProperty("itemCode")
        private String itemCode;
        @JsonProperty("itemFullName")
        private String itemFullName;
        @JsonProperty("itemAbbrName")
        private String itemAbbrName;
        @JsonProperty("imgUrl")
        private String imgUrl;

        @NotNull(message = "价格不能为空")
        @JsonProperty("price")
        private BigDecimal price;
        @NotNull(message = "数量不能为空")
        @JsonProperty("lineQty")
        private BigDecimal lineQty;
        @JsonProperty("memo")
        private String memo;
    }
}