package com.zhonghe.adapter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PurRet {
        private String fid;
        private String billType;
        private Integer factoryType;
        private String depId;
        private String depNumber;
        private String depName;
        private Date date;
        private String supplierId;
        private String supplierNumber;
        private String supplierName;
        private Integer reason;
        private String remark;
        private String createBy;
        private Date createDate;
        private String updateBy;
        private Date updateDate;

        private List<PurRetLine> entries;
}
