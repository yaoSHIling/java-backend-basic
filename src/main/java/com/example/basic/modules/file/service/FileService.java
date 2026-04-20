package com.example.basic.modules.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /** 上传文件 */
    FileRecord upload(MultipartFile file, Long userId, String username);

    /** 删除文件 */
    void delete(Long id, Long userId);

    /** 分页查询文件列表 */
    IPage<FileRecord> page(PageParams pageParams, String originalName);
}
