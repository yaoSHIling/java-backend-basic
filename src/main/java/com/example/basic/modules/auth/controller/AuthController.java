package com.example.basic.modules.auth.controller;

import com.example.basic.annotation.LogOperation;
import com.example.basic.annotation.NoRepeatSubmit;
import com.example.basic.common.result.Result;
import com.example.basic.modules.auth.entity.AuthResult;
import com.example.basic.modules.auth.entity.AuthUser;
import com.example.basic.modules.auth.entity.RegisterUser;
import com.example.basic.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口（登录 / 注册）。
 *
 * <p>公开接口，无需登录即可访问。
 *
 * @author hermes-agent
 */
@Tag(name = "01. 认证管理", description = "登录、注册")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录。
     * 成功后返回 JWT Token，前端在后续请求的 Header 中携带：
     * Authorization: Bearer <token>
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @NoRepeatSubmit(timeout = 5)  // 5秒内防重复提交
    public Result<AuthResult> login(@Valid @RequestBody AuthUser authUser) {
        AuthResult result = authService.login(authUser);
        return Result.success(result);
    }

    /**
     * 用户注册。
     * 注册成功后需重新登录获取 Token。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @NoRepeatSubmit(timeout = 10) // 注册操作 10 秒防重复
    @LogOperation(value = "用户注册", type = "AUTH")
    public Result<String> register(@Valid @RequestBody RegisterUser registerUser) {
        authService.register(registerUser);
        return Result.success("注册成功，请重新登录");
    }

    /**
     * 获取当前登录用户信息（需携带 Token）。
     * 前端可在进入主页时调用此接口获取用户信息并保存到本地。
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(
            @RequestAttribute(name = "userId", required = false) Long userId,
            @RequestAttribute(name = "username", required = false) String username) {
        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("username", username);
        return Result.success(info);
    }

    /**
     * 退出登录（前端删除 Token 即可，此接口仅作记录）。
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @LogOperation(value = "退出登录", type = "AUTH")
    public Result<String> logout() {
        // JWT 无状态，退出登录由前端删除 Token 即可
        // 如需服务端主动失效，可在 Redis 中维护黑名单
        return Result.success("已退出登录");
    }
}
