package com.example.basic.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解。
 *
 * <p>基于 Redis 实现：提交后生成唯一 key（userId + method + args），
 * 在指定时间窗口内（默认 3 秒）重复提交会被拒绝。
 *
 * <p>配合 {@link com.example.basic.aspect.NoRepeatSubmitAspect} 使用。
 *
 * <p>示例：
 * <pre>
 * &#64;PostMapping("/submit")
 * &#64;NoRepeatSubmit(timeout = 5)  // 5 秒内不可重复提交
 * public Result<Void> submitOrder(&#64;RequestBody OrderForm form) { ... }
 * </pre>
 *
 * @author hermes-agent
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoRepeatSubmit {

    /**
     * 防重复提交的时间窗口，单位：秒。
     * 默认 3 秒内同一用户同一接口不可重复提交。
     */
    long timeout() default 3;
}
