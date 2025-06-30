package com.zhonghe.adapter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurRetLine {
        private String fid;
        private Integer entryId;
        private String materialId;
        private String materialNumber;
        private String materialName;
        private String unit;
        private BigDecimal qty;
        private String batch;
        private String stockId;
        private String stockNumber;
        private String stockName;
        private BigDecimal rate;
        private BigDecimal taxPrice;
        private BigDecimal allAmount;
        private String note;
}
