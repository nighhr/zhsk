package com.zhonghe.adapter.utils.nctokenutils;

import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;


/**
 * 获取加密的client_secret和signature
 */
public class GetSignatureUtil {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String client_id = "rms";//第三方应用的编码
		//pubKey,注意清除空格
		String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh+DsomXf8K4DkhpxLcZcPtXKLEU6e7ofonGQuq3Z7ovu6n5TNcQ0rM40s1BB9c09P15Tk7esHAu43AWfR3AwmcA/gxyn3u9rxae6fYQ37BanG6NBrYsVv/Noqw9YX+kJZ6lH3Xoo2FRscwBaB1Xn5lpttai6MREbkvosKZD0aAVQuQT9Dy2Cio0bh9fChnCvUYIgP+lBcXUtrFzQOTa/Y5KWagsR8pAJHK+nY9d4+G5u0vcePy/gu6WoTVFZigrNgmL2yczKCox+vOdt2H6+O1kUoX1uHNhFEGB8IRbes5x9EPf7gX6e4JjT4+y41Y3BvTdTDiD1UoeVbb6G20hdiwIDAQAB";
		pubKey = StringUtils.replaceAll(pubKey, " ", "");
		//第三方应用的密文
		String client_secret = "d8d94b1ca7554646bef9";
		try {
			// 第三方应用secret 公钥加密,使用postman等工具调用的参数
			String client_secret_encode = URLEncoder.encode(Encryption.pubEncrypt(pubKey, client_secret), "UTF-8");
//			System.out.println(client_secret_encode);
			
			// 签名
//			String signature = SHA256Util.getSHA256(client_id + client_secret + pubKey,pubKey);
//			System.out.println(signature);
			
			//接口签名
			String requestBody = "{\"array\":[{\"so_saleorder\":{\"ccustomerid\":\"1002342\",\"cdeptid\":\"120201\",\"cdeptvid\":\"120201\",\"cemployeeid\":\"RMS\",\"pk_org\":\"0101\",\"vdef14\":\"2\",\"vdef18\":\"RMS\",\"vdef19\":\"iDataWXFK20240806002\",\"vdef2\":\"01\",\"vtrantypecode\":\"30-Cxx-05\"},\"so_saleorder_b\":[{\"blargessflag\":\"N\",\"castunitid\":\"PCS\",\"cmaterialvid\":\"C01010147\",\"cprojectid\":\"PM000042\",\"cqtunitid\":\"PCS\",\"csendstordocid\":\"0101006\",\"nastnum\":1.0,\"nnum\":1.0,\"norigtaxmny\":200.0,\"nqtunitnum\":\"1\",\"vbdef1\":\"SB10\",\"vbdef11\":\"3\",\"vbdef12\":\"2\"},{\"blargessflag\":\"N\",\"castunitid\":\"PCS\",\"cmaterialvid\":\"201010011\",\"cprojectid\":\"PM000042\",\"cqtunitid\":\"PCS\",\"csendstordocid\":\"0101006\",\"nastnum\":1.0,\"nnum\":1.0,\"norigtaxmny\":10.0,\"nqtunitnum\":\"1\",\"vbdef1\":\"SB10\",\"vbdef11\":\"3\",\"vbdef12\":\"2\"},{\"blargessflag\":\"Y\",\"castunitid\":\"PCS\",\"cmaterialvid\":\"403010001\",\"cprojectid\":\"PM000042\",\"cqtunitid\":\"PCS\",\"csendstordocid\":\"0101006\",\"nastnum\":1.0,\"nnum\":1.0,\"norigtaxmny\":0.0,\"nqtunitnum\":\"1\",\"vbdef1\":\"SB10\",\"vbdef11\":\"3\",\"vbdef12\":\"2\"}]}]}";
			String requestBodysign = SHA256Util.getSHA256(client_id + requestBody + pubKey, pubKey);
			System.out.println(requestBodysign);
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
}
