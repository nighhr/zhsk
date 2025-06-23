package com.zhonghe.backoffice.model.enums;

import lombok.Data;

@Data
public class SuppEnum {
    public enum SupplierStatus {
        ENABLED(1, "启用"),
        DISABLED(2, "暂停");

        private final int code;
        private final String desc;

        SupplierStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }

    public enum SupplierDataType {
        INTERNAL("internal", "内部"),
        EXTERNAL("external", "外部");

        private final String code;
        private final String desc;

        SupplierDataType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }

    public enum SupplierBusinessCategory {
        SELL("SELL", "经销"),
        AGENCY("AGENCY", "代销"),
        POOL("POOL", "联营");

        private final String code;
        private final String desc;

        SupplierBusinessCategory(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
