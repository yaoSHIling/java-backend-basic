package com.example.basic.aspect;

import cn.hutool.crypto.SecureUtil;
import com.example.basic.annotation.NoRepeatSubmit;
import com.example.basic.common.constant.AuthConstant;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.Result;
import com.example.basic.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交 AOP 切面。
 *
 * <p>基于 Redis 实现：
 * <ul>
 *   <li>方法被首次调用时，在 Redis 中写入唯一 key（用户+接口+参数摘要）</li>
 *   <li>在 timeout 时间窗口内再次调用，返回错误提示</li>
 *   <li>超时后 key 自动失效，可再次提交</li>
 * </ul>
 *
 * <p>配合 @NoRepeatSubmit 注解使用：
 * <pre>
 * &#64;PostMapping("/submit")
 * &#64;NoRepeatSubmit(timeout = 5)
 * public Result<Void> submit(...) { ... }
 * </pre>
 *
 * @author hermes-agent
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NoRepeatSubmitAspect {

    private final StringRedisTemplate redisTemplate;

    private static final String REPEAT_KEY_PREFIX = "repeat:submit:";

    @Around("@annotation(com.example.basic.annotation.NoRepeatSubmit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // ========== 1. 提取 @NoRepeatSubmit 注解参数 ==========
        MethodSignature signature = (MethodSignature) point.getSignature();
        int timeoutSec = signature.getMethod()
                .getAnnotation(NoRepeatSubmit.class).timeout();

        // ========== 2. 构建 Redis key ==========
        String repeatKey = buildRepeatKey(point);
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute(AuthConstant.USER_ID_KEY);
            if (userId != null) {
                repeatKey = REPEAT_KEY_PREFIX + userId + ":" + repeatKey;
            } else {
                // 未登录用户：按 IP + key 限制
                repeatKey = REPEAT_KEY_PREFIX + "ip:" + request.getRemoteAddr() + ":" + repeatKey;
            }
        }

        // ========== 3. 检查 Redis 中是否已有记录 ==========
        Boolean exists = redisTemplate.hasKey(repeatKey);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("重复提交拦截 | key={}", repeatKey);
            throw new BizException(ResultCode.TOO_MANY_REQUESTS);
        }

        // ========== 4. 写入 Redis，设置过期时间 ==========
        redisTemplate.opsForValue().set(repeatKey, "1", timeoutSec, TimeUnit.SECONDS);

        // ========== 5. 执行目标方法 ==========
        try {
            return point.proceed();
        } catch (BizException e) {
            throw e;
        } catch (Throwable e) {
            // 方法执行出错，删除 Redis key（允许立即重试）
            redisTemplate.delete(repeatKey);
            throw e;
        }
    }

    /**
     * 构建方法级唯一 key。
     * 格式：类名.方法名 + 参数摘要（MD5）
     */
    private String buildRepeatKey(ProceedingJoinPoint point) {
        String className = point.getSignature().getDeclaringType().getSimpleName();
        String methodName = point.getSignature().getName();
        String argsStr = point.getArgs() != null
                ? cn.hutool.core.collection.CollUtil.join(point.getArgs(), ",")
                : "";
        String md5 = SecureUtil.md5(argsStr);
        return className + "." + methodName + ":" + md5;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
