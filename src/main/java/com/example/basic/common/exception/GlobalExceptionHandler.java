package com.example.basic.common.exception;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.basic.common.result.Result;
import com.example.basic.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 全局统一异常处理器。
 *
 * <p>将各类异常统一封装为 {@link Result} 返回给前端，
 * 保证所有接口的响应格式一致性，便于前端统一处理。
 *
 * <p>异常处理顺序（从上到下匹配，精确优先）：
 * <ol>
 *   <li>业务异常 {@link BizException}           → 返回业务错误码</li>
 *   <li>参数校验异常 {@link MethodArgumentNotValidException}  → 返回 400</li>
 *   <li>文件上传超限                               → 返回 413</li>
 *   <li>404 未找到                                → 返回 404</li>
 *   <li>405 方法不支持                            → 返回 405</li>
 *   <li>其他未处理异常                            → 返回 500</li>
 * </ol>
 *
 * @author hermes-agent
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 通用业务异常。
     * 抛出此异常时需指定具体的 {@link ResultCode}，由前端展示友好错误信息。
     *
     * @param e        BizException
     * @param request  HTTP 请求（用于记录日志）
     * @return 统一响应
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e, HttpServletRequest request) {
        ResultCode rc = e.getResultCode();
        log.warn("⚠️ 业务异常 | URI={} | code={} | msg={}",
                request.getRequestURI(), rc.getCode(), rc.getMessage());
        return Result.fail(rc);
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @RequestBody 参数校验失败异常。
     * 常见于 @NotNull/@NotBlank/@Size 等注解不通过时。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {}", detail);
        return Result.fail(ResultCode.VALIDATION_FAILED, detail);
    }

    /**
     * 处理 @ModelAttribute 参数绑定异常（如类型转换失败）。
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String detail = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败: {}", detail);
        return Result.fail(ResultCode.BAD_REQUEST, detail);
    }

    /**
     * 处理缺少必需参数异常（如 @RequestParam("xxx") 但未传）。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = StrUtil.format("缺少必需参数: {}", e.getParameterName());
        log.warn(msg);
        return Result.fail(ResultCode.BAD_REQUEST, msg);
    }

    // ==================== 文件上传异常 ====================

    /**
     * 处理文件大小超出限制异常（HTTP 413 Payload Too Large）。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        return Result.fail(ResultCode.FILE_SIZE_EXCEEDED);
    }

    // ==================== 路由异常 ====================

    /**
     * 404：无对应处理器（需 application.yml 设置
     * spring.mvc.throw-exception-if-no-handler-found=true）。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(NoHandlerFoundException e) {
        log.warn("404 Not Found: {} {}", e.getHttpMethod(), e.getRequestURL());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 405：请求方法不支持（如接口只允许 GET，但前端发了 POST）。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = StrUtil.format("不支持 {} 方法", e.getMethod());
        log.warn(msg);
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED, msg);
    }

    // ==================== 安全异常 ====================

    /**
     * Spring Security 访问被拒绝（已登录但无权限）。
     * 配合 @PreAuthorize 等注解使用。
     */
    // @ExceptionHandler(AccessDeniedException.class)
    // @ResponseStatus(HttpStatus.FORBIDDEN)
    // public Result<?> handleAccessDenied(AccessDeniedException e) {
    //     log.warn("权限不足: {}", e.getMessage());
    //     return Result.fail(ResultCode.FORBIDDEN);
    // }

    // ==================== 其他未处理异常 ====================

    /**
     * 所有未显式处理的异常兜底。
     * 生产环境建议将详细异常信息记录到日志文件，仅返回通用提示给前端。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("💥 未处理异常 | URI={} | {}", request.getRequestURI(), e.getMessage(), e);
        // ⚠️ 生产环境：不要把异常信息返回给前端，防止信息泄露
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }

    // ==================== 内部类：业务异常快捷构造器 ====================

    /**
     * 业务异常快捷构造工具。
     * 用法: throw BizException.of(ResultCode.USER_NOT_FOUND);
     */
    public static class BizException extends RuntimeException {

        private final ResultCode resultCode;

        public BizException(ResultCode resultCode) {
            super(resultCode.getMessage());
            this.resultCode = resultCode;
        }

        public ResultCode getResultCode() {
            return resultCode;
        }

        /** 快捷构造：传入 ResultCode */
        public static BizException of(ResultCode rc) {
            return new BizException(rc);
        }

        /** 快捷构造：传入 ResultCode + 自定义消息 */
        public static BizException of(ResultCode rc, String customMessage) {
            return new BizException(ResultCode.builder()
                    .code(rc.getCode())
                    .message(customMessage)
                    .build());
        }
    }
}
