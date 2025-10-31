package com.zhonghe.adapter.utils.nctokenutils;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

/**
 * Base64工具类
 *
 * @author daixusky
 *
 */
public class Base64Util {

	/**
	 * Base64编码
	 */
	public static String encryptBASE64(byte[] key) {
		return (new Base64()).encodeToString(key);
	}

	/**
	 * Base64解码
	 *
	 * @throws IOException
	 */
	public static byte[] decryptBASE64(String key) {
		return (new Base64()).decode(key);
	}

}