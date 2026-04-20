package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_task")
public class WfTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工作流实例ID */
    private Long instanceId;

    /** 节点ID */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 审批人ID */
    private Long assigneeId;

    /** 审批人姓名 */
    private String assigneeName;

    /** 审批人类型：1=指定人 2=角色 3=发起人自选 */
    private Integer assigneeType;

    /** 审批人表达式 */
    private String assigneeExpr;

    /** 状态：0=待审批 1=已审批 2=已转交 3=已驳回 */
    private Integer status;

    /** 审批意见 */
    private String opinion;

    /** 操作：agree=同意 / reject=拒绝 / transfer=转交 */
    private String action;

    /** 审批时间 */
    private Date operatedAt;

    /** 实际操作人 */
    private Long operatorId;

    /** 审批顺序 */
    private Integer sequence;

    /** 创建时间 */
    private Date createdTime;

    // ===== 枚举值 =====
    public static final int STATUS_PENDING = 0;     // 待审批
    public static final int STATUS_APPROVED = 1;    // 已审批
    public static final int STATUS_TRANSFERRED = 2; // 已转交
    public static final int STATUS_REJECTED = 3;   // 已驳回

    public static final int ASSIGNEE_TYPE_USER = 1;    // 指定人
    public static final int ASSIGNEE_TYPE_ROLE = 2;   // 角色
    public static final int ASSIGNEE_TYPE_OPTIONAL = 3; // 发起人自选

    public static final String ACTION_AGREE = "agree";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_TRANSFER = "transfer";
}
