package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_instance")
public class WfInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long definitionId;
    private String definitionCode;
    private String graphData;
    /** 0=运行中 1=成功 2=失败 3=暂停 */
    private Integer status;
    private String inputData;
    private String outputData;
    private String errorMsg;
    private String currentNodeId;
    private Long initiatorId;
    private Date startedAt;
    private Date finishedAt;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_PAUSED = 3;
}
