package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_instance_log")
public class WfInstanceLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    /** 0=等待 1=运行中 2=成功 3=失败 */
    private Integer status;
    private String inputData;
    private String outputData;
    private String errorMsg;
    private Date startedAt;
    private Date finishedAt;
    private Integer elapsedMs;

    public static final int STATUS_WAIT = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAILED = 3;
}
