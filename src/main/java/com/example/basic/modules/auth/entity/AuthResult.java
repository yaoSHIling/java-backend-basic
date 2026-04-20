package com.example.basic.modules.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 认证结果（登录成功后返回给前端）。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "认证结果")
public class AuthResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT Token，前端每次请求需在 Header 中携带：Authorization: Bearer <token>")
    private String token;

    @Schema(description = "Token 过期时间（时间戳，秒）")
    private Long expiresAt;

    @Schema(description = "当前登录用户名")
    private String username;

    @Schema(description = "用户 ID")
    private Long userId;
}
