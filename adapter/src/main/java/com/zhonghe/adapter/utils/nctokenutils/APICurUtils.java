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


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class APICurUtils implements IAPIUtils {

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
     * @return
     * @throws Exception
     */
    public String getTokenByClient() throws Exception {
        Map<String, String> paramMap = new HashMap<String, String>();
        // 密码模式认证֤
        paramMap.put("grant_type", "client_credentials");
        // 第三方应用id
        paramMap.put("client_id", this.client_id);
        // 第三方应用secret 公钥加密
        String encode = URLEncoder.encode(Encryption.pubEncrypt(this.pubKey, this.client_secret), OpenAPIConstants.UTF_8);
        paramMap.put("client_secret", encode);
        // 账套编码
        paramMap.put("biz_center", biz_center);
        // 集团编码O
//		paramMap.put("groupcode", this.gr);
        // 传递数据源和ncc登录用户
        // paramMap.put("dsname", "TM_0614");
        paramMap.put("usercode", this.user_name);
        // 签名
        String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + this.pubKey, this.pubKey);
        paramMap.put("signature", "64d29c5fc7ceb113fe61d1aee1c8be287b1cb838c8edefabf183e43b5cd11f23");
//		paramMap.put("langCode", "English");//语种

        String url = this.baseUrl + "nccloud/opm/accesstoken";
        String mediaType = "application/x-www-form-urlencoded";
        String token = doPost(url, paramMap, mediaType, null, "");
        return token;
    }

    /**
     * 通过refresh_token获取token
     *
     * @param refresh_token
     * @return
     * @throws Exception
     */
    private String getTokenByRefreshToken(String refresh_token) throws Exception {
        Map<String, String> paramMap = new HashMap<String, String>();
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
     * @return
     * @throws Exception
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
     * @return
     * @throws Exception
     */
    private String getTokenByPWD() throws Exception {
        Map<String, String> paramMap = new HashMap<String, String>();
        // 密码模式认证֤
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
     * @param source
     * @param security_key
     * @param level
     * @return
     * @throws Exception
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
     * @param source
     * @param security_key
     * @param level
     * @return
     * @throws Exception
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
     * 发送post请求
     *
     * @param baseUrl
     * @param paramMap
     * @param mediaType
     * @param headers
     * @param json
     * @return
     */
    private String doPost(String baseUrl, Map<String, String> paramMap, String mediaType, Map<String, String> headers, String json) throws Exception {

        HttpURLConnection urlConnection = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader bufferedReader = null;
        String result = null;
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(baseUrl);
            if (paramMap != null) {
                sb.append("?");
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    sb.append(key + "=" + value).append("&");
                }
                baseUrl = sb.toString().substring(0, sb.toString().length() - 1);
            }

            URL urlObj = new URL(baseUrl);
            urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setConnectTimeout(this.timeout == null ? 50000 : this.timeout);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.addRequestProperty("content-type", mediaType);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    urlConnection.addRequestProperty(key, headers.get(key));
                }
            }

            // 输出请求信息
            System.out.println("=== POST请求信息 ===");
            System.out.println("请求URL: " + baseUrl);
            System.out.println("请求方法: POST");
            System.out.println("Content-Type: " + mediaType);

            // 输出请求头
            System.out.println("=== 请求头 ===");
            System.out.println("Content-Type: " + mediaType);
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    System.out.println(header.getKey() + ": " + header.getValue());
                }
            }

            // 输出请求体
            System.out.println("=== 请求体 ===");
            System.out.println(json);
            System.out.println("===================");

            out = urlConnection.getOutputStream();
            out.write(json.getBytes(OpenAPIConstants.UTF_8));
            out.flush();

            int resCode = urlConnection.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_CREATED || resCode == HttpURLConnection.HTTP_ACCEPTED) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(in, OpenAPIConstants.UTF_8));
            StringBuffer temp = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                temp.append(line).append("\r\n");
                line = bufferedReader.readLine();
            }
            String ecod = urlConnection.getContentEncoding();
            if (ecod == null) {
                ecod = Charset.forName(OpenAPIConstants.UTF_8).name();
            }
            result = new String(temp.toString().getBytes(OpenAPIConstants.UTF_8), ecod);
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            urlConnection.disconnect();
        }
        return result;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    @Override
    public String getAPIRetrun(String token, String json) throws Exception {
        APIReturnEntity tokenJson = JSON.parseObject(token, APIReturnEntity.class);
        String access_token;
        String security_key;
        //String refresh_token;
        if (tokenJson != null && tokenJson.getData() != null && tokenJson.getData() instanceof JSONObject) {
            access_token = ((JSONObject) tokenJson.getData()).getString("access_token");
            security_key = ((JSONObject) tokenJson.getData()).getString("security_key");
            //refresh_token = ((JSONObject)tokenJson.getData()).getString("refresh_token");
        } else {
            throw new Exception("获取token失败:" + token);
        }
        //api请求路径
        String url = this.baseUrl + this.apiUrl;
        Map<String, String> headermap = new HashMap<String, String>();
        headermap.put("access_token", access_token);
        headermap.put("client_id", this.client_id);
        if (this.tenant_id != null) {
            headermap.put("tenant_id", this.tenant_id);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(this.client_id);
        if (json != null) {
            sb.append(json);
        }
        sb.append(this.pubKey);
        // this.client_id + this.client_secret + this.user_name + this.pwd + this.pubKey
        String sign = SHA256Util.getSHA256(sb.toString(), this.pubKey);
        headermap.put("signature", sign);

        //下面两个表示添加幂等校验
//		headermap.put("busi_id", "123456a");//唯一性的业务标识
//		headermap.put("repeat_check", "Y");//是否进行唯一性校验---幂等校验

        headermap.put("ucg_flag", "y");//ucg_flag 表示绕过验签

        String mediaType = "application/json;charset=utf-8";

        // 表体数据json
        // 根据安全级别选择加密或压缩请求表体参数
        String requestBody = dealRequestBody(json, security_key, this.secret_level);

        // 返回值
        //发起请求
        String result = doPost(url, null, mediaType, headermap, requestBody);
//		System.out.print(result);
        String responseBody = dealResponseBody(result, security_key, this.secret_level);
        return responseBody;
    }

    @Override
    public void init(String ip, String port, String biz_center, String client_id
            , String client_secret, String pubKey, String user_name, String pwd, String tenant_id) {
        this.baseUrl = "http://" + ip + ":" + port + "/";
        this.biz_center = biz_center;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.pubKey = pubKey;
        this.user_name = user_name;
        this.pwd = pwd;
        this.tenant_id = tenant_id;
    }

    @Override
    public void init(String ip, String port, String biz_center, String client_id
            , String client_secret, String pubKey, String user_name, String pwd, String tenant_id, String secret_level) {
        this.baseUrl = "http://" + ip + ":" + port + "/";
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
        APICurUtils util = new APICurUtils();
        //链接高级版环境
        String ip = "112.102.234.196";//高级版环境ip
        String port = "11110";//高级版环境端口
        String biz_center = "LT";//表 sm_busicenter 中的code字段的值
        String client_id = "dingding";//第三方应用(OpenAPI应用)的 应用编码
        String client_secret = "46742db37b3948e2bfef";//第三方应用(OpenAPI应用)的 应用密文
        //pubKey 直接拷贝过来含有空格，注意要清楚空格
        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqQLfSbqXDtvg2IucmyI8c0RB678h6dgfKncuDIU+fQmYUVdbUmV7ZKEjFjolo7xqN9SMjrSVUIX4c3AqovzX7NbCVHfQkxmk3SeiIsF8lvjEAMqra5xH9VaZnbSmeyh143bXzO9SJgdegLOQOV2SeENy93BFAyr16Q1E1AYsMq0xmoc/D+fbxDN94aDNKbwzEcSttOVXuOHziF3uO42/8oHYcN3dbudJAu6FkSqR4b5m1ZDVcJb1Q7KcVs44jQ8cpZWBa97RN7S9xROVMnIF43G33Gi7WdwgsbExozaeco3OK3Bew0Z0BLkz1q1G+nF6GqpHgyd1ftrLaL6y9UVBTQIDAQAB+hDwpvAZBUPVem52kfJibm4UPobviXLrWuuahRcrzvVWX6JKlRmDXuFhUw7VomlOYp5kUqOelV6LO9ShKoxx+awtNexvSsEPOX0qluF5TuuSI7yTNjphKSrw3CmCpQcWrOmOBxjsbuKLt3Xu76WDtMmsjiW8HEbL4NbI3/T6hs7b95qbvbm4UJqKQTl1ukQUlV97fboRwPCT+OHmFjJOhQnWg3p65iuQcQwC2yTHM1S2lY8G7xiTvofh3e/Mtb7wXt89mOV4VdS7QdXKEisjM1i7SYiwBLzPKJnV97hCQcV6t/l0Ta0/KHLIwzAfkKgJ2vtjFm1cnhwIDAQAB";//第三方应用的 公钥
        String user_name = "admin04";//业务用户，可以不传，不传取第三方应用(OpenAPI应用)里的用户,注意不能是系统管理员
        String pwd = "";//业务用户密码， 密码模式需要，client模式不需要
        String tenant_id = null;// 应用编码or租户idor集团tenantid    tenant_id不能和token同时有值
        String secret_level = "L0";//加密等级
        util.init(ip, port, biz_center, client_id, client_secret, pubKey, user_name, pwd, tenant_id, secret_level);
        try {
            //第一步 获取token，  模式--client，  客户端、用户名密码--password
            util.setGrant_type("client");
            String token = util.getToken();
            System.out.println("token" + token);

            //第二步  调接口，
            util.setApiUrl("nccloud/api/uapbd/psndocmanage/querypsndoc/condition");
            String json = "{" +
                    "\"ufinterface\": {" +
                    "\"sender\": \"default\"," +
                    "\"data\": {" +
                    "\"pk_group\": null," +
                    "\"pk_org\": [\"A0101\",\"A0102\",\"A0103\"]," +
                    "\"pk_dept\": []," +
                    "\"code\": []," +
                    "\"name\": []," +
                    "\"id\": []," +
                    "\"ts\": null" +
                    "}," +
                    "\"pageInfo\": {" +
                    "\"pageIndex\": \"0\"," +
                    "\"pageSize\": \"500\"" +
                    "}" +
                    "}" +
                    "}";
            String apiRetrun = util.getAPIRetrun(token, json);

            System.out.print(" apiRetrun --> " + apiRetrun);
        } catch (Exception e) {
            System.out.println(e);
        }


    }

}
