package com.example.basic.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.basic.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_definition")
public class WfDefinition extends BaseEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /** 工作流名称 */
    private String name;

    /** 工作流编码（唯一） */
    private String code;

    /** 描述 */
    private String description;

    /** 版本号 */
    private Integer version;

    /** 状态：0=草稿 1=已发布 2=已禁用 */
    private Integer status;

    /** 关联表单编码 */
    private String formCode;

    /** 完整流程配置（JSON：nodes + edges） */
    private String config;

    /** 创建人ID */
    private Long createdBy;

    // ===== 枚举值 =====
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_DISABLED = 2;
}
