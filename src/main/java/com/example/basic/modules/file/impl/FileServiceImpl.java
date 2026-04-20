package com.example.basic.modules.file.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.file.dao.FileRecordDao;
import com.example.basic.modules.file.entity.FileRecord;
import com.example.basic.modules.file.service.FileService;
import com.example.basic.model.query.PageParams;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * 文件服务实现。
 *
 * <p>采用本地存储策略：文件存在服务器本地目录，数据库只存元数据。
 * 访问路径：/files/{storedName}
 *
 * <p>生产环境建议改用 OSS（阿里云OSS / 腾讯COS / 华为OBS），
 * 代码结构无需大改，只需改 FileServiceImpl 中的存储逻辑。
 *
 * @author hermes-agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRecordDao fileRecordDao;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 上传文件。
     * 1. 校验文件类型和大小
     * 2. 生成唯一文件名（UUID + 时间戳）
     * 3. 保存文件到本地目录
     * 4. 写入数据库记录
     */
    @Override
    @Transactional
    public FileRecord upload(MultipartFile file, Long userId, String username) {
        // ========== 1. 校验文件 ==========
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new BizException(ResultCode.FILE_SIZE_EXCEEDED);
        }

        // ========== 2. 生成存储文件名 ==========
        String ext = FileUtil.extName(originalName);  // 获取扩展名
        String storedName = IdUtil.fastSimpleUUID() + "." + ext;
        // 按日期分目录存储：uploads/2024/04/xxx.jpg
        String dateDir = DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd");
        String relativePath = "files/" + dateDir + "/" + storedName;
        String absolutePath = uploadDir + "/" + relativePath;

        // ========== 3. 创建目录并写入文件 ==========
        try {
            Path dir = Paths.get(uploadDir, "files", dateDir);
            Files.createDirectories(dir);
            file.transferTo(Paths.get(absolutePath));
        } catch (IOException e) {
            log.error("文件写入失败 | path={}", absolutePath, e);
            throw new BizException(ResultCode.FILE_UPLOAD_ERROR);
        }

        // ========== 4. 写入数据库 ==========
        FileRecord record = FileRecord.builder()
                .originalName(originalName)
                .storedName(storedName)
                .filePath(relativePath)
                .fileUrl("/files/" + dateDir + "/" + storedName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .extension(ext)
                .uploaderId(userId)
                .uploaderName(username)
                .downloadCount(0)
                .createTime(LocalDateTime.now())
                .build();

        fileRecordDao.insert(record);
        log.info("文件上传成功 | id={} | name={} | size={}",
                record.getId(), originalName, record.getFileSize());

        return record;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileRecord record = fileRecordDao.selectById(id);
        if (record == null) {
            throw new BizException(ResultCode.RECORD_NOT_FOUND);
        }
        // 物理删除文件
        File file = new File(uploadDir + "/" + record.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        fileRecordDao.deleteById(id);
        log.info("文件删除 | id={} | userId={}", id, userId);
    }

    @Override
    public IPage<FileRecord> page(PageParams pageParams, String originalName) {
        Page<FileRecord> page = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(originalName)) {
            wrapper.like(FileRecord::getOriginalName, originalName);
        }
        wrapper.orderByDesc(FileRecord::getCreateTime);
        return fileRecordDao.selectPage(page, wrapper);
    }
}
