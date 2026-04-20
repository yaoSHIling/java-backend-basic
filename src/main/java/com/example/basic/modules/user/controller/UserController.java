package com.example.basic.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.user.entity.User;
import com.example.basic.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口。
 *
 * <p>CRUD + 分页 + 状态管理。
 *
 * @author hermes-agent
 */
@Tag(name = "02. 用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表。
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param username 用户名（模糊搜索，可空）
     * @param status   状态筛选（1=启用，0=禁用，可空）
     */
    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    @Login
    public Result<IPage<User>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "用户名（模糊）") @RequestParam(required = false) String username,
            @Parameter(description = "状态：1=启用，0=禁用") @RequestParam(required = false) Integer status
    ) {
        PageParams pageParams = new PageParams(pageNum, pageSize);
        return Result.success(userService.page(pageParams, username, status));
    }

    /** 查询单个用户详情 */
    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @Login
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /** 新增用户 */
    @Operation(summary = "新增用户")
    @PostMapping
    @Login
    @LogOperation("新增用户")
    public Result<String> save(@RequestBody User user) {
        userService.save(user);
        return Result.success("添加成功");
    }

    /** 更新用户 */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @Login
    @LogOperation("更新用户")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return Result.success("更新成功");
    }

    /** 删除用户（逻辑删除） */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除用户")
    public Result<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功");
    }

    /** 获取所有用户（下拉选项等场景） */
    @Operation(summary = "用户下拉列表")
    @GetMapping("/list")
    @Login
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /** 启用/禁用用户 */
    @Operation(summary = "启用/禁用用户")
    @PatchMapping("/{id}/status")
    @Login
    @LogOperation("变更用户状态")
    public Result<String> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        userService.updateStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    /** 修改当前用户密码 */
    @Operation(summary = "修改密码")
    @PatchMapping("/{id}/password")
    @Login
    @LogOperation("修改密码")
    public Result<String> updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword
    ) {
        userService.updatePassword(id, newPassword);
        return Result.success("密码修改成功");
    }

    /** 管理员重置用户密码 */
    @Operation(summary = "重置用户密码")
    @PatchMapping("/{id}/reset-password")
    @Login
    @LogOperation("重置用户密码")
    public Result<String> resetPassword(
            @PathVariable Long id,
            @RequestParam(required = false) String newPassword
    ) {
        userService.resetPassword(id, newPassword);
        return Result.success("密码已重置为：" + (newPassword != null ? newPassword : "123456"));
    }
}
