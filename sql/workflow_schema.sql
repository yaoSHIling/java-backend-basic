
-- ============================================================
-- 工作流模块数据库 Schema
-- 对应后端模块：java-backend-basic/modules/workflow
-- ============================================================

-- 工作流定义表
CREATE TABLE wf_definition (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(200)  NOT NULL COMMENT '工作流名称',
    code            VARCHAR(100)  NOT NULL COMMENT '工作流编码（唯一）',
    description     VARCHAR(500)  COMMENT '描述',
    version         INT           NOT NULL DEFAULT 1 COMMENT '版本号',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已发布 2=已禁用',
    form_code       VARCHAR(100)  COMMENT '关联表单编码',
    config          JSON          COMMENT '完整流程配置（nodes + edges）',
    created_by      BIGINT        NOT NULL COMMENT '创建人ID',
    created_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_code_version (code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- 工作流实例表（每次提交申请创建一条）
CREATE TABLE wf_instance (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    definition_id   BIGINT        NOT NULL COMMENT '工作流定义ID',
    definition_code VARCHAR(100)  NOT NULL COMMENT '工作流编码',
    business_id     VARCHAR(100)  COMMENT '关联业务ID（如订单ID/报销单ID）',
    business_type   VARCHAR(100)  COMMENT '业务类型（如 order/reimburse）',
    title           VARCHAR(300)  NOT NULL COMMENT '流程标题（如：请假申请 - 张三 - 2024-04-20）',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=审批中 1=已通过 2=已拒绝 3=已撤回',
    form_data       JSON          COMMENT '提交的表单数据',
    current_node_id VARCHAR(100)  COMMENT '当前节点ID',
    initiator_id    BIGINT        NOT NULL COMMENT '发起人ID',
    started_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    finished_at     DATETIME      COMMENT '结束时间',
    deleted         TINYINT       NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例表';

-- 审批任务表（每个审批节点生成一条或多条）
CREATE TABLE wf_task (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id     BIGINT        NOT NULL COMMENT '工作流实例ID',
    definition_id   BIGINT        COMMENT '工作流定义ID',
    definition_code VARCHAR(100)  COMMENT '工作流编码',
    node_id         VARCHAR(100)  NOT NULL COMMENT '节点ID（对应definition中的nodeId）',
    node_name       VARCHAR(200)  NOT NULL COMMENT '节点名称（如：部门主管审批）',
    assignee_id     BIGINT        COMMENT '审批人ID（单人审批）',
    assignee_name   VARCHAR(100)  COMMENT '审批人姓名',
    assignee_type   TINYINT       NOT NULL DEFAULT 1 COMMENT '审批人类型：1=指定人 2=角色 3=发起人自选',
    assignee_expr   VARCHAR(500)  COMMENT '审批人表达式（如：role:manager）',
    title           VARCHAR(300)  COMMENT '审批标题',
    content         TEXT          COMMENT '审批内容',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=待审批 1=已审批 2=已转交 3=已驳回',
    opinion         VARCHAR(1000) COMMENT '审批意见',
    action          VARCHAR(20)   COMMENT '操作：agree=同意 / reject=拒绝 / transfer=转交',
    operated_at     DATETIME      COMMENT '审批时间',
    operator_id     BIGINT        COMMENT '实际操作人',
    operator_name   VARCHAR(100)  COMMENT '实际操作人姓名',
    sequence        INT           NOT NULL DEFAULT 1 COMMENT '审批顺序（第几人）',
    created_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    KEY idx_instance (instance_id),
    KEY idx_assignee (assignee_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 任务历史（每个操作记录一条）
CREATE TABLE wf_task_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id         BIGINT        NOT NULL COMMENT '原任务ID',
    instance_id     BIGINT        NOT NULL COMMENT '工作流实例ID',
    node_id         VARCHAR(100)  NOT NULL COMMENT '节点ID',
    node_name       VARCHAR(200)  NOT NULL COMMENT '节点名称',
    operator_id     BIGINT        NOT NULL COMMENT '操作人ID',
    operator_name   VARCHAR(100)  NOT NULL COMMENT '操作人姓名',
    action          VARCHAR(20)   NOT NULL COMMENT '操作：submit=提交/agree=同意/reject=拒绝/transfer=转交/revoke=撤回',
    opinion         VARCHAR(1000) COMMENT '审批意见',
    operated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    deleted         TINYINT       NOT NULL DEFAULT 0,
    KEY idx_instance (instance_id),
    KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务历史表';

-- ============================================================
-- 工作流节点类型说明
-- ============================================================
-- node.type = 'start'     ：发起节点（流程起点，必填，自动跳过）
-- node.type = 'end'        ：结束节点（流程终点）
-- node.type = 'approver'   ：审批节点（assignee=审批人配置）
-- node.type = 'condition'   ：条件分支节点（conditions=条件列表）
-- node.type = 'auto'       ：自动节点（自动执行逻辑，如发送通知）

-- 审批人表达式（assignee_expr）格式：
--   user:123           → 指定用户ID
--   role:manager       → 指定角色
--   ${initiator}       → 发起人自己
--   ${initiator_leader}→ 发起人的直属领导

-- 条件表达式（condition.expr）格式：
--   amount > 1000      → 表单金额大于1000
--   type == 'reimburse'→ 表单类型为reimburse
--   level == 1         → 等级等于1
-- ============================================================
