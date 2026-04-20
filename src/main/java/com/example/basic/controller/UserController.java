package com.example.basic.controller;

import com.example.basic.model.ApiResponse;
import com.example.basic.model.User;
import com.example.basic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody User user) {
        userService.add(user);
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return ApiResponse.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("删除成功");
    }
}
