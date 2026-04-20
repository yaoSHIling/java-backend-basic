package com.example.basic.modules.auth.service;

import com.example.basic.modules.auth.entity.AuthResult;
import com.example.basic.modules.auth.entity.AuthUser;
import com.example.basic.modules.auth.entity.RegisterUser;

/**
 * 认证服务接口。
 *
 * @author hermes-agent
 */
public interface AuthService {

    /**
     * 用户登录，验证用户名密码，签发 JWT Token。
     *
     * @param authUser 登录参数（用户名 + 密码）
     * @return 认证结果（Token + 过期时间）
     */
    AuthResult login(AuthUser authUser);

    /**
     * 用户注册（新增账号）。
     *
     * @param registerUser 注册参数
     */
    void register(RegisterUser registerUser);
}
