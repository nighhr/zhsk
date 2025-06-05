package com.zhonghe.apporder.model;

import com.zhonghe.kernel.model.BaseEditModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User extends BaseEditModel {

    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 工号
     */
    private String userCode;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 手机号
     */
    private String mobileNo;
    /**
     * 部门编码
     */
    private String departmentId;
    /**
     * 部门名称
     */
    private String departmentName;
    /**
     * 备注
     */
    private String desc;
    /**
     * 是否在职
     */
    private Boolean enabled;
    /**
     * 上次登录时间
     */
    private Date lastLoginTime;
    /**
     * 是否删除
     */
    private Boolean deleted;
    /**
     * 上级
     */
    private String parents;
    /**
     * 用户在微信开放平台的unionId
     */
    private String unionId;
    /**
     * 用户在微信服务号的openId
     */
    private String openId;
}
