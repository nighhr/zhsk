package com.zhonghe.adapter.utils.nctokenutils;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class SHA256Util {
	public static String getSHA256(String str,String key) throws Exception {
		byte[] salt = new byte[16];
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(key.getBytes());
		random.nextBytes(salt);
		String salt_value = Base64Util.encryptBASE64(salt);
		return getSHA256(str + salt_value.replaceAll("\r|\n", ""));
	}

	private static String getSHA256(String str) throws Exception {
		MessageDigest messageDigest;
		String encodestr = "";
		messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(str.getBytes("UTF-8"));
		encodestr = byte2Hex(messageDigest.digest());
		return encodestr;
	}

	private static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

}