package com.example.basic.modules.workflow.engine;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

/**
 * 节点执行器实现（参考 Coze 官方节点类型）
 *
 * <p>包含以下节点：
 * <ul>
 *   <li>StartNode     - 开始节点（入口，自动产出一个 nextNodeId）</li>
 *   <li>EndNode       - 结束节点（终止流程，返回 output）</li>
 *   <li>LLMNode       - 大模型节点（调用 AI 接口）</li>
 *   <li>CodeNode      - 代码节点（脚本执行）</li>
 *   <li>ConditionNode - 条件分支节点（按表达式路由）</li>
 *   <li>ApprovalNode  - 审批节点（等待人工审批）</li>
 *   <li>HTTPNode      - HTTP 请求节点</li>
 *   <li>VariableNode  - 变量节点（读取/设置）</li>
 *   <li>LoopNode      - 循环节点（for/while/forEach）</li>
 *   <li>SubflowNode   - 子流程节点（调用子工作流）</li>
 *   <li>MessageNode   - 消息节点（发送通知）</li>
 *   <li>DatabaseNode  - 数据库节点（执行 SQL）</li>
 * </ul>
 */
@Slf4j
@Component
public class NodeExecutors {

    // ==================== 开始节点 ====================
    @Component
    public static class StartNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "start"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            // 开始节点：直接跳到下一节点，输入即参数
            String next = context.findNextNodeId(node.getId());
            return WfNodeResult.success(node.getId(), inputData, next, 0);
        }
    }

    // ==================== 结束节点 ====================
    @Component
    public static class EndNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "end"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            // 结束节点：流程正常终止
            context.setFinalOutput(inputData);
            return WfNodeResult.success(node.getId(), inputData, null, 0);
        }
    }

    // ==================== LLM 大模型节点 ====================
    @Component
    public static class LLMNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "llm"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String model = data.getModel() != null ? data.getModel() : "MiniMax-M*";
                VariableResolver resolver = new VariableResolver(inputData);
                String prompt = resolver.render(data.getPrompt());
                String systemPrompt = resolver.render(data.getSystemPrompt());

                log.info("LLM节点调用 | model={} | prompt={}", model,
                    prompt == null ? "" : prompt.substring(0, Math.min(50, prompt.length())));

                // TODO: 实际调用 AI 接口（MiniMax / OpenAI / Claude）
                // 这里先用占位符，后续接入 Coze API 或直接调用 MiniMax
                Map<String, Object> output = new HashMap<>();
                output.put("result", "\u672a\u5b9e\u73b0\uff1a\u9700\u63a5\u5165 AI \u63a5\u53e3");
                output.put("_prompt", prompt);
                output.put("_model", model);

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("LLM节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 代码节点 ====================
    @Component
    public static class CodeNodeExecutor implements NodeExecutor {
        private static final ScriptEngineManager SEM = new ScriptEngineManager();

        @Override public String nodeType() { return "code"; }
        @Override
        @SuppressWarnings("unchecked")
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String code = data.getCode();
                if (StrUtil.isBlank(code)) {
                    return WfNodeResult.failure(node.getId(), "代码为空", System.currentTimeMillis() - start);
                }

                ScriptEngine engine = SEM.getEngineByName(
                    "javascript".equals(data.getLanguage()) ? "JavaScript" : "JavaScript"
                );

                // 将 inputData 展开为变量
                for (Map.Entry<String, Object> e : inputData.entrySet()) {
                    engine.put(e.getKey(), e.getValue());
                }
                engine.put("_output", new HashMap<String, Object>());

                engine.eval(code);
                Map<String, Object> output = (Map<String, Object>) engine.get("_output");
                if (output == null) output = new HashMap<>();

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("代码节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 条件分支节点 ====================
    @Component
    public static class ConditionNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "condition"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                List<WfGraph.Branch> branches = data.getBranches();

                if (branches == null || branches.isEmpty()) {
                    // 没有分支，直接跳到默认
                    String next = context.findNextNodeId(node.getId());
                    return WfNodeResult.success(node.getId(), inputData, next, System.currentTimeMillis() - start);
                }

                // 遍历条件，找到第一个满足的分支
                for (WfGraph.Branch branch : branches) {
                    if (ConditionUtil.eval(branch.getConditionExpr(), inputData)) {
                        log.info("条件命中 | nodeId={} | branch={} | expr={}",
                            node.getId(), branch.getName(), branch.getConditionExpr());
                        Map<String, Object> output = new HashMap<>(inputData);
                        output.put("_branch", branch.getName());
                        return WfNodeResult.success(node.getId(), output, branch.getTargetNodeId(), System.currentTimeMillis() - start);
                    }
                }

                // 默认分支
                String defaultTarget = data.getDefaultBranch() != null
                    ? data.getDefaultBranch().getTargetNodeId()
                    : null;
                if (defaultTarget == null) defaultTarget = context.findNextNodeId(node.getId());

                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_branch", "default");
                return WfNodeResult.success(node.getId(), output, defaultTarget, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("条件节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 审批节点 ====================
    @Component
    public static class ApprovalNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "approval"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();

                // 渲染标题和内容
                VariableResolver resolver = new VariableResolver(inputData);
                String title = resolver.render(data.getTitleTemplate());
                String content = resolver.render(data.getContentTemplate());

                // 创建审批任务（入库，等待人工处理）
                Long taskId = context.createApprovalTask(
                    node.getId(),
                    node.getData().getName(),
                    data.getAssigneeType(),
                    data.getAssigneeExpr(),
                    title,
                    content
                );

                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_approvalTaskId", taskId);
                output.put("_approvalStatus", "pending");

                // 审批节点不自动推进，流程暂停等待审批结果
                return WfNodeResult.waiting(node.getId(), output);
            } catch (Exception e) {
                log.error("审批节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== HTTP 请求节点 ====================
    @Component
    public static class HTTPNodeExecutor implements NodeExecutor {
        private final RestTemplate restTemplate = new RestTemplate();

        @Override public String nodeType() { return "http"; }
        @Override
        @SuppressWarnings("unchecked")
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                VariableResolver resolver = new VariableResolver(inputData);
                String url = resolver.render(data.getUrl());
                String method = data.getMethod() != null ? data.getMethod() : "GET";

                HttpHeaders headers = new HttpHeaders();
                if (data.getHeaders() != null) {
                    data.getHeaders().forEach((k, v) -> headers.add(k, resolver.render(v)));
                }

                HttpEntity<Object> entity = new HttpEntity<>(resolver.renderMap(data.getBody()), headers);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.valueOf(method), entity, Map.class);

                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_httpStatus", response.getStatusCode().value());
                output.put("_httpBody", response.getBody());

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("HTTP节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 变量节点（读取/设置）====================
    @Component
    public static class VariableNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "variable"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                Map<String, Object> output = new HashMap<>(inputData);

                if (data.getVariableValue() != null) {
                    // 设置变量
                    Object value = data.getVariableValue() instanceof String
                        ? new VariableResolver(inputData).render((String) data.getVariableValue())
                        : data.getVariableValue();
                    context.setVariable(data.getVariableName(), value);
                    output.put(data.getVariableName(), value);
                } else {
                    // 读取变量（从 context 取出写入 output）
                    Object v = context.getVariable(data.getVariableName());
                    if (v != null) output.put(data.getVariableName(), v);
                }

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("变量节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 循环节点 ====================
    @Component
    public static class LoopNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "loop"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String loopType = data.getLoopType();
                int maxTimes = data.getMaxLoopTimes() != null ? data.getMaxLoopTimes() : 100;
                int times = data.getLoopTimes() != null ? data.getLoopTimes() : 1;

                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_loopTimes", times);
                output.put("_loopType", loopType);

                // 循环节点：记录循环上下文，返回第一个子节点
                context.setLoopContext(node.getId(), loopType, Math.min(times, maxTimes));

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("循环节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 子流程节点 ====================
    @Component
    public static class SubflowNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "subflow"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String subflowCode = data.getSubflowCode();
                Map<String, Object> subflowInput = data.getSubflowInput();

                log.info("子流程调用 | code={} | input={}", subflowCode, subflowInput);

                // TODO: 递归调用 WorkflowEngine 执行子流程
                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_subflowCode", subflowCode);
                output.put("_subflowResult", "\u672a\u5b9e\u73b0\uff1a\u5b50\u6d41\u7a0b\u8c03\u7528");

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("子流程节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 消息节点 ====================
    @Component
    public static class MessageNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "message"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String channel = data.getChannel() != null ? data.getChannel() : "log";
                VariableResolver resolver = new VariableResolver(inputData);
                String title = resolver.render(data.getTitle());
                String content = resolver.render(data.getContent());

                log.info("[{}] 消息发送 | title={} | to={}", channel, title, data.getToUser());

                // TODO: 根据 channel 调用对应通知渠道
                // channel: weixin / email / dingtalk / webhook / serverchan

                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_messageSent", true);
                output.put("_messageChannel", channel);

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("消息节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 数据库节点 ====================
    @Component
    public static class DatabaseNodeExecutor implements NodeExecutor {
        @Override public String nodeType() { return "database"; }
        @Override
        public WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context) {
            long start = System.currentTimeMillis();
            try {
                WfGraph.NodeData data = node.getData();
                String sql = new VariableResolver(inputData).render(data.getSql());
                Boolean isSelect = data.getIsSelect();

                log.info("数据库节点 | sql={} | isSelect={}", sql, isSelect);

                // TODO: 实际执行 SQL（通过 JdbcTemplate 或 MyBatis）
                Map<String, Object> output = new HashMap<>(inputData);
                output.put("_sql", sql);
                output.put("_sqlResult", "\u672a\u5b9e\u73b0\uff1aSQL\u6267\u884c");

                String next = context.findNextNodeId(node.getId());
                return WfNodeResult.success(node.getId(), output, next, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("数据库节点执行失败 | nodeId={}", node.getId(), e);
                return WfNodeResult.failure(node.getId(), e.getMessage(), System.currentTimeMillis() - start);
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 渲染模板字符串，将 {{ variable }} 替换为实际值
     * 支持嵌套引用，如：Hello {{ user.name }}
     */
    public static String renderTemplate(String template, Map<String, Object> vars) {
        return new VariableResolver(vars).render(template);
    }
}
