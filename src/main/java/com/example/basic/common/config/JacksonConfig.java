package com.example.basic.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson JSON 配置。
 *
 * <p>解决常见问题：
 * <ul>
 *   <li>LocalDateTime 序列化/反序列化报错：配置 JavaTimeModule</li>
 *   <li>未知属性忽略：FAIL_ON_UNKNOWN_PROPERTIES = false</li>
 *   <li>日期默认写成数组而非字符串：WRITE_DATES_AS_TIMESTAMPS = false</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间类型支持（LocalDateTime / Instant 等）
        mapper.registerModule(new JavaTimeModule());

        // 日期序列化为 ISO-8601 字符串（而非时间戳数组）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 反序列化时忽略未知属性（前端多传字段不报错）
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 允许单引号作为字符串边界
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        return mapper;
    }
}
