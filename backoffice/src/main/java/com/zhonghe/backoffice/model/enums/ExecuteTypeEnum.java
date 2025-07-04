package com.zhonghe.backoffice.model.enums;


import lombok.Getter;

@Getter
public enum ExecuteTypeEnum {


    MANUAL("手动执行"),
    FIXED_TIME("固定时间"),
    FIXED_INTERVAL("固定间隔");

    private final String executeType;

    ExecuteTypeEnum(String executeType) {
        this.executeType = executeType;
    }


}
