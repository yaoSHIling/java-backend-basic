package com.example.basic.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 全局配置。
 *
 * <p>包含：
 * <ul>
 *   <li>分页插件（PaginationInnerInterceptor）</li>
 *   <li>自动填充处理器（createTime / updateTime）</li>
 * </ul>
 *
 * @author hermes-agent
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件。
     * 注册后，Mapper 方法传入 Page 对象即可自动分页。
     * 无需手写 LIMIT #{offset}, #{size}。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // MySQL 分页（自动处理 offset = (pageNum-1) * pageSize）
        interceptor.addInnerInterceptor(
                new PaginationInnerInterceptor(DbType.MYSQL)
        );
        return interceptor;
    }

    /**
     * 自动填充处理器。
     *
     * <p>当实体类字段标注了：
     * <ul>
     *   <li>@TableField(fill = FieldFill.INSERT)        → 插入时自动填充</li>
     *   <li>@TableField(fill = FieldFill.INSERT_UPDATE) → 插入/更新时自动填充</li>
     * </ul>
     *
     * <p>这样就不用在每次 insert/update 时手动 setCreateTime/setUpdateTime。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时填充创建时间和更新时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时只填充更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
