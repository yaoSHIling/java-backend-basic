package com.example.basic.modules.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数。
 *
 * @author hermes-agent
 */
@Data
@Schema(name = "登录请求")
public class AuthUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码（明文）", example = "123456")
    private String password;

    @Schema(description = "记住我（延长 Token 有效期）", example = "false")
    private Boolean rememberMe = false;
}
