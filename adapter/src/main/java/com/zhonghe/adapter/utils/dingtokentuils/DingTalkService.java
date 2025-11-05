package com.zhonghe.adapter.utils.dingtokentuils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
public class DingTalkService {

    @Autowired
    private DingTalkProperties dingTalkProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取钉钉访问令牌
     */
    public String getAccessToken() {
        try {
            String url = dingTalkProperties.getServerUrl() + "/gettoken" +
                    "?appkey=" + dingTalkProperties.getAppKey() +
                    "&appsecret=" + dingTalkProperties.getAppSecret();

            log.info("请求钉钉 token 地址: {}", url);
            ResponseEntity<TokenResponse> response = restTemplate.getForEntity(url, TokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();
                if (tokenResponse.getErrcode() == 0) {
                    log.info("成功获取钉钉访问令牌");
                    return tokenResponse.getAccessToken();
                } else {
                    log.error("获取钉钉访问令牌失败: {} - {}", tokenResponse.getErrcode(), tokenResponse.getErrmsg());
                }
            }
        } catch (Exception e) {
            log.error("获取钉钉访问令牌时发生异常", e);
        }
        return null;
    }

    /**
     * 通用钉钉接口调用方法（支持 GET/POST）
     *
     * @param apiPath   接口路径（例如 "topapi/smartwork/hrm/employee/list" 或 "gettoken"）
     * @param params    请求参数（Map格式）
     * @param method    请求方式（HttpMethod.GET / HttpMethod.POST）
     * @param needToken 是否需要自动拼接 access_token
     * @return 钉钉返回结果（JsonNode格式）
     */
    public JsonNode callDingTalkApi(String apiPath, Map<String, Object> params, HttpMethod method, boolean needToken) {
        try {
            // 构建完整URL
            String url = buildUrl(apiPath, params, method, needToken);
            System.out.println(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity;
            if (method == HttpMethod.POST) {
                entity = new HttpEntity<>(params, headers);
            } else {
                entity = new HttpEntity<>(headers);
            }

            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                log.info("钉钉接口调用成功: {}", apiPath);
                return result;
            } else {
                log.error("钉钉接口响应错误: 状态码 {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用钉钉接口失败: {}", apiPath, e);
        }
        return null;
    }

    /**
     * 拼接请求 URL
     */
    private String buildUrl(String apiPath, Map<String, Object> params, HttpMethod method, boolean needToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                dingTalkProperties.getServerUrl() + "/topapi/" + apiPath
        );

        if (needToken) {
            builder.queryParam("access_token", getAccessToken());
        }

        if (method == HttpMethod.GET && params != null) {
            params.forEach(builder::queryParam);
        }

        return builder.toUriString();
    }

    @Data
    public static class TokenResponse {
        @JsonProperty("errcode")
        private Integer errcode;

        @JsonProperty("errmsg")
        private String errmsg;

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;
    }
}
