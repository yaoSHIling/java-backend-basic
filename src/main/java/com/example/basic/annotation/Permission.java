package com.example.basic.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解。
 *
 * <p>标注在方法上，指定访问该接口所需的权限标识。
 * 配合 Spring Security 或自定义权限拦截器使用。
 *
 * <p>示例：
 * <pre>
 * &#64;PostMapping("/delete")
 * &#64;Login
 * &#64;Permission("user:delete")  // 需要 user:delete 权限
 * public Result<Void> deleteUser(&#64;RequestParam Long id) { ... }
 * </pre>
 *
 * @author hermes-agent
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    /** 所需权限标识，多个用逗号分隔 */
    String value();

    /** 权限校验逻辑，默认 AND（所有权限都满足才行） */
    String logic() default "AND";
}
