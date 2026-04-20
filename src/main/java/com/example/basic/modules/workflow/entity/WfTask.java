package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("wf_task")
public class WfTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long definitionId;
    private String definitionCode;
    private String nodeId;
    private String nodeName;
    private Integer assigneeType;
    private String assigneeExpr;
    private Long assigneeId;
    private String assigneeName;
    private String title;
    private String content;
    /** 0=待审批 1=已通过 2=已转交 3=已驳回 */
    private Integer status;
    private String opinion;
    private String action;
    private Long operatorId;
    private String operatorName;
    private Date operatedAt;
    private Date createdTime;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_TRANSFERRED = 2;
    public static final int STATUS_REJECTED = 3;

    public static final String ACTION_APPROVE = "approve";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_TRANSFER = "transfer";
}
