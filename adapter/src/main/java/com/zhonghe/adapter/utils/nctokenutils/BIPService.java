package com.zhonghe.adapter.utils.nctokenutils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * API 服务类
 * 用于调用 NC 云 API 接口
 */
@Service
public class BIPService {

    @Value("${bip.config.ip}")
    private String ip;

    @Value("${bip.config.port}")
    private String port;

    @Value("${bip.config.biz-center}")
    private String bizCenter;

    @Value("${bip.config.secret-level}")
    private String secretLevel;

    @Value("${bip.config.client-id}")
    private String clientId;

    @Value("${bip.config.client-secret}")
    private String clientSecret;

    @Value("${bip.config.pub-key}")
    private String pubKey;

    @Value("${bip.config.user-name}")
    private String userName;

    @Value("${bip.config.pwd}")
    private String password;

    @Value("${bip.config.tenant-id}")
    private String tenantId;

    @Value("${bip.config.grant-type}")
    private String grantType;

    private APICurUtils apiUtil;

    /**
     * 初始化 API 工具
     */
    private void initApiUtil() {
        if (apiUtil == null) {
            apiUtil = new APICurUtils();
            // 清除 pubKey 中的空格
            String cleanPubKey = pubKey.replaceAll("\\s+", "");
            apiUtil.init(ip, port, bizCenter, clientId, clientSecret, cleanPubKey, userName, password, tenantId, secretLevel);
            apiUtil.setGrant_type(grantType);
        }
    }

    /**
     * 获取 Token
     *
     * @return token 字符串
     * @throws Exception 异常信息
     */
    public String getToken() throws Exception {
        initApiUtil();
        return apiUtil.getToken();
    }

    /**
     * 调用 API 接口
     *
     * @param apiUrl API 接口 URL（相对路径）
     * @param requestJson 请求的 JSON 参数
     * @return API 返回结果
     * @throws Exception 异常信息
     */
    public String callApi(String apiUrl, String requestJson) throws Exception {
        return callApi(apiUrl, requestJson, null);
    }

    /**
     * 调用 API 接口（带 token）
     *
     * @param apiUrl API 接口 URL（相对路径）
     * @param requestJson 请求的 JSON 参数
     * @param token 授权 token，如果为 null 则自动获取
     * @return API 返回结果
     * @throws Exception 异常信息
     */
    public String callApi(String apiUrl, String requestJson, String token) throws Exception {
        initApiUtil();

        if (token == null) {
            token = getToken();
        }

        apiUtil.setApiUrl(apiUrl);
        return apiUtil.getAPIRetrun(token, requestJson);
    }



}
