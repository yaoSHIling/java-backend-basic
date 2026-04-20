package com.example.basic.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 *
 * <p>提供 Token 的生成、解析、验证功能。
 *
 * <p>Token 存储内容：
 * <ul>
 *   <li>sub — 用户ID</li>
 *   <li>username — 用户名</li>
 *   <li>iat — 签发时间</li>
 *   <li>exp — 过期时间</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:604800000}")
    private long expiration; // 默认7天（毫秒）

    /**
     * 生成 JWT Token。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))          // 用户ID放sub
                .claim("username", username)               // 用户名
                .issuedAt(now)                              // 签发时间
                .expiration(expiryDate)                    // 过期时间
                .signWith(getSigningKey())                 // 签名（HS512算法）
                .compact();
    }

    /**
     * 从 Token 中解析用户ID。
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从 Token 中解析用户名。
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否合法（签名正确且未过期）。
     *
     * @param token JWT 字符串
     * @return true=合法，false=无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT 格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 签名失败: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT 验证异常: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 判断 Token 是否已过期。
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // ==================== 内部方法 ====================

    /** 解析 Token，返回 Claims */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 获取签名密钥（自动适配密钥长度） */
    private SecretKey getSigningKey() {
        // 密钥长度至少 256bit（HS256），不够则重复填充
        String paddedSecret = secret;
        while (paddedSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            paddedSecret += secret;
        }
        return Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
