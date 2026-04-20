package com.example.basic.modules.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.annotation.Login;
import com.example.basic.common.result.Result;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.log.entity.OperationLog;
import com.example.basic.modules.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志查询接口（只读，供管理员查看）。
 *
 * @author hermes-agent
 */
@Tag(name = "06. 操作日志")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /** 分页查询操作日志 */
    @Operation(summary = "日志分页列表")
    @GetMapping("/page")
    @Login
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String ip
    ) {
        return Result.success(
                operationLogService.page(new PageParams(pageNum, pageSize), username, operation, ip)
        );
    }
}
