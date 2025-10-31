package com.zhonghe.adapter.utils.nctokenutils;

import java.security.KeyPair;

/**
 * keys 主键对
 *
 * @author daixusky
 *
 */
public class KeyPairs {

	private KeyPair keyPair;

	public KeyPairs(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public String getPublicKey() {
		return Base64Util.encryptBASE64(keyPair.getPublic().getEncoded());
	}

	public String getPrivateKey() {
		return Base64Util.encryptBASE64(keyPair.getPrivate().getEncoded());
	}

}
