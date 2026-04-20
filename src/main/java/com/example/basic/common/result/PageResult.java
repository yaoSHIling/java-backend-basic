package com.example.basic.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>统一分页响应格式，与前端 Vben Admin 等主流前端框架兼容。
 *
 * <p>响应示例：
 * <pre>
 * {
 *   "code": 0,
 *   "msg": "success",
 *   "data": {
 *     "list": [ ... ],   // 当前页数据
 *     "total": 100,      // 总记录数
 *     "page": 1,         // 当前页码
 *     "pageSize": 10     // 每页条数
 *   }
 * }
 * </pre>
 *
 * @param <T> 列表元素类型
 * @author hermes-agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "分页结果")
public class PageResult<T> {

    /** 当前页数据列表 */
    @Schema(description = "当前页数据列表")
    private List<T> list;

    /** 总记录数 */
    @Schema(description = "总记录数")
    private Long total;

    /** 当前页码（从 1 开始） */
    @Schema(description = "当前页码")
    private Long page;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Long pageSize;

    /** 总页数（计算属性） */
    @Schema(description = "总页数")
    private Long totalPages;

    /**
     * 从 MyBatis-Plus IPage 构建分页结果。
     *
     * <pre>
     * IPage<User> page = userDao.selectPage(query.toPage(), wrapper);
     * return PageResult.of(page);
     * </pre>
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        long totalPages = (page.getTotal() + page.getSize() - 1) / page.getSize();
        return PageResult.<T>builder()
                .list(page.getRecords())
                .total(page.getTotal())
                .page((long) page.getCurrent())
                .pageSize((long) page.getSize())
                .totalPages(totalPages)
                .build();
    }

    /**
     * 快速构建分页结果（从 IPage，忽略泛型警告）。
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<?> page) {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) page.getRecords();
        long totalPages = (page.getTotal() + page.getSize() - 1) / page.getSize();
        return PageResult.<T>builder()
                .list(list)
                .total(page.getTotal())
                .page((long) page.getCurrent())
                .pageSize((long) page.getSize())
                .totalPages(totalPages)
                .build();
    }
}
