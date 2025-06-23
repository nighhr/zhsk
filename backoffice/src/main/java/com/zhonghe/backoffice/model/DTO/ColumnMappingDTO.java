package com.zhonghe.backoffice.model.DTO;

import lombok.Data;

@Data
public class ColumnMappingDTO {
    private Integer id;
    private Integer tableMappingId;
    private String sourceColumnName;
    private String targetColumnName;
    private String description;
}