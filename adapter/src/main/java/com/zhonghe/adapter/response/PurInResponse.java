package com.zhonghe.adapter.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhonghe.adapter.model.FOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurInResponse {
    private String OFlag;
    private String Message;
    private Integer CountPage;
    private List<FOrder> Data;

}