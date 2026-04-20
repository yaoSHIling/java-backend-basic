package com.example.basic.aspect;

import com.example.basic.annotation.LogOperation;
import com.example.basic.common.constant.AuthConstant;
import com.example.basic.modules.log.entity.OperationLog;
import com.example.basic.modules.log.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面。
 *
 * <p>拦截标注了 @LogOperation 的方法，在方法执行前后自动记录操作日志，
 * 包括：操作用户、操作时间、操作描述、请求参数、执行结果、耗时等。
 *
 * <p>日志数据写入 sys_operation_log 表，支持查询统计。
 *
 * @author hermes-agent
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogOperationAspect {

    private final OperationLogService operationLogService;

    /** 切入点：所有标注了 @LogOperation 注解的方法 */
    @Around("@annotation(com.example.basic.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();

        // ========== 1. 提取注解信息 ==========
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogOperation logAnnotation = method.getAnnotation(LogOperation.class);
        String operation = logAnnotation.value();    // 如 "新增用户"
        String operationType = logAnnotation.type(); // 如 "USER"

        // ========== 2. 提取请求信息 ==========
        HttpServletRequest request = getRequest();
        String url = request != null ? request.getRequestURI() : "";
        String httpMethod = request != null ? request.getMethod() : "";
        String ip = request != null ? request.getRemoteAddr() : "";

        // ========== 3. 提取当前用户 ==========
        Long userId = null;
        String username = null;
        if (request != null) {
            Object uid = request.getAttribute(AuthConstant.USER_ID_KEY);
            Object uname = request.getAttribute(AuthConstant.USERNAME_KEY);
            if (uid instanceof Long) userId = (Long) uid;
            if (uname instanceof String) username = (String) uname;
        }

        // ========== 4. 执行目标方法并记录结果 ==========
        Object result = null;
        boolean success = true;
        String errorMsg = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // ========== 5. 写入日志 ==========
            long costMs = System.currentTimeMillis() - start;

            try {
                OperationLog logEntry = OperationLog.builder()
                        .userId(userId)
                        .username(username)
                        .operation(operation)
                        .operationType(operationType)
                        .requestMethod(httpMethod)
                        .requestUrl(url)
                        .requestParams(getParams(point))
                        .ip(ip)
                        .success(success ? 1 : 0)
                        .errorMsg(truncate(errorMsg, 500))
                        .durationMs((int) costMs)
                        .createTime(LocalDateTime.now())
                        .build();
                operationLogService.saveAsync(logEntry);
            } catch (Exception e) {
                // 日志记录失败不能影响主业务，仅打印警告
                log.warn("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 提取方法调用参数（转 JSON 字符串，过滤敏感字段）。
     */
    private String getParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) return "";
            // 取第一个参数（通常是表单/Body）
            return args[0] != null ? args[0].toString() : "";
        } catch (Exception e) {
            return "<解析失败>";
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
