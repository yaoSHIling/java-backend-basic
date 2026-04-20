package com.example.basic.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.basic.model.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户实体类。
 *
 * <p>继承 {@link BaseEntity}，拥有 id / createTime / updateTime / deleted 字段。
 * 使用 MyBatis-Plus 自动维护逻辑删除和自动填充。
 *
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(name = "用户实体")
public class User extends BaseEntity {

    /** 用户名（唯一） */
    @Schema(description = "用户名")
    private String username;

    /** 密码（MD5 加密存储） */
    @Schema(description = "密码（加密存储）")
    private String password;

    /** 昵称（显示名称） */
    @Schema(description = "昵称")
    private String nickname;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 头像 URL */
    @Schema(description = "头像URL")
    private String avatar;

    /** 性别：0=未知，1=男，2=女 */
    @Schema(description = "性别：0=未知，1=男，2=女")
    private Integer gender;

    /** 账号状态：1=启用，0=禁用 */
    @Schema(description = "账号状态：1=启用，0=禁用")
    private Integer status;

    /** 最后登录 IP */
    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
