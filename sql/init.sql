-- ================================================================
-- Java 后端快速开发脚手架 · 数据库初始化脚本
-- 适用：MySQL 5.7+ / MySQL 8.0+
-- ================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `java_backend_basic`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `java_backend_basic`;

-- ================================================================
-- 1. 用户表 sys_user
-- ================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名（唯一）',
    `password`        VARCHAR(255) NOT NULL COMMENT '密码（MD5加密存储）',
    `nickname`        VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    `email`           VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    `phone`           VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    `avatar`          VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    `gender`          TINYINT DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
    `status`          TINYINT DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `last_login_ip`   VARCHAR(50)  DEFAULT '' COMMENT '最后登录IP',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_status`   (`status`),
    INDEX `idx_deleted`  (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 默认管理员：admin / 123456（MD5: e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1);

INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
    ('test',   'e10adc3949ba59abbe56e057f20f883e', '测试用户',   'test@example.com',   1),
    ('editor', 'e10adc3949ba59abbe56e057f20f883e', '编辑人员',   'editor@example.com',  1);

-- ================================================================
-- 2. 操作日志表 sys_operation_log
-- ================================================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id`              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id`         BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
    `username`        VARCHAR(50)  DEFAULT '' COMMENT '操作用户名',
    `operation`       VARCHAR(100) DEFAULT '' COMMENT '操作描述',
    `operation_type`  VARCHAR(50)  DEFAULT '' COMMENT '操作类型',
    `request_method`  VARCHAR(10)  DEFAULT '' COMMENT 'HTTP方法',
    `request_url`     VARCHAR(500) DEFAULT '' COMMENT '请求URL',
    `request_params`  TEXT COMMENT '请求参数',
    `ip`              VARCHAR(50)  DEFAULT '' COMMENT '操作人IP',
    `success`         TINYINT DEFAULT 1 COMMENT '是否成功：1=成功，0=失败',
    `error_msg`       VARCHAR(500) DEFAULT '' COMMENT '错误信息',
    `duration_ms`     INT DEFAULT 0 COMMENT '执行耗时（毫秒）',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user_id`   (`user_id`),
    INDEX `idx_username`  (`username`),
    INDEX `idx_create_time`(`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ================================================================
-- 3. 数据字典表 sys_dict_data
-- ================================================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id`          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型（分组标识）',
    `dict_label`  VARCHAR(100) NOT NULL COMMENT '字典标签（显示名称）',
    `dict_value`  VARCHAR(100) NOT NULL COMMENT '字典值（存储值）',
    `sort`        INT DEFAULT 0 COMMENT '排序（越小越靠前）',
    `status`      TINYINT DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    `remark`      VARCHAR(255) DEFAULT '' COMMENT '备注',
    `css_class`   VARCHAR(50)  DEFAULT '' COMMENT '样式（前端着色用）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT DEFAULT 0,
    INDEX `idx_dict_type` (`dict_type`),
    INDEX `idx_deleted`   (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`, `status`, `css_class`) VALUES
    ('gender',      '男',   '1', 1, 1, ''),
    ('gender',      '女',   '2', 2, 1, ''),
    ('gender',      '未知', '0', 3, 1, ''),
    ('user_status', '正常', '1', 1, 1, 'success'),
    ('user_status', '禁用', '0', 2, 1, 'danger'),
    ('yes_no',      '是',   '1', 1, 1, 'success'),
    ('yes_no',      '否',   '0', 2, 1, 'danger'),
    ('order_status','待支付','0', 1, 1, 'warning'),
    ('order_status','已支付','1', 2, 1, 'success'),
    ('order_status','已完成','2', 3, 1, ''),
    ('order_status','已取消','3', 4, 1, 'danger');

-- ================================================================
-- 4. 系统配置表 sys_config
-- ================================================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `config_key`   VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_name`  VARCHAR(100) DEFAULT '' COMMENT '配置名称（中文说明）',
    `group_name`   VARCHAR(50)  DEFAULT 'default' COMMENT '分组',
    `config_type`  VARCHAR(20)  DEFAULT 'string' COMMENT '类型：string/number/boolean/json',
    `readonly`     TINYINT DEFAULT 0 COMMENT '是否内置：1=内置不可改',
    `status`       TINYINT DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `remark`       VARCHAR(255) DEFAULT '' COMMENT '备注',
    `sort`         INT DEFAULT 0 COMMENT '排序',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT DEFAULT 0,
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_group_name`(`group_name`),
    INDEX `idx_deleted`   (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `group_name`, `config_type`, `readonly`, `remark`) VALUES
    ('sys.name',         'Java后端脚手架',       '系统名称',        'basic', 'string',  1, '系统名称'),
    ('sys.logo',         '/assets/logo.png',     '系统Logo',        'basic', 'string',  1, 'Logo路径'),
    ('upload.max.size',  '10485760',             '文件上传大小限制', 'upload', 'number', 1, '单位：字节，默认10MB'),
    ('jwt.expiration',   '604800',               'Token有效期',      'auth',  'number', 1, '单位：秒，默认7天'),
    ('sys.maintenance',   'false',               '维护模式',         'basic', 'boolean', 1, '开启后用户无法访问');

-- ================================================================
-- 5. 文件记录表 sys_file_record
-- ================================================================
DROP TABLE IF EXISTS `sys_file_record`;
CREATE TABLE `sys_file_record` (
    `id`             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `original_name`  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name`    VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    `file_path`      VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_url`       VARCHAR(500) DEFAULT '' COMMENT '访问URL',
    `file_size`      BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    `content_type`   VARCHAR(100) DEFAULT '' COMMENT 'MIME类型',
    `extension`      VARCHAR(20)  DEFAULT '' COMMENT '文件扩展名',
    `uploader_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人ID',
    `uploader_name`  VARCHAR(50)  DEFAULT '' COMMENT '上传人昵称',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT DEFAULT 0,
    INDEX `idx_uploader_id`(`uploader_id`),
    INDEX `idx_deleted`    (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ================================================================
SELECT '✅ 初始化完成' AS msg;
SELECT '用户表' AS tbl, COUNT(*) AS rows FROM `sys_user`;
SELECT '字典表' AS tbl, COUNT(*) AS rows FROM `sys_dict_data`;
SELECT '配置表' AS tbl, COUNT(*) AS rows FROM `sys_config`;
