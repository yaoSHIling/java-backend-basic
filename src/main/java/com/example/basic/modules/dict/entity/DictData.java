package com.example.basic.modules.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.basic.model.entity.BaseEntity;
    import io.swagger.v3.oas.annotations.media.Schema;
    import lombok.*;

    /**
     * 数据字典实体。
     *
     * <p>用于存储系统中通用的选项数据，如：性别、状态、类型等。
     * 相比枚举类，字典数据可运行时动态修改，无需重新编译。
     *
     * <p>使用场景：
     * - 前端下拉选项（性别、职业、状态等）
     * - 配置文件（系统开关、限制值等）
     *
     * <p>表结构示例：
     * <pre>
     * dict_type  | dict_label | dict_value | sort | status | remark
     * -----------|------------|-------------|------|--------|-------
     * gender     | 男         | 1           | 1    | 1      |
     * gender     | 女         | 2           | 2    | 1      |
     * user_status| 正常       | 1           | 1    | 1      |
     * user_status| 禁用       | 0           | 2    | 1      |
     * </pre>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @TableName("sys_dict_data")
    @Schema(name = "字典数据")
    public class DictData extends BaseEntity {

        /** 字典类型（同一类选项归为一组，如 gender / user_status） */
        private String dictType;

        /** 字典标签（显示名称，如"男"） */
        private String dictLabel;

        /** 字典值（存储值，如"1"） */
        private String dictValue;

        /** 排序（数字越小越靠前） */
        private Integer sort;

        /** 状态：1=正常，0=禁用 */
        private Integer status;

        /** 备注（可选） */
        private String remark;

        /** 样式（如 coloring: "blue" / "danger"，前端可据此着色） */
        private String cssClass;
    }
