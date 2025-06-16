package com.zhonghe.crm.model.DTO;

import lombok.Data;

@Data
public class WxSessionDTO {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}