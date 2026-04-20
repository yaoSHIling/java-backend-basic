package com.example.basic.modules.dict.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.modules.dict.entity.DictData;
import com.example.basic.modules.dict.service.DictService;
import com.example.basic.model.query.PageParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典接口。
 *
 * <p>提供字典项的增删改查，以及根据类型批量获取选项列表。
 *
 * @author hermes-agent
 */
@Tag(name = "04. 数据字典")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 根据字典类型获取选项列表（前端下拉专用）。
     * 一次可查询多个类型：GET /dict/options?types=gender,user_status
     */
    @Operation(summary = "获取字典选项（前端下拉用）")
    @GetMapping("/options")
    @Login
    public Result<Map<String, List<DictData>>> getOptions(
            @Parameter(description = "字典类型，多个用逗号分隔") @RequestParam String types
    ) {
        Map<String, List<DictData>> result = java.util.Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> dictService.getByType(type)
                ));
        return Result.success(result);
    }

    /** 分页查询字典数据（后台管理用） */
    @Operation(summary = "字典分页列表（后台）")
    @GetMapping("/page")
    @Login
    public Result<IPage<DictData>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(dictService.page(new PageParams(pageNum, pageSize), dictType, dictLabel, status));
    }

    /** 新增字典项 */
    @Operation(summary = "新增字典")
    @PostMapping
    @Login
    @LogOperation("新增字典")
    public Result<String> save(@RequestBody DictData dictData) {
        dictService.save(dictData);
        return Result.success("添加成功");
    }

    /** 更新字典项 */
    @Operation(summary = "更新字典")
    @PutMapping
    @Login
    @LogOperation("更新字典")
    public Result<String> update(@RequestBody DictData dictData) {
        dictService.update(dictData);
        return Result.success("更新成功");
    }

    /** 删除字典项 */
    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除字典")
    public Result<String> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.success("删除成功");
    }
}
