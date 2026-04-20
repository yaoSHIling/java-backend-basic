package com.example.basic.modules.config.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.modules.config.entity.SysConfig;
    import com.example.basic.modules.config.service.SysConfigService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;

/**
 * 系统配置管理接口。
 *
 * @author hermes-agent
 */
@Tag(name = "05. 系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /** 获取所有配置 */
    @Operation(summary = "获取所有配置（Map形式）")
    @GetMapping("/all")
    @Login
    public Result<Map<String, String>> getAll() {
        return Result.success(sysConfigService.getAllConfig());
    }

    /** 根据 key 获取单个配置值 */
    @Operation(summary = "根据 key 获取配置")
    @GetMapping("/{key}")
    @Login
    public Result<SysConfig> getByKey(@PathVariable String key) {
        return Result.success(sysConfigService.getByKey(key));
    }

    /** 根据分组获取配置列表 */
    @Operation(summary = "根据分组获取配置")
    @GetMapping("/group/{groupName}")
    @Login
    public Result<List<SysConfig>> getByGroup(@PathVariable String groupName) {
        return Result.success(sysConfigService.getByGroup(groupName));
    }

    /** 保存或更新配置 */
    @Operation(summary = "保存/更新配置")
    @PostMapping
    @Login
    @LogOperation("修改系统配置")
    public Result<String> saveOrUpdate(@RequestBody SysConfig config) {
        sysConfigService.saveOrUpdate(config);
        return Result.success("保存成功");
    }

    /** 删除配置 */
    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除系统配置")
    public Result<String> delete(@PathVariable Long id) {
        sysConfigService.delete(id);
        return Result.success("删除成功");
    }
}
