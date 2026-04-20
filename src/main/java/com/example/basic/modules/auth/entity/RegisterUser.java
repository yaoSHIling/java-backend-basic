package com.example.basic.modules.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求参数。
 *
 * @author hermes-agent
 */
@Data
@Schema(name = "注册请求")
public class RegisterUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 位")
    @Schema(description = "用户名", example = "newuser")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    @Schema(description = "密码", example = "xxt123456")
    private String password;

    @Schema(description = "确认密码（需与密码一致）", example = "xxt123456")
    private String confirmPassword;

    @Schema(description = "邮箱（可选）", example = "user@example.com")
    private String email;

    @Schema(description = "手机号（可选）", example = "13800138000")
    private String phone;
}
