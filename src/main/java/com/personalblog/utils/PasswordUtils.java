package com.personalblog.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    /**
     * 对明文密码进行加密
     *
     * @param plainPassword 明文密码 (如 "123456")
     * @return 加密后的密文 (如 "$2a$10$...")
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) return null;
        // gensalt() 会自动生成随机盐
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 校验密码是否正确
     *
     * @param plainPassword 用户输入的明文密码
     * @param hashedPassword 数据库中存储的密文
     * @return 匹配返回 true，否则 false
     */
    public static boolean check(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            // BCrypt 会自动从 hashedPassword 中提取盐，并进行哈希比对
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // 如果数据库存的是旧的明文密码，这里可能会报错，视为校验失败
            return false;
        }
    }
}