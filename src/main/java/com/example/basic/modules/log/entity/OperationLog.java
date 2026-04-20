package com.example.basic.modules.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 操作日志实体。
 *
 * <p>由 {@link com.example.basic.aspect.LogOperationAspect} AOP 切面自动写入，
 * 记录谁在什么时间做了什么操作、请求参数、执行结果和耗时。
 *
 * <p>存储结构：sys_operation_log 表（MyISAM 引擎，不支持事务，
 * 写入失败不影响主业务）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
@Schema(name = "操作日志")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述（如"新增用户"） */
    private String operation;

    /** 操作类型（如"USER"） */
    private String operationType;

    /** HTTP 请求方法 */
    private String requestMethod;

    /** 请求URL */
    private String requestUrl;

    /** 请求参数 */
    private String requestParams;

    /** 操作人 IP */
    private String ip;

    /** 是否成功：1=成功，0=失败 */
    private Integer success;

    /** 错误信息（失败时） */
    private String errorMsg;

    /** 执行耗时（毫秒） */
    private Integer durationMs;

    /** 操作时间 */
    private LocalDateTime createTime;
}
