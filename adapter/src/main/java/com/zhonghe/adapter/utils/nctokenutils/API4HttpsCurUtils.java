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

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class API4HttpsCurUtils implements IAPIUtils {

	/**
	 *  授权模式
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
		// 集团编码
		//		paramMap.put("groupcode", this.gr);
		// 传递数据源和ncc登录用户
		// paramMap.put("dsname", "TM_0614");
		paramMap.put("usercode", this.user_name);

		// 签名
		String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + this.pubKey,this.pubKey);
		paramMap.put("signature", sign);

		String url = this.baseUrl + "nccloud/opm/accesstoken";
		String mediaType = "application/x-www-form-urlencoded";
		String token = doPost(url, paramMap, mediaType, null, "");
		return token;
	}

	/**
	 * 通过refresh_token获取token
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
		String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + refresh_token + this.pubKey,this.pubKey);
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
		} else{
			throw new Exception("token获取模式错误");
		}
		return token;
	}

	/**
	 * 密码模式获取token
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
		String sign = SHA256Util.getSHA256(this.client_id + this.client_secret + this.user_name + this.pwd + this.pubKey,this.pubKey);
		paramMap.put("signature", sign);
		String url = this.baseUrl + "nccloud/opm/accesstoken";
		String mediaType = "application/x-www-form-urlencoded";
		String token = doPost(url, paramMap, mediaType, null, "");
		return token;
	}

	/**
	 * 根据加密等级解密返回数据
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


	// 忽略HTTPS 请求的SSL 证书，必须在openConnection 之前调用
	private void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

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
			return;
		}

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
	public static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};


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
			trustAllHttpsCertificates();//调用端不用装https的证书
			HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);

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
	public String getAPIRetrun(String token,String json) throws Exception{
		APIReturnEntity tokenJson = JSON.parseObject(token, APIReturnEntity.class);
		String access_token;
		String security_key;
		//String refresh_token;
		if(tokenJson != null && tokenJson.getData() != null && tokenJson.getData() instanceof JSONObject) {
			access_token = ((JSONObject)tokenJson.getData()).getString("access_token");
			security_key = ((JSONObject)tokenJson.getData()).getString("security_key");
			//refresh_token = ((JSONObject)tokenJson.getData()).getString("refresh_token");
		}else {
			throw new Exception("获取token失败:"+token);
		}
		//api请求路径
		String url = this.baseUrl + this.apiUrl;
		Map<String,String> headermap = new HashMap<String,String>();
		headermap.put("access_token", access_token);
		headermap.put("client_id", this.client_id);
		if(this.tenant_id != null) {
			headermap.put("tenant_id", this.tenant_id);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(this.client_id);
		if (json!=null) {
			sb.append(json);
		}
		sb.append(this.pubKey);
		// this.client_id + this.client_secret + this.user_name + this.pwd + this.pubKey
		String sign = SHA256Util.getSHA256(sb.toString(),this.pubKey);
		headermap.put("signature", sign);

		//headermap.put("busi_id", busi_id);
		headermap.put("repeat_check", "Y");
		headermap.put("ucg_flag", "y");

		String mediaType = "application/json;charset=utf-8";

		// 表体数据json
		// 根据安全级别选择加密或压缩请求表体参数
		String requestBody = dealRequestBody(json, security_key, this.secret_level);

		// 返回值
		String result = doPost(url, null, mediaType, headermap, requestBody);
		//		System.out.print(result);
		String responseBody = dealResponseBody(result, security_key, this.secret_level);
		return responseBody;
	}

	@Override
	public void init(String ip,String port,String biz_center,String client_id
			,String client_secret,String pubKey,String user_name,String pwd,String tenant_id) {
		this.baseUrl = "https://"+ip+":"+port+"/";
		this.biz_center = biz_center;
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.pubKey = pubKey;
		this.user_name = user_name;
		this.pwd = pwd;
		this.tenant_id = tenant_id;
	}

	@Override
	public void init(String ip,String port,String biz_center,String client_id
			,String client_secret,String pubKey,String user_name,String pwd,String tenant_id, String secret_level) {
		this.baseUrl = "https://"+ip+":"+port+"/";
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

		//链接春训环境
		//		String ip = "10.16.231.90";//高级版环境ip
		//		String port = "2311";//高级版环境端口
		//本地环境
		String ip = "ncc.qa.91jkys.com";//高级版环境ip
		String port = "443";//https协议默认的端口是443

		String biz_center = "002";//表 sm_busicenter 中的code字段的值
		String client_id = "ddc31deaba3e4bcbaf2c";//第三方应用的 应用编码
		String client_secret = "4afaaa131304413fbbbf";//第三方应用的 应用密文
		//pubKey 直接拷贝过来含有空格，注意要清楚空格
		String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAinPp5+QYUodma18XLkP9aMx4bqrPSaRh 1Zg+GK3UjGGoQYWdemLFiQ8rtSW93HTm9fi40pOf0horLTbISEErSnQnsAT+NqmGdIteToGXYVEr W4ZAUzFndK3qVUEXMSiwuv0KxoUNZCRW1gVQvmGt1Oav91nkaEvY+oo4/nxgKccLoAhAghZtx4hT XuHNEk0tI/ghJ0P/No3X9+BsC4a+zMk/cFmiHnuoAGBLkJxC9KTldZVBDNhQCD/WNai9pqmS/rJ5 EhjWZpWjM40nt3D8f+tZKsxYf/6LV+gUWPgXimX5ZRzDq5AWl2aqcTesFBkee5hsAQIjCwrFUaeU 69nB0wIDAQAB";//第三方应用的 公钥
		pubKey = StringUtils.replaceAll(pubKey, " ", "");
		String user_name = "00003325";
		String pwd = null;
		String tenant_id = null;// 应用编码or租户idor集团tenantid    tenant_id不能合token同时有值

		//加密等级
		String secret_level = "L0";

		util.init(ip, port, biz_center, client_id, client_secret, pubKey, user_name, pwd, tenant_id,secret_level);
		try {
			String token = util.getToken();
			System.out.println(token);
			//组织查询
			util.setApiUrl("nccloud/api/riaorg/orgmanage/org/queryOrgByCode");
			//			//部门查询
			//			util.setApiUrl("nccloud/api/riaorg/org/dept/queryDeptByCode");
			String json = "{\"code\": [\"01\", \"cxzgs\"]}";//[\"T2001\"]

			//供应商基本分类nccloud/api/uapbd/supplierclassmanage/supplierclass/supplierclassquery
			//			util.setApiUrl("nccloud/api/uapbd/supplierclassmanage/supplierclass/supplierclassquery");
			//			String json = "{  \"ufinterface\": {    \"sender\": \"default\",    \"data\": {      \"pk_org\": [        \"cxzgs\"      ]    }  }}";//[\"T2001\"]
			String apiRetrun = util.getAPIRetrun(token, json);
			System.out.print(apiRetrun);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
