package com.example.basic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java 后端快速开发脚手架 · 启动入口
 *
 * <p>本项目为 Spring Boot 3 + MyBatis-Plus + JWT 的企业级后端脚手架，
 * 内置通用响应封装、全局异常处理、逻辑删除、雪花ID、常用工具类等基础设施，
 * 适合快速搭建 OA/CRM/后台管理/接口服务等常见系统。
 *
 * <p>访问接口文档（启动后）：
 * <ul>
 *   <li>Swagger UI:  http://localhost:8080/api/swagger-ui.html</li>
 *   <li>API 文档 JSON: http://localhost:8080/api/v3/api-docs</li>
 *   <li>Druid 监控:   http://localhost:8080/api/druid/</li>
 *   <li>健康检查:     http://localhost:8080/api/actuator/health</li>
 * </ul>
 *
 * @author hermes-agent
 */
@SpringBootApplication
@MapperScan("com.example.basic.dao")  // MyBatis-Plus：扫描所有 Mapper 接口所在包
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
