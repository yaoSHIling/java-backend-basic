package com.example.basic.modules.file.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.modules.file.entity.FileRecord;
import com.example.basic.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;

/**
 * 文件上传/下载接口。
 *
 * <p>上传后返回文件访问路径，前端可直接拼接域名访问。
 *
 * @author hermes-agent
 */
@Tag(name = "03. 文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 上传文件 */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    @Login
    @LogOperation("上传文件")
    public Result<FileRecord> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @RequestAttribute(name = "userId") Long userId,
            @RequestAttribute(name = "username") String username
    ) {
        return Result.success(fileService.upload(file, userId, username));
    }

    /** 删除文件 */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除文件")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestAttribute(name = "userId") Long userId
    ) {
        fileService.delete(id, userId);
        return Result.success("删除成功");
    }

    /** 文件分页列表 */
    @Operation(summary = "文件列表")
    @GetMapping("/page")
    @Login
    public Result<IPage<FileRecord>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String originalName
    ) {
        return Result.success(
                fileService.page(new com.example.basic.model.query.PageParams(pageNum, pageSize), originalName)
        );
    }

    /** 文件下载（通过文件ID） */
    @Operation(summary = "文件下载")
    @GetMapping("/download/{id}")
    @Login
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        var record = fileService.page(
                new com.example.basic.model.query.PageParams(1, 1), null
        );
        FileRecord fileRecord = record.getRecords().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultCode.RECORD_NOT_FOUND));

        Resource resource = new FileSystemResource(
                Paths.get(uploadDir, fileRecord.getFilePath()).toFile()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="" + fileRecord.getOriginalName() + """)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 引入 BizException（避免单独 import）
    private static class BizException extends RuntimeException {
        BizException(com.example.basic.common.result.ResultCode rc) { super(rc.getMessage()); }
        BizException(com.example.basic.common.result.ResultCode rc, String msg) { super(msg); }
    }
}
