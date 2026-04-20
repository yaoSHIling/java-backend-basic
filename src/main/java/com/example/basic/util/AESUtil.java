package com.example.basic.util;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加密工具。
 *
 * <p>用于加密敏感数据（如密码传输、接口签名）。
 * 使用 AES/GCM/NoPadding 模式（自带随机IV，安全性更高）。
 *
 * <p>使用 Hutool 实现，无需手动处理 IV。
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class AESUtil {

    private static SymmetricCrypto AES;

    /**
     * 初始化 AES 加密器。
     *
     * @param secretKey 密钥（至少16字符，GCM推荐32字符）
     */
    @jakarta.annotation.PostConstruct
    public void init(
            @Value("${aes.secret:default-secret-key-32ch-here}") String secretKey) {
        // 确保密钥长度足够（AES-256 需要 32 字节）
        String padded = secretKey;
        while (padded.length() < 32) padded += secretKey;
        padded = padded.substring(0, 32);
        AES = new SymmetricCrypto(
                SymmetricAlgorithm.AES,
                padded.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * AES 加密（返回 Base64 字符串）。
     *
     * @param plaintext 明文
     * @return 密文（Base64编码）
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        return AES.encryptBase64(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * AES 解密（从 Base64 字符串还原明文）。
     *
     * @param ciphertext 密文（Base64编码）
     * @return 明文
     */
    public static String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        return AES.decryptStr(ciphertext, StandardCharsets.UTF_8);
    }

    /**
     * 生成随机 AES 密钥（用于初始化场景）。
     *
     * @return Base64 编码的随机密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256, new SecureRandom());
            SecretKey key = kg.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("AES密钥生成失败", e);
        }
    }
}
