package com.example.basic.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>主要配置：
 * <ul>
 *   <li>文件上传的本地访问路径（/files/** → ./uploads/files/）</li>
 *   <li>静态资源路径</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /files/** 请求映射到本地 uploads/files/ 目录
        // 前端访问：http://localhost:8080/api/files/2024/04/abc.jpg 即可看到上传的文件
        String absolutePath = "file:" + uploadDir + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(absolutePath);
    }
}
