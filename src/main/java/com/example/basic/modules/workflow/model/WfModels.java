package com.example.basic.modules.workflow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 工作流设计器配置 DTO。
 *
 * <p>前端传来的完整流程配置结构：
 * <pre>
 * {
 *   "nodes": [
 *     { "id": "node_start", "type": "start", "data": { "name": "发起人" } },
 *     { "id": "node_1", "type": "approver", "data": { "name": "部门主管审批", "assigneeType": 2, "assigneeExpr": "role:manager", "sequence": 1 } },
 *     { "id": "node_cond", "type": "condition", "data": { "name": "条件判断", "conditions": [{ "expr": "amount > 1000", "targetNodeId": "node_2" }], "defaultNodeId": "node_3" } },
 *     { "id": "node_2", "type": "approver", "data": { "name": "总监审批", "assigneeType": 1, "assigneeExpr": "user:5", "sequence": 2 } },
 *     { "id": "node_3", "type": "end", "data": { "name": "结束" } }
 *   ],
 *   "edges": [
 *     { "source": "node_start", "target": "node_1" },
 *     { "source": "node_1", "target": "node_cond" },
 *     { "source": "node_cond", "target": "node_2", "conditionExpr": "amount > 1000" },
 *     { "source": "node_cond", "target": "node_3" },
 *     { "source": "node_2", "target": "node_3" }
 *   ]
 * }
 * </pre>
 */
@Data
@Schema(name = "工作流定义配置")
public class WfDefinitionConfig {

    @Schema(description = "节点列表")
    private List<Node> nodes;

    @Schema(description = "连线列表")
    private List<Edge> edges;

    @Data
    @Schema(name = "节点")
    public static class Node {
        @Schema(description = "节点ID（唯一）")
        private String id;

        @Schema(description = "节点类型：start=发起/approver=审批/condition=条件/end=结束/auto=自动")
        private String type;

        @Schema(description = "节点配置")
        private NodeData data;

        /** X坐标（设计器用） */
        private Double x;
        /** Y坐标（设计器用） */
        private Double y;
    }

    @Data
    @Schema(name = "节点数据")
    public static class NodeData {
        @Schema(description = "节点显示名称")
        private String name;

        // === 审批节点专用 ===
        /** 审批人类型：1=指定人 2=角色 3=发起人自选 */
        private Integer assigneeType;
        /** 审批人表达式：user:123 / role:manager / ${initiator} */
        private String assigneeExpr;
        /** 审批顺序（同一 sequence 并行审批） */
        private Integer sequence;

        // === 条件节点专用 ===
        /** 条件列表（满足则流向对应 targetNodeId） */
        private List<Condition> conditions;
        /** 默认目标节点（条件都不满足时） */
        private String defaultNodeId;
    }

    @Data
    @Schema(name = "条件")
    public static class Condition {
        /** 条件表达式，支持 SPEL 简化语法：amount > 1000 */
        private String expr;
        /** 满足条件时流向的节点ID */
        private String targetNodeId;
        /** 条件描述（前端展示用） */
        private String label;
    }

    @Data
    @Schema(name = "连线")
    public static class Edge {
        @Schema(description = "源节点ID")
        private String source;
        @Schema(description = "目标节点ID")
        private String target;
        /** 条件表达式（条件节点后的边才需要） */
        private String conditionExpr;
        /** 连线标签（前端展示） */
        private String label;
    }
}

// ==================== 请求 DTO ====================

@Data
@Schema(name = "创建工作流定义")
class SaveDefinitionDTO {
    private String name;
    private String code;
    private String description;
    private String formCode;
    private WfDefinitionConfig config;
}

@Data
@Schema(name = "发布工作流")
class PublishDefinitionDTO {
    private Long id;
}

@Data
@Schema(name = "提交工作流申请")
class SubmitInstanceDTO {
    /** 工作流编码（必填） */
    private String definitionCode;
    /** 业务ID（可选，如订单ID） */
    private String businessId;
    /** 业务类型（可选） */
    private String businessType;
    /** 流程标题 */
    private String title;
    /** 表单数据（JSON） */
    private Map<String, Object> formData;
}

@Data
@Schema(name = "审批操作")
class ApproveDTO {
    /** 任务ID */
    private Long taskId;
    /** 操作：agree=同意 / reject=拒绝 / transfer=转交 */
    private String action;
    /** 审批意见 */
    private String opinion;
    /** 转交目标用户ID（action=transfer时必填） */
    private Long transferTo;
}

@Data
@Schema(name = "撤回申请")
class RevokeDTO {
    private Long instanceId;
    private String opinion;
}
