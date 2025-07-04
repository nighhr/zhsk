package com.zhonghe.backoffice.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class VoucherSubject {

    private Integer id;

    private Integer ruleId;

    private String subjectCode;
    // 选中的字段名列表(英文)
    private List<String> selectedFields;

    private Map<String, String> dynamicFields; // 存储 name, sex 等

    public void addField(String fieldName, String value) {
        if (dynamicFields == null) {
            dynamicFields = new HashMap<>();
        }
        dynamicFields.put(fieldName, value);
    }
    // 创建人
    private String creator;
}
