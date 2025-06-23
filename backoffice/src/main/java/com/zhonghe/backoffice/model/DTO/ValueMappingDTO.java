package com.zhonghe.backoffice.model.DTO;

import lombok.Data;

@Data
public class ValueMappingDTO {
    private Integer id;

    private Integer columnMappingId;

    private String sourceValue;

    private String targetValue;

    private String creator;
    private String updater;
}