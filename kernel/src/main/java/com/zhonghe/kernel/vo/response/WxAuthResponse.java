package com.zhonghe.kernel.vo.response;

import lombok.Data;

@Data
public class WxAuthResponse {
    private String openid;
    private String session_key;
}