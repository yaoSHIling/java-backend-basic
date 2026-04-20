package com.example.basic.model.query;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求参数。
 *
 * <p>封装 pageNum + pageSize，接受前端分页请求。
 * 配合 MyBatis-Plus 分页插件使用：
 * <pre>
 * Page<User> page = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
 * userDao.selectPage(page, wrapper);
 * </pre>
 *
 * @author hermes-agent
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParams {

    /** 页码（从1开始，默认第1页） */
    @Parameter(description = "页码（从1开始）")
    private Long pageNum = 1L;

    /** 每页条数（默认10条，最大100条） */
    @Parameter(description = "每页条数")
    private Long pageSize = 10L;

    public PageParams(Long pageNum) {
        this.pageNum = pageNum;
    }
}
