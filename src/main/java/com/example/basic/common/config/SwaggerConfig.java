package com.example.basic.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 3（OpenAPI 3.0）配置。
 *
 * <p>启动后访问：
 * <ul>
 *   <li>Swagger UI:  http://localhost:8080/api/swagger-ui.html</li>
 *   <li>API 文档 JSON: http://localhost:8080/api/v3/api-docs</li>
 * </ul>
 *
 * <p>⚠️ 生产环境建议关闭 Swagger（设置 springdoc.enabled=false），
 * 避免接口文档暴露给外部。
 *
 * <p>常用注解说明：
 * <ul>
 *   <li>@Tag — 类名旁描述，标记接口分组</li>
 *   <li>@Operation — 方法名旁描述，标记具体接口</li>
 *   <li>@Parameter — 参数描述</li>
 *   <li>@Schema — 实体字段描述（用于生成文档中的请求/响应参数说明）</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 全局服务器配置（可指定多个，如测试环境、正式环境）
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("本地开发环境")
                ))
                // 项目基本信息
                .info(new Info()
                        .title("Java 后端快速开发脚手架 API")
                        .description("""
                                ## 项目定位

                                面向 **OA / CRM / 后台管理 / 接口服务** 等常见系统的 Java 后端快速开发脚手架。

                                内置功能：

                                | 模块 | 说明 |
                                |------|------|
                                | 用户管理 | 增删改查、分页、状态管理 |
                                | 登录认证 | JWT 无状态认证、Token 续期 |
                                | 文件上传 | 本地存储、大小/类型限制 |
                                | 数据字典 | 通用字典项（性别/状态/类型） |
                                | 参数配置 | 系统级键值对配置 |
                                | 操作日志 | AOP 记录所有变更操作 |

                                ## 认证方式

                                登录后获取 Token，在请求头中携带：

                                ```
                                Authorization: Bearer <your-token>
                                ```
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                );
    }
}
