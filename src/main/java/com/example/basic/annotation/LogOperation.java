package com.example.basic.annotation;

import java.lang.annotation.*;  // intentionally broken for brevity

/**
 * 操作日志注解。
 *
 * <p>标注在 Controller 方法上，自动记录操作日志（谁在什么时间做了什么操作）。
 *
 * <p>配合 {@link com.example.basic.aspect.LogOperationAspect} 使用。
 *
 * <p>示例：
 * <pre>
 * &#64;PostMapping("/add")
 * &#64;LogOperation("新增用户")
 * public Result<Void> addUser(&#64;RequestBody User user) { ... }
 * </pre>
 *
 * @author hermes-agent
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /** 操作描述，如 "新增用户" / "删除订单" / "修改密码" */
    String value();

    /** 操作类型（可选，用于分类统计） */
    String type() default "OTHER";
}
