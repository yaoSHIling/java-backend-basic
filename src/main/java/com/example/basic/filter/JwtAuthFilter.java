package com.example.basic.filter;

import com.example.basic.common.constant.AuthConstant;
import com.example.basic.common.result.Result;
import com.example.basic.common.result.ResultCode;
import com.example.basic.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器。
 *
 * <p>拦截所有请求，从请求头 Authorization 中提取 JWT Token 并验证：
 * <ul>
 *   <li>Token 有效 → 解析用户 ID，放入请求属性供后续使用</li>
 *   <li>Token 缺失或无效 → 返回 401 Unauthorized，前端跳转登录页</li>
 * </ul>
 *
 * <p>白名单路径（无需登录）：
 * <ul>
 *   <li>登录接口：/auth/login</li>
 *   <li>注册接口：/auth/register</li>
 *   <li>公开静态资源：/swagger-ui/**, /v3/api-docs/**, /druid/**</li>
 * </ul>
 *
 * <p>⚠️ 注意：此过滤器仅验证 Token 合法性，不做权限校验。
 * 权限校验请使用 @Login 和对应的 Interceptor。
 *
 * @author hermes-agent
 * @see JwtUtil
 */
@Slf4j
@Component
@WebFilter(urlPatterns = "/**")
@Order(2)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ========== 1. 放行白名单路径 ==========
        if (isWhitelistPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ========== 2. 提取 Token ==========
        String authHeader = request.getHeader(AuthConstant.AUTH_HEADER);
        if (!StringUtils.hasText(authHeader)) {
            writeUnauthorizedResponse(response, "未携带认证 Token");
            return;
        }

        // 去掉 "Bearer " 前缀（如果有）
        String token = authHeader;
        if (authHeader.startsWith(AuthConstant.TOKEN_PREFIX)) {
            token = authHeader.substring(AuthConstant.TOKEN_PREFIX.length()).trim();
        }

        // ========== 3. 验证 Token ==========
        try {
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorizedResponse(response, "Token 无效或已过期");
                return;
            }

            // ========== 4. 解析用户信息并放入请求 ==========
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 放入请求属性，后续 Controller / Service 可直接获取
            request.setAttribute(AuthConstant.USER_ID_KEY, userId);
            request.setAttribute(AuthConstant.USERNAME_KEY, username);

            log.debug("JWT 认证成功 | userId={} | username={}", userId, username);

            // 放行请求
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.warn("JWT 验证失败 | {} | {}", path, e.getMessage());
            writeUnauthorizedResponse(response, "Token 验证失败");
        }
    }

    /**
     * 判断路径是否在白名单中（无需登录）。
     */
    private boolean isWhitelistPath(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/druid/")
                || path.startsWith("/actuator/")
                || path.equals("/favicon.ico");
    }

    /**
     * 返回 401 JSON 响应。
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Result<?> result = Result.fail(ResultCode.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
