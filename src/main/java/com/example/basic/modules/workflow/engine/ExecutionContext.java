package com.example.basic.modules.workflow.engine;

import com.example.basic.modules.workflow.engine.WfGraph;
import lombok.Data;
import java.util.*;

/**
 * 工作流执行上下文
 *
 * <p>贯穿整个工作流执行周期的状态容器，包含：
 * <ul>
 *   <li>全局变量（variables）</li>
 *   <li>当前执行节点 ID</li>
 *   <li>图结构引用（用于查找下一个节点）</li>
 *   <li>审批任务回调（approvalTaskId → taskId）</li>
 *   <li>循环上下文（loopType / loopCount）</li>
 * </ul>
 */
@Data
public class ExecutionContext {

    @FunctionalInterface
    public interface ApprovalTaskCallback {
        Long create(String nodeId, String nodeName, Integer assigneeType, String assigneeExpr,
                    String title, String content);
    }

    private WfGraph graph;
    private Long instanceId;
    private Long initiatorId;

    /** 全局变量表 */
    private Map<String, Object> variables = new HashMap<>();

    /** 当前节点 ID */
    private String currentNodeId;

    /** 最终输出（end 节点设置）*/
    private Map<String, Object> finalOutput;

    /** 审批任务映射：nodeId → taskId（用于回调时路由）*/
    private Map<String, Long> approvalTaskMap = new HashMap<>();

    /** 循环上下文：nodeId → LoopContext */
    private Map<String, LoopContext> loopContexts = new HashMap<>();

    /** 审批任务 ID → nodeId（反向索引）*/
    private Map<Long, String> taskNodeMap = new HashMap<>();

    /** 审批任务创建回调 */
    private ApprovalTaskCallback approvalTaskCallback;

    // ==================== 审批任务管理 ====================

    /**
     * 创建审批任务，返回数据库 taskId
     * 由 WorkflowEngine 注入实现
     */
    public Long createApprovalTask(String nodeId, String nodeName,
            Integer assigneeType, String assigneeExpr,
            String title, String content) {
        if (approvalTaskCallback == null) {
            throw new UnsupportedOperationException("审批任务回调尚未注入");
        }
        Long taskId = approvalTaskCallback.create(nodeId, nodeName, assigneeType, assigneeExpr, title, content);
        if (taskId != null) {
            approvalTaskMap.put(nodeId, taskId);
            taskNodeMap.put(taskId, nodeId);
        }
        return taskId;
    }

    public String findNodeIdByTaskId(Long taskId) {
        return taskNodeMap.get(taskId);
    }

    // ==================== 节点路由 ====================

    /**
     * 根据当前节点 ID，找到图中有向边指向的下一个节点 ID
     */
    public String findNextNodeId(String currentNodeId) {
        if (graph == null || graph.getEdges() == null) return null;
        return graph.getEdges().stream()
            .filter(e -> currentNodeId.equals(e.getSource()))
            .findFirst()
            .map(WfGraph.Edge::getTarget)
            .orElse(null);
    }

    /**
     * 根据出边找到目标节点（带条件）
     */
    public String findNextNodeIdByCondition(String currentNodeId, String conditionExpr) {
        if (graph == null || graph.getEdges() == null) return null;
        return graph.getEdges().stream()
            .filter(e -> currentNodeId.equals(e.getSource()))
            .filter(e -> conditionExpr == null || conditionExpr.equals(e.getData() != null ? e.getData().getLabel() : null))
            .findFirst()
            .map(WfGraph.Edge::getTarget)
            .orElse(findNextNodeId(currentNodeId));
    }

    // ==================== 变量 ====================

    public void setVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    public Object getVariable(String name) {
        return this.variables.get(name);
    }

    public Map<String, Object> getAllVariables() {
        return new HashMap<>(this.variables);
    }

    // ==================== 循环上下文 ====================

    public void setLoopContext(String nodeId, String loopType, int maxTimes) {
        this.loopContexts.put(nodeId, new LoopContext(loopType, maxTimes));
    }

    public LoopContext getLoopContext(String nodeId) {
        return this.loopContexts.get(nodeId);
    }

    public void incrLoopCount(String nodeId) {
        LoopContext lc = this.loopContexts.get(nodeId);
        if (lc != null) lc.currentCount++;
    }

    @Data
    public static class LoopContext {
        private String loopType;   // for / while / forEach
        private int maxTimes;
        private int currentCount = 0;

        public LoopContext(String loopType, int maxTimes) {
            this.loopType = loopType;
            this.maxTimes = maxTimes;
        }
    }
}
