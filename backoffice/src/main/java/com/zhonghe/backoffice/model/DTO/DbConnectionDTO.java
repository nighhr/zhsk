package com.zhonghe.backoffice.model.DTO;

import lombok.Data;

@Data
public class DbConnectionDTO {
    private String connectionName;

    private String dbName;

    private String connectionType;

    private Integer page = 1;

    private Integer pageSize = 10;
}
