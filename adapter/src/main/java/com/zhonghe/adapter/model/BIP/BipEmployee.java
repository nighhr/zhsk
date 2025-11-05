package com.zhonghe.adapter.model.BIP;

import lombok.Data;
import java.util.Date;

@Data
public class BipEmployee {
    private Long id;
    private String code;
    private String dingdingId;
    private String name;
    private String mobile;
    private Integer enablestate;
    private String pkPsndoc;
    private String pkGroupCode;
    private String pkGroupName;
    private String pkGroupPk;
    private String pkOrgCode;
    private String pkOrgName;
    private String pkOrgPk;
    private String deptCode;
    private String deptName;
    private String jobCode;
    private String jobName;
    private String postCode;
    private String postName;
    private String creatorCode;
    private String creatorName;
    private Date creationTime;
    private Date ts;
    private Integer dr;
    private Integer dataoriginflag;
    private String isshopassist;
}
