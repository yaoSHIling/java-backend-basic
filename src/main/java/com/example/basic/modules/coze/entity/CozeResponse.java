package com.example.basic.modules.coze.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

/**
 * Coze 对话响应。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Coze对话响应")
public class CozeResponse {

    @Schema(description = "Coze 返回码：0=成功，其他=失败")
    private int code;

    @Schema(description = "提示信息")
    private String msg;

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "对话 ID")
    private String conversationId;

    @Schema(description = "消息 ID")
    private String messageId;

    @Schema(description = "Bot 回复内容")
    private String content;

    @Schema(description = "消息列表")
    private List<CozeMessage> messages;

    @Schema(description = "异步状态：completed/in_progress")
    private String asyncStatus;

    public static CozeResponse error(int code, String msg) {
        return CozeResponse.builder().code(code).msg(msg).success(false).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CozeMessage {
        private String role;
        private String content;
        private String id;
    }
}
