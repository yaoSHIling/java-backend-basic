package com.example.basic.modules.coze.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.Map;

/**
 * Coze 工作流/对话请求参数。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Coze请求")
public class CozeRequest {

    @Schema(description = "Bot ID")
    private String botId;

    @Schema(description = "Workflow ID")
    private String workflowId;

    @Schema(description = "用户输入")
    @NotBlank(message = "query 不能为空")
    private String query;

    @Schema(description = "对话 ID（多轮对话上下文）")
    private String conversationId;

    @Schema(description = "执行模式：sync=同步，async=异步")
    private String botType = "chat";

    @Schema(description = "额外参数（传递给工作流的自定义变量）")
    private Map<String, Object> variables;
}
