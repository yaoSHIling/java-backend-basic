package com.example.basic.modules.coze.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.coze.config.CozeConfig;
import com.example.basic.modules.coze.entity.*;
import com.example.basic.modules.coze.service.CozeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Coze 工作流服务实现。
 *
 * <p>调用 Coze 开放平台 API：
 * <ul>
 *   <li>POST /open/api/v2/chat — 发起 Bot 对话</li>
 *   <li>GET  /open/api/v2/chat/retrieve — 查询对话结果</li>
 *   <li>POST /open/api/v1/workflow/run — 触发工作流</li>
 *   <li>GET  /open/api/v1/workflow/run/retrieve — 查询工作流结果</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CozeServiceImpl implements CozeService {

    private final CozeConfig config;

    @PostConstruct
    public void init() {
        if (StrUtil.isBlank(config.getApiToken())) {
            log.warn("⚠️ Coze API Token 未配置，Coze 功能已禁用");
        }
    }

    @Override
    public CozeResponse chatSync(String query) { return chat(query); }

    @Override
    public CozeResponse chat(String query) {
        return chatAsync(CozeRequest.builder().query(query).stream(false).botType("chat").build());
    }

    @Override
    public CozeResponse chatAsync(CozeRequest request) {
        checkEnabled();
        String botId = StrUtil.blankToDefault(request.getBotId(), config.getBotId());
        if (StrUtil.isBlank(botId)) throw new BizException(ResultCode.BAD_REQUEST, "Bot ID 不能为空");

        String url = config.getBaseUrl() + "/open/api/v2/chat";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bot_id", botId);
        body.put("user_id", "backend-user");
        body.put("stream", false);
        body.put("messages", List.of(Map.of("role", "user", "content", request.getQuery())));
        if (StrUtil.isNotBlank(request.getConversationId())) {
            body.put("conversation_id", request.getConversationId());
        }
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            body.put("variables", request.getVariables());
        }

        log.debug("Coze 对话请求 | botId={}", botId);
        try {
            String raw = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + config.getApiToken())
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(body))
                    .timeout(30000).execute().body();
            return parseChatResponse(raw);
        } catch (Exception e) {
            log.error("Coze 对话请求失败", e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "Coze 服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public CozeResponse pollChatResult(String conversationId, String messageId) {
        checkEnabled();
        String url = config.getBaseUrl() + "/open/api/v2/chat/retrieve?conversation_id=" + conversationId + "&chat_id=" + messageId;
        long start = System.currentTimeMillis();
        long maxWait = config.getPollTimeout() * 1000L;
        while (System.currentTimeMillis() - start < maxWait) {
            try {
                String raw = HttpRequest.get(url)
                        .header("Authorization", "Bearer " + config.getApiToken())
                        .header("Content-Type", "application/json")
                        .timeout(10000).execute().body();
                JSONObject resp = JSON.parseObject(raw);
                JSONObject data = resp.getJSONObject("data");
                if (data == null) break;
                String status = data.getString("status");
                if ("completed".equalsIgnoreCase(status)) {
                    return CozeResponse.builder()
                            .code(0).msg("success").success(true)
                            .conversationId(conversationId)
                            .content(extractContent(data.getJSONArray("messages")))
                            .asyncStatus(status).build();
                }
                if ("failed".equalsIgnoreCase(status)) return CozeResponse.error(-1, "对话执行失败");
                Thread.sleep(config.getPollInterval() * 1000L);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); break;
            } catch (Exception e) { log.warn("轮询失败: {}", e.getMessage()); }
        }
        return CozeResponse.error(-1, "轮询超时");
    }

    @Override
    public CozeWorkflowResponse triggerWorkflow(CozeRequest request) {
        checkEnabled();
        String workflowId = StrUtil.blankToDefault(request.getWorkflowId(), config.getWorkflowId());
        if (StrUtil.isBlank(workflowId)) throw new BizException(ResultCode.BAD_REQUEST, "workflowId 不能为空");

        String url = config.getBaseUrl() + "/open/api/v1/workflow/run";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", workflowId);
        body.put("is_async", true);
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            body.put("parameters", request.getVariables());
        }

        log.info("Coze 工作流触发 | workflowId={}", workflowId);
        try {
            String raw = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + config.getApiToken())
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(body))
                    .timeout(30000).execute().body();
            JSONObject resp = JSON.parseObject(raw);
            if (resp.getIntValue("code") != 0) {
                return CozeWorkflowResponse.builder()
                        .code(resp.getIntValue("code")).msg(resp.getString("msg")).build();
            }
            JSONObject data = resp.getJSONObject("data");
            return CozeWorkflowResponse.builder()
                    .code(0).msg("success")
                    .workflowRunId(data.getString("workflow_run_id"))
                    .status(data.getString("status"))
                    .workflowRunStartAt(data.getLong("workflow_run_start_at"))
                    .workflowRunEndAt(data.getLong("workflow_run_end_at"))
                    .workflowRunDuration(data.getLong("workflow_run_duration"))
                    .output(data.get("output")).build();
        } catch (Exception e) {
            log.error("Coze 工作流触发失败", e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "Coze 工作流调用失败: " + e.getMessage());
        }
    }

    @Override
    public CozeWorkflowResponse pollWorkflowResult(String workflowRunId) {
        checkEnabled();
        String url = config.getBaseUrl() + "/open/api/v1/workflow/run/retrieve?workflow_run_id=" + workflowRunId;
        long start = System.currentTimeMillis();
        long maxWait = config.getPollTimeout() * 1000L;
        while (System.currentTimeMillis() - start < maxWait) {
            try {
                String raw = HttpRequest.get(url)
                        .header("Authorization", "Bearer " + config.getApiToken())
                        .header("Content-Type", "application/json")
                        .timeout(10000).execute().body();
                CozeWorkflowResponse r = JSON.parseObject(raw, CozeWorkflowResponse.class);
                if (r == null) break;
                log.debug("轮询 | workflowRunId={} | status={}", workflowRunId, r.getStatus());
                if (r.isCompleted()) { log.info("Coze 工作流完成 | workflowRunId={}", workflowRunId); return r; }
                if (r.isFailed()) return r;
                Thread.sleep(config.getPollInterval() * 1000L);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); break;
            } catch (Exception e) { log.warn("轮询失败: {}", e.getMessage()); }
        }
        return CozeWorkflowResponse.builder().code(-1).msg("轮询超时").status("timeout").build();
    }

    // ===== 内部方法 =====

    private void checkEnabled() {
        if (!config.isEnabled()) throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "Coze 功能已禁用");
        if (StrUtil.isBlank(config.getApiToken())) throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "Coze API Token 未配置");
    }

    private CozeResponse parseChatResponse(String raw) {
        JSONObject resp = JSON.parseObject(raw);
        if (resp.getIntValue("code") != 0) return CozeResponse.error(resp.getIntValue("code"), resp.getString("msg"));
        JSONObject data = resp.getJSONObject("data");
        if (data == null) return CozeResponse.error(-1, "Coze 返回数据为空");
        JSONArray msgArr = data.getJSONArray("messages");
        String content = extractContent(msgArr);
        return CozeResponse.builder()
                .code(0).msg("success").success(true)
                .conversationId(data.getString("id"))
                .content(content)
                .mode("sync")
                .messages(extractMessages(msgArr)).build();
    }

    private String extractContent(JSONArray arr) {
        if (arr == null || arr.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject msg = arr.getJSONObject(i);
            if ("assistant".equalsIgnoreCase(msg.getString("role"))) {
                String c = msg.getString("content");
                if (StrUtil.isNotBlank(c)) { sb.append(c.trim()); if (i < arr.size()-1) sb.append("\n"); }
            }
        }
        return sb.toString().trim();
    }

    private List<CozeResponse.CozeMessage> extractMessages(JSONArray arr) {
        if (arr == null || arr.isEmpty()) return new ArrayList<>();
        List<CozeResponse.CozeMessage> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject msg = arr.getJSONObject(i);
            list.add(CozeResponse.CozeMessage.builder()
                    .role(msg.getString("role"))
                    .content(msg.getString("content"))
                    .id(msg.getString("id")).build());
        }
        return list;
    }
}
