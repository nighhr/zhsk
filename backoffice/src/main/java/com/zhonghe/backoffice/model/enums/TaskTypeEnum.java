package com.zhonghe.backoffice.model.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    U8("U8-财务凭证");

    private final String typeName;

    TaskTypeEnum(String typeName) {
        this.typeName = typeName;
    }

}

