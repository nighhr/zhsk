package com.zhonghe.backoffice.model.DTO;

import lombok.Data;

@Data
public class TableMappingDTO {
    private String ruleName;    // 映射名称（模糊查询）
    private String description; // 映射描述（模糊查询）
    private String type;        // 类型（精确匹配）
    private Integer pageNum = 1;   // 当前页码，默认第1页
    private Integer pageSize = 10; // 每页条数，默认10条
}