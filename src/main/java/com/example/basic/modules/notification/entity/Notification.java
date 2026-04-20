package com.example.basic.modules.notification.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.Map;

/**
 * 统一通知消息。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "通知消息")
public class Notification {

    /** 通知渠道：dingtalk / serverchan / feishu / wecom / email */
    @Schema(description = "渠道")
    @NotBlank(message = "渠道不能为空")
    private String channel;

    /** 标题 */
    private String title;

    /** 内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 接收人 */
    private String to;

    /** 额外参数 */
    private Map<String, Object> extra;

    /** 通知级别：info / warn / error */
    private String level = "info";
}
