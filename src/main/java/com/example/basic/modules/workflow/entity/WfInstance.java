package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_instance")
public class WfInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工作流定义ID */
    private Long definitionId;

    /** 工作流编码 */
    private String definitionCode;

    /** 关联业务ID */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 流程标题 */
    private String title;

    /** 状态：0=审批中 1=已通过 2=已拒绝 3=已撤回 */
    private Integer status;

    /** 提交的表单数据（JSON） */
    private String formData;

    /** 当前节点ID */
    private String currentNodeId;

    /** 发起人ID */
    private Long initiatorId;

    /** 发起时间 */
    private Date startedAt;

    /** 结束时间 */
    private Date finishedAt;

    // ===== 枚举值 =====
    public static final int STATUS_RUNNING = 0;    // 审批中
    public static final int STATUS_APPROVED = 1;   // 已通过
    public static final int STATUS_REJECTED = 2;  // 已拒绝
    public static final int STATUS_REVOKED = 3;    // 已撤回
}
