package com.example.basic.modules.workflow.engine;

import lombok.Data;
import java.util.Map;

/**
 * 节点执行结果
 */
@Data
public class WfNodeResult {
    /** 节点ID */
    private String nodeId;
    /** 执行状态：success / failure / waiting / paused */
    private String status;
    /** 输出数据 */
    private Map<String, Object> outputData;
    /** 错误信息 */
    private String errorMsg;
    /** 下一步要执行的节点ID列表（用于分支节点）*/
    private String nextNodeId;
    /** 执行耗时ms */
    private long elapsedMs;

    public static WfNodeResult success(String nodeId, Map<String, Object> output, String nextNodeId, long elapsedMs) {
        WfNodeResult r = new WfNodeResult();
        r.setNodeId(nodeId);
        r.setStatus("success");
        r.setOutputData(output);
        r.setNextNodeId(nextNodeId);
        r.setElapsedMs(elapsedMs);
        return r;
    }

    public static WfNodeResult failure(String nodeId, String errorMsg, long elapsedMs) {
        WfNodeResult r = new WfNodeResult();
        r.setNodeId(nodeId);
        r.setStatus("failure");
        r.setErrorMsg(errorMsg);
        r.setElapsedMs(elapsedMs);
        return r;
    }

    public static WfNodeResult waiting(String nodeId, Map<String, Object> output) {
        WfNodeResult r = new WfNodeResult();
        r.setNodeId(nodeId);
        r.setStatus("waiting");
        r.setOutputData(output);
        return r;
    }
}
