package com.example.basic.modules.workflow.engine;

import java.util.Map;

/**
 * 节点执行器接口（参考 Coze 节点模型）
 *
 * <p>每个节点类型对应一个实现类，统一 execute() 方法入口。
 * 节点从 context 读取输入，执行逻辑后写入 output，
 * 返回 nextNodeId 决定下一跳（null 表示流程结束）。
 */
public interface NodeExecutor {

    /**
     * 执行节点
     *
     * @param node        节点定义
     * @param inputData   来自上游的输入数据
     * @param context     运行时上下文（含全局变量、审批状态等）
     * @return 执行结果（outputData + nextNodeId）
     */
    WfNodeResult execute(WfGraph.Node node, Map<String, Object> inputData, ExecutionContext context);

    /** 节点类型标识 */
    String nodeType();

    // ==================== Coze 节点类型清单 ====================
    // start      : 开始节点（流程入口，自动跳过）
    // end        : 结束节点（返回结果）
    // llm        : 大模型节点（AI 对话/生成）
    // code       : 代码节点（执行自定义脚本）
    // condition  : 条件分支节点（按条件分流）
    // approval   : 审批节点（人工审批）
    // http       : HTTP 请求节点
    // variable   : 变量节点（读取/设置变量）
    // loop       : 循环节点（for/while/forEach）
    // subflow    : 子流程节点（调用其他工作流）
    // message    : 消息节点（发送通知）
    // database   : 数据库节点（执行 SQL）
}
