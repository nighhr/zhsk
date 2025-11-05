package com.zhonghe.adapter.utils.dingtokentuils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkProperties {
    private String appKey;
    private String appSecret;
    private String serverUrl = "https://oapi.dingtalk.com";
    private Integer timeout = 5000;
}