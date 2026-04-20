package com.example.basic.modules.coze.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.modules.coze.entity.*;
import com.example.basic.modules.coze.service.CozeService;
import com.example.basic.util.StrUtil;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Coze AI 工作流接口。
 *
 * <p>配置：application.yml 中的 coze.* 配置块。
 *
 * @author hermes-agent
 */
@Tag(name = "08. Coze工作流", description = "Coze AI 工作流/对话集成")
@RestController
@RequestMapping("/coze")
@RequiredArgsConstructor
public class CozeController {

    private final CozeService cozeService;

    @Operation(summary = "AI 对话（同步）", description = "向 Coze Bot 发送消息，立即返回 AI 回复")
    @PostMapping("/chat")
    @Login
    @LogOperation("Coze AI 对话")
    public Result<CozeResponse> chat(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (StrUtil.isBlank(query)) return Result.fail(400, "query 不能为空");
        return Result.success(cozeService.chat(query));
    }

    @Operation(summary = "AI 对话（异步）", description = "异步发起对话，返回 conversationId，前端轮询获取结果")
    @PostMapping("/chat/async")
    @Login
    @LogOperation("Coze 异步对话")
    public Result<CozeResponse> chatAsync(@RequestBody CozeRequest request) {
        return Result.success(cozeService.chatAsync(request));
    }

    @Operation(summary = "轮询异步对话结果")
    @GetMapping("/chat/poll")
    @Login
    public Result<CozeResponse> pollChat(
            @Parameter(description = "对话ID") @RequestParam String conversationId,
            @Parameter(description = "消息ID") @RequestParam String messageId) {
        return Result.success(cozeService.pollChatResult(conversationId, messageId));
    }

    @Operation(summary = "触发工作流（异步）", description = "触发 Coze 平台上的 AI 工作流，返回 workflowRunId")
    @PostMapping("/workflow/trigger")
    @Login
    @LogOperation("Coze 工作流触发")
    public Result<CozeWorkflowResponse> triggerWorkflow(@RequestBody CozeRequest request) {
        return Result.success(cozeService.triggerWorkflow(request));
    }

    @Operation(summary = "轮询工作流结果")
    @GetMapping("/workflow/poll")
    @Login
    public Result<CozeWorkflowResponse> pollWorkflow(
            @Parameter(description = "工作流执行ID") @RequestParam String workflowRunId) {
        return Result.success(cozeService.pollWorkflowResult(workflowRunId));
    }

    @Operation(summary = "执行工作流（同步）", description = "触发工作流并同步等待结果，适合执行时间较短的工作流")
    @PostMapping("/workflow/run")
    @Login
    @LogOperation("Coze 工作流同步执行")
    public Result<CozeWorkflowResponse> runWorkflow(@RequestBody CozeRequest request) {
        CozeWorkflowResponse trigger = cozeService.triggerWorkflow(request);
        if (trigger.getWorkflowRunId() != null && !trigger.getWorkflowRunId().isEmpty()) {
            CozeWorkflowResponse result = cozeService.pollWorkflowResult(trigger.getWorkflowRunId());
            return Result.success(result);
        }
        return Result.success(trigger);
    }
}
