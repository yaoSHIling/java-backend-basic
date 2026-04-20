package com.example.basic.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域（CORS）配置。
 *
 * <p>允许前端项目（不同域名/端口）调用本后端接口。
 *
 * <p>如果只需要给某个接口开跨域，最简单的方式是在
 * Controller 或方法上加 @CrossOrigin(origins = "http://localhost:3000")。
 * 这里做全局配置，一劳永逸。
 *
 * @author hermes-agent
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ========== 允许的来源 ==========
        // ⚠️ 生产环境请改为具体前端域名，如：http://localhost:3000
        // 多个域名用逗号分隔
        config.addAllowedOriginPattern("*");

        // ========== 允许的请求头 ==========
        config.addAllowedHeader("*");  // 允许所有请求头

        // ========== 允许的 HTTP 方法 ==========
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS"); // 预检请求

        // ========== 是否允许携带凭证（Cookie） ==========
        // 如果开启，前端 origin 不能用 *，必须指定具体域名
        config.setAllowCredentials(false);

        // ========== 预检请求缓存时间（秒） ==========
        // 在此时间内，浏览器不会重复发 OPTIONS 预检请求
        config.setMaxAge(3600L);

        // ========== 暴露响应头 ==========
        // 允许前端通过 Access-Control-Expose-Headers 访问这些响应头
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
