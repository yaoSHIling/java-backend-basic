package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("wf_definition")
public class WfDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer version;
    /** 0=草稿 1=已发布 2=禁用 */
    private Integer status;
    /** 画布数据：{ nodes: [...], edges: [...] } */
    private String graphData;
    /** 全局变量定义 */
    private String variables;
    private Long createdBy;
    private Date createdTime;
    private Date updatedTime;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_DISABLED = 2;
}
