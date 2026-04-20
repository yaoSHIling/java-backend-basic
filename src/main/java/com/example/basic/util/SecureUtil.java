package com.example.basic.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 安全加密工具（封装 Hutool）。
 *
 * @author hermes-agent
 */
public class SecureUtil {

    public static String md5(String s)           { return DigestUtil.md5Hex(s); }
    public static String md5Salt(String t, String salt) { return DigestUtil.md5Hex(md5(t) + salt); }
    public static String sha256(String s)         { return DigestUtil.sha256Hex(s); }
    public static String sha256Salt(String p, String salt) { return DigestUtil.sha256Hex(p + salt); }
    public static boolean verifyPwd(String inp, String salt, String stored) { return sha256Salt(inp, salt).equals(stored); }

    public static String aesEnc(String p, String k) {
        String pk = padKey(k, 32);
        AES aes = new AES(SymmetricAlgorithm.AES, pk.getBytes(StandardCharsets.UTF_8));
        return aes.encryptBase64(p, StandardCharsets.UTF_8);
    }
    public static String aesDec(String c, String k) {
        String pk = padKey(k, 32);
        AES aes = new AES(SymmetricAlgorithm.AES, pk.getBytes(StandardCharsets.UTF_8));
        return aes.decryptStr(c, StandardCharsets.UTF_8);
    }
    private static String padKey(String k, int len) {
        if (k == null) k = "";
        StringBuilder sb = new StringBuilder(k);
        while (sb.length() < len) sb.append(k);
        return sb.substring(0, len);
    }

    public static String base64Enc(String s)    { return Base64.encode(s, StandardCharsets.UTF_8); }
    public static String base64Dec(String e)    { return Base64.decodeStr(e, StandardCharsets.UTF_8); }
    public static String uuid()                  { return IdUtil.fastSimpleUUID(); }
    public static String uuidDash()              { return UUID.randomUUID().toString(); }
    public static int randInt(int min, int max)  { return RandomUtil.randomInt(min, max + 1); }
    public static String randCode(int n)         { return RandomUtil.randomNumbers(n); }
    public static String randStr(int n)          { return RandomUtil.randomString(n); }
    public static String randLetter(int n)       { return RandomUtil.randomLetters(n); }
    public static String genSalt()               { return uuid().substring(0, 16); }
}
