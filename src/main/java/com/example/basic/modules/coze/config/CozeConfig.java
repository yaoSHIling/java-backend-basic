package com.example.basic.modules.coze.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Coze 工作流配置。
 *
 * <p>配置项在 application.yml 中以 coze.* 前缀注入。
 *
 * <p>获取 API Token：https://www.coze.cn/open/api
 *
 * @author hermes-agent
 */
@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeConfig {

    /** Coze API 基础地址（国内版: coze.cn，国际版: coze.com） */
    private String baseUrl = "https://api.coze.cn";

    /** Coze 个人访问令牌 */
    private String apiToken;

    /** 默认 Bot ID */
    private String botId;

    /** 默认 Workflow ID */
    private String workflowId;

    /** 轮询间隔（秒） */
    private int pollInterval = 2;

    /** 轮询超时（秒） */
    private int pollTimeout = 60;

    /** 是否启用 Coze */
    private boolean enabled = true;
}
