package com.example.basic.common.result;

/**
 * 统一响应码枚举。
 * <p>
 * 遵循 HTTP 语义：
 * 2xx 成功 | 4xx 客户端错误 | 5xx 服务端错误
 * <p>
 * 自定义业务码占 5 位：前两位为分类，后三位为序号。
 */
public enum ResultCode {

    /* ----------- 成功 ----------- */
    SUCCESS(0, "操作成功"),

    /* ----------- 通用错误（4xx） ----------- */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /* ----------- 业务错误（5xxxx） ----------- */
    // 5xx01 ~ 5xx99：通用业务错误
    SYSTEM_ERROR(50001, "系统异常，请稍后重试"),
    DB_ERROR(50002, "数据库操作失败"),
    SERVICE_UNAVAILABLE(50003, "服务暂不可用"),

    // 51xxx：用户认证相关
    USER_NOT_FOUND(51001, "用户不存在"),
    USER_DISABLED(51002, "账号已被禁用"),
    PASSWORD_ERROR(51003, "密码错误"),
    TOKEN_EXPIRED(51004, "Token 已过期"),
    TOKEN_INVALID(51005, "Token 无效"),
    USERNAME_EXISTS(51006, "用户名已存在"),

    // 52xxx：业务数据相关
    RECORD_NOT_FOUND(52001, "记录不存在"),
    RECORD_CONFLICT(52002, "数据已存在，请勿重复操作"),
    VALIDATION_FAILED(52003, "数据校验失败"),
    FILE_UPLOAD_ERROR(52004, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(52005, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(52006, "文件大小超出限制"),
    ;

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据 code 反查枚举。
     *
     * @param code 响应码
     * @return 对应的枚举实例，未找到返回 SYSTEM_ERROR
     */
    public static ResultCode fromCode(int code) {
        for (ResultCode rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return SYSTEM_ERROR;
    }
}
