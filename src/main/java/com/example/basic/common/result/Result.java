package com.example.basic.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应封装。
 *
 * <p>所有接口返回值统一为此格式，前端无需处理多种响应格式。
 *
 * <p>格式示例：
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },
 *   "timestamp": 1713001234567
 * }
 * </pre>
 *
 * <p>前端只需判断 code === 200，即成功；否则取 message 展示错误。
 *
 * @param <T> data 字段的数据类型
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "统一响应")
public class Result<T> {

    /** 响应状态码：200=成功，其他=失败（HTTP语义码或业务码） */
    @Schema(description = "状态码：200=成功，其他=失败")
    private int code;

    /** 提示信息：成功时为"操作成功"，失败时为具体原因 */
    @Schema(description = "提示信息")
    private String message;

    /** 响应数据（泛型，具体类型由接口定义） */
    @Schema(description = "响应数据")
    private T data;

    /** 响应时间戳（毫秒），可用于防重放攻击校验 */
    @Schema(description = "响应时间戳（毫秒）")
    private long timestamp;

    // ==================== 静态工厂方法 ====================

    /** 成功响应（无数据） */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 成功响应（有数据） */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 成功响应（自定义消息） */
    public static <T> Result<T> success(T data, String message) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 失败响应（使用 ResultCode） */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 失败响应（ResultCode + 自定义消息） */
    public static <T> Result<T> fail(ResultCode resultCode, String customMessage) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(customMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 失败响应（直接传 code + message） */
    public static <T> Result<T> fail(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
