package com.example.basic.modules.config.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.basic.model.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
    import lombok.*;

    /**
     * 系统参数配置实体。
     *
     * <p>用于存储系统中可运行时修改的配置项（键值对）。
     * 类似于"功能开关"或"系统设置"，无需改代码重启服务。
     *
     * <p>示例数据：
     * <pre>
     * config_key          | config_value            | remark
     * --------------------|-------------------------|-------------------
     * sys.logo.url        | /assets/logo.png        | 系统Logo
     * sys.copyright.text  | ©2024 公司名称           | 版权信息
     * upload.max.size     | 10485760                | 上传文件最大10MB
     * </pre>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_config")
    @Schema(name = "系统配置")
    public class SysConfig extends BaseEntity {

        /** 配置键（唯一，英文+下划线） */
        private String configKey;

        /** 配置值 */
        private String configValue;

        /** 配置名称（中文说明） */
        private String configName;

        /** 配置分组 */
        private String groupName;

        /** 配置类型：string / number / boolean / json */
        private String configType;

        /** 是否系统内置（系统内置不可删除） */
        private Integer readonly;

        /** 状态：1=启用，0=禁用 */
        private Integer status;

        /** 备注 */
        private String remark;
    }
