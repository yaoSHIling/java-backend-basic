package com.example.basic.modules.coze.entity;

import lombok.*;
import java.util.List;

/**
 * Coze 工作流执行结果。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeWorkflowResponse {

    private int code;
    private String msg;

    /** in_progress=进行中，completed=已完成，failed=失败 */
    private String status;

    private String workflowRunId;
    private Long workflowRunStartAt;
    private Long workflowRunEndAt;
    private Long workflowRunDuration;

    /** 工作流最终输出 */
    private Object output;

    /** 节点日志 */
    private List<NodeLog> nodeLogs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeLog {
        private String nodeId;
        private String nodeName;
        private String status;
        private Object output;
    }

    public boolean isCompleted() { return "completed".equalsIgnoreCase(status); }
    public boolean isFailed()     { return "failed".equalsIgnoreCase(status); }

    public String getOutputText() {
        if (output == null) return "";
        return output.toString();
    }
}
