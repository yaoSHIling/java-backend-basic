package com.example.basic.annotation;

import java.lang.annotation.*;

/**
 * 标记方法需要登录认证才能访问。
 *
 * <p>配合 {@link com.example.basic.filter.LoginAuthInterceptor} 使用。
 * 放在 Controller 方法上，标注该接口需要登录。
 *
 * <p>示例：
 * <pre>
 * &#64;PostMapping("/update")
 * &#64;Login  // 必须登录才能访问
 * public Result<String> updateProfile(&#64;RequestBody UserForm form) { ... }
 * </pre>
 *
 * @author hermes-agent
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {
}
