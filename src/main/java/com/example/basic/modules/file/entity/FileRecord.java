package com.example.basic.modules.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.basic.model.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/** 文件记录实体（存储文件元数据，非文件本身） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_record")
@Schema(name = "文件记录")
public class FileRecord extends BaseEntity {

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名（UUID，避免文件名冲突） */
    private String storedName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件访问URL */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME类型 */
    private String contentType;

    /** 文件扩展名 */
    private String extension;

    /** 上传用户ID */
    private Long uploaderId;

    /** 上传用户昵称 */
    private String uploaderName;

    /** 下载次数 */
    private Integer downloadCount;
}
