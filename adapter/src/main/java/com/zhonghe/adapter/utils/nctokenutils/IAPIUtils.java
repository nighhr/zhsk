package com.zhonghe.adapter.utils.nctokenutils;

public interface IAPIUtils {

	public String getToken() throws Exception;

	// 适配公有云租户id（tenant_id）
	public void init(String ip,String port,String biz_center,String client_id
			,String client_secret,String pubKey,String user_name,String pwd,String tenant_id);

	public void init(String ip, String port, String biz_center, String client_id, String client_secret, String pubKey,
			String user_name, String pwd, String tenant_id, String secret_level);

	public void setApiUrl(String apiUrl);

	public void setTimeout(Integer timeout);

	public String getAPIRetrun(String token,String json) throws Exception;

	class SecretConst {
		/**
		 * LEVEL0 不压缩、不加密
		 */
		public static final String LEVEL0 = "L0";
		/**
		 * LEVEL1 只加密、不压缩
		 */
		public static final String LEVEL1 = "L1";
		/**
		 * LEVEL2 只压缩、不加密
		 */
		public static final String LEVEL2 = "L2";
		/**
		 * LEVEL3 先压缩、后加密
		 */
		public static final String LEVEL3 = "L3";
		/**
		 * LEVEL4 先加密、后压缩
		 */
		public static final String LEVEL4 = "L4";
	}


}
