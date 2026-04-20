package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_task_history")
public class WfTaskHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原任务ID */
    private Long taskId;

    /** 工作流实例ID */
    private Long instanceId;

    /** 节点ID */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作类型：submit=提交/agree=同意/reject=拒绝/transfer=转交/revoke=撤回 */
    private String action;

    /** 审批意见 */
    private String opinion;

    /** 操作时间 */
    private Date operatedAt;

    // ===== 枚举值 =====
    public static final String ACTION_SUBMIT = "submit";
    public static final String ACTION_AGREE = "agree";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_TRANSFER = "transfer";
    public static final String ACTION_REVOKE = "revoke";
}
