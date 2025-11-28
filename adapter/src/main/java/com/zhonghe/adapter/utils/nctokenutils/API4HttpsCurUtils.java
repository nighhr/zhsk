package com.zhonghe.adapter.utils.nctokenutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


/**
 * HTTPS API 调用工具类
 *
 * 原使用 FastJSON，已改为 Jackson (避免 CVE 漏洞)
 */
@Slf4j
public class API4HttpsCurUtils implements IAPIUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 授权模式
     */
    private String grant_type = "client";

    private String apiUrl;

    private String secret_level = SecretConst.LEVEL1;

    private String pubKey;

    private String baseUrl;

    // 访问的nccloud系统的账套code
    private String biz_center;

    // 对应于在第三方应用注册当中的app_id
    private String client_id;

    // client_secret：对应于第三方应用注册当中的app_secret
    private String client_secret;

    private String user_name;

    private String pwd;

    private String tenant_id;
    // 超时时间
    private Integer timeout;

    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * 客户端模式获取token
     *
     * @return token 字符串
     * @throws Exception 异常
     */
    public String getTokenByClient() throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        // 密码模式认证
        paramMap.put("grant_type", "client_credentials");
        // 第三方应用id
        paramMap.put("client_id", this.client_id);
        // 第三方应用secret 公钥加密
        String encode = URLEncoder.encode(Encryption.pubEncrypt(this.pubKey, this.client_secret), OpenAPIConstants.UTF_8);
        paramMap.put("client_secret", encode);
        // 账套编码
        paramMap.put("biz_center", biz_center);
        // 传递数据源和ncc登录用户
        paramMap.put("usercode", this.user_name);

        // 签名
        String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + this.pubKey, this.pubKey);
        paramMap.put("signature", sign);

        String url = this.baseUrl + "nccloud/opm/accesstoken";
        String mediaType = "application/x-www-form-urlencoded";
        String token = doPost(url, paramMap, mediaType, null, "");
        return token;
    }

    /**
     * 通过refresh_token获取token
     *
     * @param refresh_token 刷新令牌
     * @return token 字符串
     * @throws Exception 异常
     */
    private String getTokenByRefreshToken(String refresh_token) throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("grant_type", "refresh_token");
        // 第三方应用id
        paramMap.put("client_id", this.client_id);
        // 第三方应用secret 公钥加密
        paramMap.put("client_secret", URLEncoder.encode(Encryption.pubEncrypt(this.pubKey, this.client_secret), OpenAPIConstants.UTF_8));
        // 签名
        String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + refresh_token + this.pubKey, this.pubKey);
        paramMap.put("signature", sign);
        paramMap.put("refresh_token", refresh_token);
        paramMap.put("biz_center", biz_center);

        String url = this.baseUrl + "nccloud/opm/accesstoken";
        String mediaType = "application/x-www-form-urlencoded";
        String token = doPost(url, paramMap, mediaType, null, "");
        return token;
    }

    /**
     * 获取token
     *
     * @return token 字符串
     * @throws Exception 异常
     */
    @Override
    public String getToken() throws Exception {
        String token = null;
        if ("password".equals(this.grant_type)) {
            // 密码模式
            token = getTokenByPWD();
        } else if ("client".equals(this.grant_type)) {
            // 客户端模式
            token = getTokenByClient();
        } else {
            throw new Exception("token获取模式错误");
        }
        return token;
    }

    /**
     * 密码模式获取token
     *
     * @return token 字符串
     * @throws Exception 异常
     */
    private String getTokenByPWD() throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        // 密码模式认证
        paramMap.put("grant_type", "password");
        // 第三方应用id
        paramMap.put("client_id", this.client_id);
        // 第三方应用secret 公钥加密
        paramMap.put("client_secret", URLEncoder.encode(Encryption.pubEncrypt(this.pubKey, this.client_secret), OpenAPIConstants.UTF_8));
        // ncc用户名
        paramMap.put("username", this.user_name);
        // 密码 公钥加密
        paramMap.put("password", URLEncoder.encode(Encryption.pubEncrypt(this.pubKey, this.pwd), OpenAPIConstants.UTF_8));
        // 账套编码
        paramMap.put("biz_center", biz_center);
        // 签名
        String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + this.user_name + this.pwd + this.pubKey, this.pubKey);
        paramMap.put("signature", sign);

        String url = this.baseUrl + "nccloud/opm/accesstoken";
        String mediaType = "application/x-www-form-urlencoded";
        String token = doPost(url, paramMap, mediaType, null, "");
        return token;
    }

    /**
     * 根据加密等级解密返回数据
     *
     * @param source 原始数据
     * @param security_key 安全密钥
     * @param level 加密等级
     * @return 解密后的数据
     * @throws Exception 异常
     */
    private String dealResponseBody(String source, String security_key, String level) throws Exception {
        String result = null;
        if (level == null || "".equals(level.trim()) || SecretConst.LEVEL0.equals(level)) {
            result = source;
        } else if (SecretConst.LEVEL1.equals(level)) {
            result = Decryption.symDecrypt(security_key, source);
        } else if (SecretConst.LEVEL2.equals(level)) {
            result = CompressUtil.gzipDecompress(source);
        } else if (SecretConst.LEVEL3.equals(level)) {
            result = CompressUtil.gzipDecompress(Decryption.symDecrypt(security_key, source));
        } else if (SecretConst.LEVEL4.equals(level)) {
            result = Decryption.symDecrypt(security_key, CompressUtil.gzipDecompress(source));
        } else {
            throw new Exception("无效的安全等级");
        }

        return result;
    }

    /**
     * 根据加密等级加密请求体
     *
     * @param source 原始数据
     * @param security_key 安全密钥
     * @param level 加密等级
     * @return 加密后的数据
     * @throws Exception 异常
     */
    private String dealRequestBody(String source, String security_key, String level) throws Exception {
        String result = null;
        if (level == null || "".equals(level.trim()) || SecretConst.LEVEL0.equals(level)) {
            result = source;
        } else if (SecretConst.LEVEL1.equals(level)) {
            result = Encryption.symEncrypt(security_key, source);
        } else if (SecretConst.LEVEL2.equals(level)) {
            result = CompressUtil.gzipCompress(source);
        } else if (SecretConst.LEVEL3.equals(level)) {
            result = Encryption.symEncrypt(security_key, CompressUtil.gzipCompress(source));
        } else if (SecretConst.LEVEL4.equals(level)) {
            result = CompressUtil.gzipCompress(Encryption.symEncrypt(security_key, source));
        } else {
            throw new Exception("无效的安全等级");
        }

        return result;
    }

    /**
     * 忽略HTTPS 请求的SSL 证书，必须在openConnection 之前调用
     *
     * ⚠️ 警告：仅用于开发/测试环境，生产环境应使用正确的证书验证
     */
    private void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * 信任所有主机的证书验证器
     *
     * ⚠️ 警告：仅用于开发/测试环境
     */
    class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            // 不进行任何验证
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            // 不进行任何验证
        }
    }

    public static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 发送POST请求
     *
     * @param baseUrl 请求URL
     * @param paramMap 参数映射
     * @param mediaType 媒体类型
     * @param headers 请求头
     * @param json JSON 体
     * @return 响应字符串
     * @throws Exception 异常
     */
    private String doPost(String baseUrl, Map<String, String> paramMap, String mediaType, Map<String, String> headers, String json) throws Exception {

        HttpURLConnection urlConnection = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader bufferedReader = null;
        String result = null;
        try {
            // 构建 URL
            StringBuilder sb = new StringBuilder();
            sb.append(baseUrl);
            if (paramMap != null && !paramMap.isEmpty()) {
                sb.append("?");
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    sb.append(key).append("=").append(value).append("&");
                }
                baseUrl = sb.toString().substring(0, sb.toString().length() - 1);
            }

            // 忽略 SSL 证书验证
            trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);

            // 建立连接
            URL urlObj = new URL(baseUrl);
            urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setConnectTimeout(this.timeout == null ? 50000 : this.timeout);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.addRequestProperty("content-type", mediaType);

            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 发送请求体
            out = urlConnection.getOutputStream();
            out.write(json.getBytes(OpenAPIConstants.UTF_8));
            out.flush();

            // 读取响应
            int resCode = urlConnection.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED || resCode == HttpURLConnection.HTTP_ACCEPTED) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }

            bufferedReader = new BufferedReader(new InputStreamReader(in, OpenAPIConstants.UTF_8));
            StringBuilder temp = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                temp.append(line).append("\r\n");
            }

            String ecod = urlConnection.getContentEncoding();
            if (ecod == null) {
                ecod = Charset.forName(OpenAPIConstants.UTF_8).name();
            }
            result = new String(temp.toString().getBytes(OpenAPIConstants.UTF_8), ecod);

        } catch (Exception e) {
            log.error("HTTP POST 请求失败: {}", baseUrl, e);
            throw e;
        } finally {
            // 关闭资源
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                log.warn("关闭资源时出错", e);
            }
        }

        return result;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    /**
     * 获取 API 返回结果
     *
     * 改用 Jackson 替代 FastJSON，避免安全漏洞
     *
     * @param token 访问令牌
     * @param json 请求 JSON
     * @return API 返回结果
     * @throws Exception 异常
     */
    @Override
    public String getAPIRetrun(String token, String json) throws Exception {
        try {
            // 使用 Jackson 替代 FastJSON
            JsonNode tokenNode = objectMapper.readTree(token);

            if (tokenNode == null || tokenNode.get("data") == null) {
                throw new Exception("获取token失败: " + token);
            }

            JsonNode dataNode = tokenNode.get("data");
            String access_token = dataNode.has("access_token") ? dataNode.get("access_token").asText() : null;
            String security_key = dataNode.has("security_key") ? dataNode.get("security_key").asText() : null;

            if (access_token == null || security_key == null) {
                throw new Exception("获取token失败：access_token 或 security_key 为空");
            }

            // api请求路径
            String url = this.baseUrl + this.apiUrl;
            Map<String, String> headermap = new HashMap<>();
            headermap.put("access_token", access_token);
            headermap.put("client_id", this.client_id);

            if (this.tenant_id != null) {
                headermap.put("tenant_id", this.tenant_id);
            }

            // 构建签名
            StringBuilder sb = new StringBuilder();
            sb.append(this.client_id);
            if (json != null) {
                sb.append(json);
            }
            sb.append(this.pubKey);

            String sign = SHA256Util.getSHA256(sb.toString(), this.pubKey);
            headermap.put("signature", sign);
            headermap.put("repeat_check", "Y");
            headermap.put("ucg_flag", "y");

            String mediaType = "application/json;charset=utf-8";

            // 根据安全级别加密请求体
            String requestBody = dealRequestBody(json, security_key, this.secret_level);

            // 发送请求
            String result = doPost(url, null, mediaType, headermap, requestBody);

            // 根据安全级别解密响应
            String responseBody = dealResponseBody(result, security_key, this.secret_level);

            return responseBody;

        } catch (Exception e) {
            log.error("API 调用失败", e);
            throw e;
        }
    }

    @Override
    public void init(String ip, String port, String biz_center, String client_id,
                     String client_secret, String pubKey, String user_name, String pwd, String tenant_id) {
        this.baseUrl = "https://" + ip + ":" + port + "/";
        this.biz_center = biz_center;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.pubKey = pubKey;
        this.user_name = user_name;
        this.pwd = pwd;
        this.tenant_id = tenant_id;
    }

    @Override
    public void init(String ip, String port, String biz_center, String client_id,
                     String client_secret, String pubKey, String user_name, String pwd, String tenant_id, String secret_level) {
        this.baseUrl = "https://" + ip + ":" + port + "/";
        this.biz_center = biz_center;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.pubKey = pubKey;
        this.user_name = user_name;
        this.pwd = pwd;
        this.tenant_id = tenant_id;
        this.secret_level = secret_level;
    }

    public void setSecret_level(String secret_level) {
        this.secret_level = secret_level;
    }

    public static void main(String[] args) {
        API4HttpsCurUtils util = new API4HttpsCurUtils();

        // 配置信息
        String ip = "ncc.qa.91jkys.com";
        String port = "443";
        String biz_center = "002";
        String client_id = "ddc31deaba3e4bcbaf2c";
        String client_secret = "4afaaa131304413fbbbf";
        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAinPp5+QYUodma18XLkP9aMx4bqrPSaRh1Zg+GK3UjGGoQYWdemLFiQ8rtSW93HTm9fi40pOf0horLTbISEErSnQnsAT+NqmGdIteToGXYVErW4ZAUzFndK3qVUEXMSiwuv0KxoUNZCRW1gVQvmGt1Oav91nkaEvY+oo4/nxgKccLoAhAghZtx4hTXuHNEk0tI/ghJ0P/No3X9+BsC4a+zMk/cFmiHnuoAGBLkJxC9KTldZVBDNhQCD/WNai9pqmS/rJ5EhjWZpWjM40nt3D8f+tZKsxYf/6LV+gUWPgXimX5ZRzDq5AWl2aqcTesFBkee5hsAQIjCwrFUaeU69nB0wIDAQAB";
        pubKey = pubKey.replaceAll("\\s+", ""); // 移除所有空格
        String user_name = "00003325";
        String pwd = null;
        String tenant_id = null;
        String secret_level = "L0";

        util.init(ip, port, biz_center, client_id, client_secret, pubKey, user_name, pwd, tenant_id, secret_level);

        try {
            String token = util.getToken();
            log.info("获取 token 成功");
            System.out.println(token);

            // 设置 API 路径
            util.setApiUrl("nccloud/api/riaorg/orgmanage/org/queryOrgByCode");
            String json = "{\"code\": [\"01\", \"cxzgs\"]}";

            // 调用 API
            String apiResult = util.getAPIRetrun(token, json);
            log.info("API 调用成功");
            System.out.println(apiResult);

        } catch (Exception e) {
            log.error("执行失败", e);
            e.printStackTrace();
        }
    }
}