package com.shop.util;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PasswordHelper {

    @Autowired
    private HashedCredentialsMatcher hashedCredentialsMatcher;

    /**
     * 加密密码
     */
    public String encryptPassword(String password, String salt) {
        SimpleHash hash = new SimpleHash(
                hashedCredentialsMatcher.getHashAlgorithmName(),
                password,
                ByteSource.Util.bytes(salt),
                hashedCredentialsMatcher.getHashIterations()
        );
        return hash.toHex();
    }

    /**
     * 生成随机盐值
     */
    public String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 验证密码（可选，Shiro 会自动验证）
     */
    public boolean validatePassword(String inputPassword, String dbPassword, String salt) {
        String encrypted = encryptPassword(inputPassword, salt);
        return encrypted.equals(dbPassword);
    }
}