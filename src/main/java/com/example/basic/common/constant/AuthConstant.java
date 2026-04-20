package com.example.basic.common.constant;

/**
 * 认证相关常量。
 *
 * <p>集中管理请求头名、请求属性 key 等，避免硬编码字符串。
 *
 * @author hermes-agent
 */
public class AuthConstant {

    /** 请求头名称：Authorization */
    public static final String AUTH_HEADER = "Authorization";

    /** Token 前缀（配合请求头使用：Bearer <token>） */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 请求属性中存用户ID的 key */
    public static final String USER_ID_KEY = "userId";

    /** 请求属性中存用户名的 key */
    public static final String USERNAME_KEY = "username";

    /** 请求属性中存用户角色的 key */
    public static final String ROLES_KEY = "roles";

    private AuthConstant() {
        // 禁止实例化
    }
}
