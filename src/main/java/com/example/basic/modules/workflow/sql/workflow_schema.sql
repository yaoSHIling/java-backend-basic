
-- ============================================================
-- 工作流模块（参考 Coze 工作流节点模型）
-- 画布节点类型：start / end / llm / code / condition / approval /
--               http / variable / loop / subflow / message / database
-- ============================================================

-- 工作流定义表
CREATE TABLE wf_definition (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(200)  NOT NULL COMMENT '工作流名称',
    code            VARCHAR(100)  NOT NULL COMMENT '工作流编码',
    description     VARCHAR(500)  COMMENT '描述',
    version         INT           NOT NULL DEFAULT 1,
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布 2=禁用',
    graph_data      JSON          NOT NULL COMMENT '画布数据：nodes + edges',
    variables       JSON          COMMENT '全局变量定义',
    created_by      BIGINT        NOT NULL,
    created_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_code_version (code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- 工作流实例表
CREATE TABLE wf_instance (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    definition_id   BIGINT        NOT NULL COMMENT '工作流定义ID',
    definition_code VARCHAR(100)  NOT NULL,
    graph_data      JSON          COMMENT '运行时快照',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=运行中 1=成功 2=失败 3=暂停',
    input_data      JSON          COMMENT '输入参数',
    output_data     JSON          COMMENT '输出结果',
    error_msg       VARCHAR(1000) COMMENT '错误信息',
    current_node_id VARCHAR(100)   COMMENT '当前节点ID',
    initiator_id    BIGINT        NOT NULL,
    started_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at     DATETIME      COMMENT '结束时间',
    deleted         TINYINT       NOT NULL DEFAULT 0,
    KEY idx_definition (definition_id),
    KEY idx_initiator (initiator_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例表';

-- 工作流执行日志
CREATE TABLE wf_instance_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id     BIGINT        NOT NULL,
    node_id         VARCHAR(100)  NOT NULL COMMENT '节点ID',
    node_name       VARCHAR(200)  NOT NULL COMMENT '节点名称',
    node_type       VARCHAR(50)   NOT NULL COMMENT '节点类型',
    status          TINYINT       NOT NULL COMMENT '0=等待 1=运行中 2=成功 3=失败',
    input_data      JSON          COMMENT '节点输入',
    output_data     JSON          COMMENT '节点输出',
    error_msg       VARCHAR(500)  COMMENT '错误信息',
    started_at      DATETIME      NOT NULL,
    finished_at     DATETIME,
    elapsed_ms      INT COMMENT '执行耗时ms',
    KEY idx_instance (instance_id),
    KEY idx_node (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行日志表';
