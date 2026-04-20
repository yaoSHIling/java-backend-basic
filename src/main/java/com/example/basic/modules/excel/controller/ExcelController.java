package com.example.basic.modules.excel.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.modules.excel.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Excel 导入/导出接口。
 *
 * @author hermes-agent
 */
@Tag(name = "07. Excel导入导出")
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @Operation(summary = "导出用户列表 Excel")
    @GetMapping("/export/users")
    @Login
    public void exportUsers(
            @Parameter(description = "文件名") @RequestParam(defaultValue = "用户列表") String fileName,
            jakarta.servlet.http.HttpServletResponse response) {
        // TODO: 实际从 userService.list() 获取数据
        excelService.export(List.of(), response, fileName, "用户列表");
    }

    @Operation(summary = "导入用户 Excel")
    @PostMapping("/import/users")
    @Login
    @LogOperation("导入用户Excel")
    public Result<Integer> importUsers(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail(52004, "文件不能为空");
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xls") && !name.endsWith(".xlsx"))) {
            return Result.fail(52005, "仅支持 .xls / .xlsx");
        }
        try {
            // TODO: excelService.importExcel(User.class, file.getInputStream(), 0);
            return Result.success(0, "导入成功");
        } catch (Exception e) {
            return Result.fail(52004, "导入失败: " + e.getMessage());
        }
    }
}
