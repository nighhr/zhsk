package com.zhonghe.adapter.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

public class PasswordUtils {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String SECRET_KEY = "MyDb2024!@#12345"; // 16/24/32字节
    private static final String ALGORITHM = "AES";

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");

    // 可逆加密（用于数据库密码）
    public static String encryptDBPassword(String password) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(password.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 解密（用于数据库密码）
    public static String decryptDBPassword(String encrypted) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(original);
    }

    /**
     * 安全获取密码：如果是加密密码则解密，否则返回原密码
     * @param password 密码字符串
     * @return 解密后的明文密码或原密码
     */
    public static String getSafePassword(String password) {
        if (password == null) {
            return null;
        }

        if (isEncryptedPassword(password)) {
            try {
                return decryptDBPassword(password);
            } catch (Exception e) {
                // 解密失败，返回原密码
                return password;
            }
        }

        // 明文密码，直接返回
        return password;
    }

    public static boolean isEncryptedPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // 检查是否符合Base64格式
        if (!BASE64_PATTERN.matcher(password).matches()) {
            return false;
        }

        // 检查长度：AES加密后的Base64字符串通常有特定长度特征
        // 空字符串加密后也有一定长度，普通密码加密后会更长
        if (password.length() < 16) {
            return false;
        }

        // 尝试解密验证（确保是有效的AES加密数据）
        try {
            decryptDBPassword(password);
            return true;
        } catch (Exception e) {
            // 解密失败，说明不是有效的AES加密数据
            return false;
        }
    }

    /**
     * 快速判断是否可能是加密密码（不进行解密验证，性能更好）
     * @param password 待检查的密码
     * @return true-可能是加密密码, false-很可能是明文
     */
    public static boolean isLikelyEncrypted(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // 检查是否符合Base64格式
        if (!BASE64_PATTERN.matcher(password).matches()) {
            return false;
        }

        // 检查长度特征
        if (password.length() < 16) {
            return false;
        }

        // 检查是否包含常见明文密码特征（可选）
        // 如果包含空格、中文等Base64不包含的字符，则很可能是明文
        if (password.contains(" ") || containsNonAscii(password)) {
            return false;
        }

        return true;
    }

    /**
     * 检查字符串是否包含非ASCII字符
     */
    private static boolean containsNonAscii(String str) {
        return !str.matches("\\A\\p{ASCII}*\\z");
    }

}
