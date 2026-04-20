package com.example.basic.modules.coze.service;

import com.example.basic.modules.coze.entity.*;

/**
 * Coze 工作流服务接口。
 *
 * @author hermes-agent
 */
public interface CozeService {

    /** Bot 对话（同步） */
    CozeResponse chatSync(String query);

    /** Bot 对话（异步） */
    CozeResponse chatAsync(CozeRequest request);

    /** 轮询 Bot 对话结果 */
    CozeResponse pollChatResult(String conversationId, String messageId);

    /** 触发工作流 */
    CozeWorkflowResponse triggerWorkflow(CozeRequest request);

    /** 轮询工作流结果 */
    CozeWorkflowResponse pollWorkflowResult(String workflowRunId);

    /** 一句话对话 */
    CozeResponse chat(String query);
}
