package com.example.basic.modules.workflow.engine;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 工作流图数据（与前端 Vue Flow 导出的结构一致）
 */
@Data
public class WfGraph {
    private List<Node> nodes;
    private List<Edge> edges;

    @Data
    public static class Node {
        private String id;           // 节点唯一ID
        private String type;        // 节点类型：start/end/llm/code/condition/approval/http/variable/loop/subflow/message
        private Position position;  // 画布坐标
        private NodeData data;      // 节点业务配置
    }

    @Data
    public static class Position {
        private double x;
        private double y;
    }

    @Data
    public static class NodeData {
        private String name;         // 节点显示名称

        // === 通用 ===
        private Map<String, Object> input;   // 输入映射
        private Map<String, Object> output;  // 输出映射

        // === LLM 节点 ===
        private String model;               // 模型名称
        private String prompt;              // 提示词模板
        private String systemPrompt;         // 系统提示词
        private List<Map<String, String>> messages; // 消息列表

        // === Code 节点 ===
        private String language;            // javascript / python
        private String code;                // 执行代码

        // === Condition 节点 ===
        private List<Branch> branches;     // 条件分支
        private Branch defaultBranch;       // 默认分支

        // === Approval 节点 ===
        private Integer assigneeType;        // 1=指定人 2=角色 3=发起人自选
        private String assigneeExpr;        // user:123 / role:manager / ${initiator}
        private String assigneeIds;         // 逗号分隔的ID列表
        private String titleTemplate;       // 审批标题模板
        private String contentTemplate;     // 审批内容模板

        // === HTTP 节点 ===
        private String url;
        private String method;              // GET/POST/PUT/DELETE
        private Map<String, String> headers;
        private Map<String, Object> body;
        private Integer timeout;            // 超时ms

        // === Variable 节点 ===
        private String variableName;        // 变量名
        private String variableType;        // String/Number/Boolean/Object/Array
        private Object variableValue;       // 变量值（设置变量时）
        private Boolean isList;             // 是否数组

        // === Loop 节点 ===
        private String loopType;            // for / while / forEach
        private String loopVariable;        // 循环变量名
        private Integer loopTimes;          // 循环次数
        private String loopArray;           // 数组表达式
        private Integer maxLoopTimes;       // 最大循环次数

        // === Subflow 节点 ===
        private String subflowCode;         // 子流程编码
        private Map<String, Object> subflowInput; // 子流程输入参数

        // === Message 节点 ===
        private String channel;             // weixin / email / dingtalk / webhook
        private String toUser;
        private String title;
        private String content;
        private Map<String, Object> extra;  // 额外参数

        // === Database 节点 ===
        private String sql;                 // SQL 语句
        private String datasource;          // 数据源名称
        private Boolean isSelect;           // 是否查询
    }

    @Data
    public static class Branch {
        private String id;
        private String name;
        private String conditionExpr;       // 条件表达式，如：amount > 1000
        private String targetNodeId;         // 满足条件时流向的节点ID
    }

    @Data
    public static class Edge {
        private String id;
        private String source;      // 源节点ID
        private String target;      // 目标节点ID
        private EdgeData data;
    }

    @Data
    public static class EdgeData {
        private String label;      // 连线标签
    }
}
