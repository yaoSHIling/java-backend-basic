package com.example.basic.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基类实体。
 *
 * <p>所有业务实体类应继承此类，自动获得：
 * <ul>
 *   <li>{@code id} — 主键（自增/BIGINT）</li>
 *   <li>{@code createTime} — 创建时间（插入时自动填充）</li>
 *   <li>{@code updateTime} — 更新时间（插入/更新时自动填充）</li>
 *   <li>{@code deleted} — 逻辑删除标记（0=未删除，1=已删除，查询自动过滤）</li>
 * </ul>
 *
 * <p>配合 MyBatis-Plus 的 {@link com.baomidou.mybatisplus.core.handlers.MetaObjectHandler}
 * 实现自动填充，无需在 SQL 或代码中手动维护时间字段。
 *
 * @author hermes-agent
 * @see com.example.basic.common.config.MybatisPlusConfig 自动填充配置
 */
@Data
@Schema(name = "基础实体")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /** 创建时间（插入时自动填充，配置见 MybatisPlusConfig） */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间（插入/更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 逻辑删除标记（MyBatis-Plus 全局逻辑删除配置，勿手动修改） */
    @TableLogic
    @Schema(description = "逻辑删除：0=未删除，1=已删除")
    private Integer deleted;
}
