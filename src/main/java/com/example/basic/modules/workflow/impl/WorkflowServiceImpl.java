package com.example.basic.modules.workflow.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.user.dao.UserDao;
import com.example.basic.modules.user.entity.User;
import com.example.basic.modules.workflow.dao.*;
import com.example.basic.modules.workflow.entity.*;
import com.example.basic.modules.workflow.model.*;
import com.example.basic.modules.workflow.service.WorkflowService;
import com.example.basic.util.JsonUtil;
import com.example.basic.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流服务实现。
 *
 * <p>核心流程：
 * <pre>
 * 提交申请 → 创建 Instance → 创建第一个 Task → 审批人处理
 *   → 同意 → 查找下一个节点 → 创建下一个 Task → ...
 *   → 驳回 → Instance.status = REJECTED → 结束
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class WorkflowServiceImpl implements WorkflowService {

    private final WfDefinitionDao definitionDao;
    private final WfInstanceDao instanceDao;
    private final WfTaskDao taskDao;
    private final WfTaskHistoryDao historyDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    // ==================== 定义管理 ====================

    @Override
    public IPage<WfDefinition> pageDefinitions(WfDefinition query, com.example.basic.model.query.PageParams pageParams) {
        LambdaQueryWrapper<WfDefinition> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getName())) w.like(WfDefinition::getName, query.getName());
        if (StrUtil.isNotBlank(query.getCode())) w.eq(WfDefinition::getCode, query.getCode());
        if (query.getStatus() != null) w.eq(WfDefinition::getStatus, query.getStatus());
        w.orderByDesc(WfDefinition::getCreatedTime);
        return definitionDao.selectPage(pageParams.toPage(), w);
    }

    @Override
    public WfDefinition getDefinitionById(Long id) {
        return definitionDao.selectById(id);
    }

    @Override
    public Long saveDefinition(WfDefinitionSaveDTO dto, Long operatorId) {
        // 校验编码唯一
        long exists = definitionDao.selectCount(
            new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getCode, dto.getCode())
                .eq(dto.getId() == null, 1, 1) // 忽略空判断
        );
        if (exists > 0 && (dto.getId() == null)) {
            throw new BizException(ResultCode.BAD_REQUEST, "工作流编码已存在");
        }

        WfDefinition def = new WfDefinition();
        def.setName(dto.getName());
        def.setCode(dto.getCode());
        def.setDescription(dto.getDescription());
        def.setFormCode(dto.getFormCode());
        def.setConfig(JsonUtil.toJson(dto.getConfig()));
        def.setStatus(WfDefinition.STATUS_DRAFT);
        def.setVersion(1);
        def.setCreatedBy(operatorId);
        definitionDao.insert(def);

        log.info("创建工作流定义 | id={} | name={}", def.getId(), def.getName());
        return def.getId();
    }

    @Override
    public void publishDefinition(Long id) {
        WfDefinition def = definitionDao.selectById(id);
        if (def == null) throw new BizException(ResultCode.BAD_REQUEST, "工作流不存在");
        if (def.getStatus() == WfDefinition.STATUS_PUBLISHED)
            throw new BizException(ResultCode.BAD_REQUEST, "该版本已发布，请新建版本");

        def.setStatus(WfDefinition.STATUS_PUBLISHED);
        definitionDao.updateById(def);
        log.info("发布工作流 | id={} | name={}", id, def.getName());
    }

    @Override
    public void disableDefinition(Long id) {
        WfDefinition def = definitionDao.selectById(id);
        if (def == null) throw new BizException(ResultCode.BAD_REQUEST, "工作流不存在");
        def.setStatus(WfDefinition.STATUS_DISABLED);
        definitionDao.updateById(def);
    }

    // ==================== 提交流程（核心） ====================

    @Override
    @Transactional
    public Long submit(WfSubmitDTO dto, Long initiatorId) {
        // 1. 查询已发布的工作流定义
        WfDefinition def = definitionDao.selectOne(
            new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getCode, dto.getDefinitionCode())
                .eq(WfDefinition::getStatus, WfDefinition.STATUS_PUBLISHED)
        );
        if (def == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "工作流不存在或未发布");
        }

        // 2. 解析流程配置
        WfDefinitionConfig config = JsonUtil.fromJson(def.getConfig(), WfDefinitionConfig.class);

        // 3. 创建工作流实例
        WfInstance instance = new WfInstance();
        instance.setDefinitionId(def.getId());
        instance.setDefinitionCode(def.getCode());
        instance.setBusinessId(dto.getBusinessId());
        instance.setBusinessType(dto.getBusinessType());
        instance.setTitle(dto.getTitle());
        instance.setFormData(JsonUtil.toJson(dto.getFormData()));
        instance.setStatus(WfInstance.STATUS_RUNNING);
        instance.setInitiatorId(initiatorId);
        instance.setStartedAt(new Date());
        instanceDao.insert(instance);

        // 4. 记录历史：提交
        saveHistory(instance, null, null, initiatorId, WfTaskHistory.ACTION_SUBMIT, dto.getTitle());

        // 5. 启动流程：找到第一个任务节点，创建任务
        Node firstTask = findFirstTaskNode(config);
        if (firstTask != null) {
            createTask(instance, config, firstTask, initiatorId);
        } else {
            // 没有审批节点，直接结束
            completeInstance(instance);
        }

        log.info("提交工作流申请 | instanceId={} | title={}", instance.getId(), instance.getTitle());
        return instance.getId();
    }

    /**
     * 查找第一个需要审批的节点（从 start 节点出发的第一个非end节点）
     */
    private Node findFirstTaskNode(WfDefinitionConfig config) {
        // 找 start 节点
        Node startNode = config.getNodes().stream()
            .filter(n -> "start".equals(n.getType()))
            .findFirst().orElse(null);
        if (startNode == null) return null;

        // 找 start 节点的下一跳
        Edge outEdge = config.getEdges().stream()
            .filter(e -> e.getSource().equals(startNode.getId()))
            .findFirst().orElse(null);
        if (outEdge == null) return null;

        return config.getNodes().stream()
            .filter(n -> n.getId().equals(outEdge.getTarget()))
            .findFirst().orElse(null);
    }

    /**
     * 创建审批任务
     */
    private void createTask(WfInstance instance, WfDefinitionConfig config, Node node, Long initiatorId) {
        if ("end".equals(node.getType())) {
            completeInstance(instance);
            return;
        }

        if ("condition".equals(node.getType())) {
            // 条件节点：评估条件，决定下一步
            handleConditionNode(instance, config, node, initiatorId);
            return;
        }

        if ("approver".equals(node.getType())) {
            NodeData data = node.getData();
            List<Long> assigneeIds = resolveAssignees(data, instance);

            // 更新实例当前节点
            instance.setCurrentNodeId(node.getId());
            instanceDao.updateById(instance);

            int seq = 1;
            for (Long assigneeId : assigneeIds) {
                User assignee = userDao.selectById(assigneeId);
                WfTask task = new WfTask();
                task.setInstanceId(instance.getId());
                task.setNodeId(node.getId());
                task.setNodeName(data.getName());
                task.setAssigneeId(assigneeId);
                task.setAssigneeName(assignee != null ? assignee.getName() : "");
                task.setAssigneeType(data.getAssigneeType());
                task.setAssigneeExpr(data.getAssigneeExpr());
                task.setStatus(WfTask.STATUS_PENDING);
                task.setSequence(seq++);
                task.setCreatedTime(new Date());
                taskDao.insert(task);
            }

            // 发送通知给审批人
            if (!assigneeIds.isEmpty()) {
                sendApprovalNotification(instance, node.getData().getName(), assigneeIds);
            }
        }
    }

    /**
     * 处理条件节点：评估条件，走对应分支
     */
    private void handleConditionNode(WfInstance instance, WfDefinitionConfig config, Node node, Long initiatorId) {
        NodeData data = node.getData();
        Map<String, Object> formData = JsonUtil.fromJson(instance.getFormData(), Map.class);

        String targetNodeId = null;

        if (data.getConditions() != null) {
            for (Condition cond : data.getConditions()) {
                if (evalCondition(cond.getExpr(), formData)) {
                    targetNodeId = cond.getTargetNodeId();
                    break;
                }
            }
        }

        // 默认分支
        if (targetNodeId == null) {
            targetNodeId = data.getDefaultNodeId();
        }

        // 找到下一个节点
        Node nextNode = config.getNodes().stream()
            .filter(n -> n.getId().equals(targetNodeId))
            .findFirst().orElse(null);

        if (nextNode != null) {
            createTask(instance, config, nextNode, initiatorId);
        }
    }

    /**
     * 评估条件表达式（简化版，支持 > < >= <= == !=）
     */
    private boolean evalCondition(String expr, Map<String, Object> formData) {
        if (StrUtil.isBlank(expr)) return false;
        try {
            // 简化实现：替换变量为实际值
            String evalExpr = expr;
            for (Map.Entry<String, Object> e : formData.entrySet()) {
                evalExpr = evalExpr.replace(e.getKey(), String.valueOf(e.getValue()));
            }
            // 使用 ScriptEngine 计算布尔结果
            Object result = new javax.script.ScriptEngineManager()
                .getEngineByName("JavaScript").eval(evalExpr);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("条件表达式计算失败 | expr={} | error={}", expr, e.getMessage());
            return false;
        }
    }

    /**
     * 解析审批人
     * <pre>
     * user:123     → 直接返回用户ID
     * role:manager → 查询该角色的用户
     * ${initiator} → 返回发起人ID
     * </pre>
     */
    private List<Long> resolveAssignees(NodeData data, WfInstance instance) {
        List<Long> result = new ArrayList<>();
        if (data.getAssigneeType() == null) return result;

        switch (data.getAssigneeType()) {
            case 1: // 指定人（从 assigneeExpr 解析）
                String expr = data.getAssigneeExpr();
                if (StrUtil.isBlank(expr)) break;
                if (expr.startsWith("user:")) {
                    Long userId = Long.parseLong(expr.substring(5));
                    result.add(userId);
                } else if (expr.equals("${initiator}")) {
                    result.add(instance.getInitiatorId());
                } else if (expr.startsWith("role:")) {
                    // TODO: 查询角色下的用户列表
                    // List<User> users = userDao.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, expr.substring(5)));
                    // result.addAll(users.stream().map(User::getId).collect(Collectors.toList()));
                }
                break;

            case 3: // 发起人自选：前端传了 assigneeId 参数
                // 这种情况由调用方在 DTO 中传入
                break;
        }
        return result;
    }

    // ==================== 审批操作 ====================

    @Override
    @Transactional
    public void approve(WfApproveDTO dto, Long operatorId) {
        WfTask task = taskDao.selectById(dto.getTaskId());
        if (task == null) throw new BizException(ResultCode.BAD_REQUEST, "任务不存在");
        if (task.getStatus() != WfTask.STATUS_PENDING)
            throw new BizException(ResultCode.BAD_REQUEST, "任务已处理，请勿重复审批");

        WfInstance instance = instanceDao.selectById(task.getInstanceId());
        WfDefinition def = definitionDao.selectById(instance.getDefinitionId());
        WfDefinitionConfig config = JsonUtil.fromJson(def.getConfig(), WfDefinitionConfig.class);

        // 1. 记录历史
        User operator = userDao.selectById(operatorId);
        saveHistory(instance, task, StrUtil.str(task.getNodeId()), operatorId,
            dto.getAction(), dto.getOpinion());

        // 2. 更新任务状态
        task.setStatus(dto.isAgree() ? WfTask.STATUS_APPROVED : WfTask.STATUS_REJECTED);
        task.setAction(dto.getAction());
        task.setOpinion(dto.getOpinion());
        task.setOperatedAt(new Date());
        task.setOperatorId(operatorId);
        taskDao.updateById(task);

        // 3. 判断是否还有其他未完成的并行任务
        boolean hasOtherPending = taskDao.selectCount(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, instance.getId())
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING)
                .ne(WfTask::getId, task.getId())
        ) > 0;

        if (hasOtherPending) {
            // 并行审批：等待其他审批人
            log.info("并行审批中，等待其他审批人 | taskId={}", task.getId());
            return;
        }

        // 4. 查找下一个节点
        if (dto.isAgree()) {
            // 同意 → 流向下一个节点
            Node nextNode = findNextNode(config, task.getNodeId());
            if (nextNode != null) {
                createTask(instance, config, nextNode, instance.getInitiatorId());
            } else {
                completeInstance(instance);
            }
        } else {
            // 拒绝 → 流程结束
            instance.setStatus(WfInstance.STATUS_REJECTED);
            instance.setFinishedAt(new Date());
            instanceDao.updateById(instance);

            // 通知发起人
            notificationService.sendDefault(
                "\u26a0\ufe0f 您的申请已被驳回",
                StrUtil.format("标题：{} \n审批人：{} \n意见：{}",
                    instance.getTitle(),
                    operator != null ? operator.getName() : "",
                    dto.getOpinion())
            );
        }
    }

    @Override
    public void revoke(WfRevokeDTO dto, Long operatorId) {
        WfInstance instance = instanceDao.selectById(dto.getInstanceId());
        if (instance == null) throw new BizException(ResultCode.BAD_REQUEST, "实例不存在");
        if (!instance.getInitiatorId().equals(operatorId))
            throw new BizException(ResultCode.BAD_REQUEST, "只有发起人可以撤回");
        if (instance.getStatus() != WfInstance.STATUS_RUNNING)
            throw new BizException(ResultCode.BAD_REQUEST, "当前状态不允许撤回");

        instance.setStatus(WfInstance.STATUS_REVOKED);
        instance.setFinishedAt(new Date());
        instanceDao.updateById(instance);

        // 取消所有待审批任务
        taskDao.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WfTask>()
                .eq(WfTask::getInstanceId, dto.getInstanceId())
                .eq(WfTask::getStatus, WfTask.STATUS_PENDING)
                .set(WfTask::getStatus, WfTask.STATUS_APPROVED)
        );

        saveHistory(instance, null, null, operatorId, WfTaskHistory.ACTION_REVOKE, dto.getOpinion());
        log.info("撤回申请 | instanceId={}", dto.getInstanceId());
    }

    /** 查找下一个节点 */
    private Node findNextNode(WfDefinitionConfig config, String currentNodeId) {
        // 找当前节点的出边
        Edge outEdge = config.getEdges().stream()
            .filter(e -> e.getSource().equals(currentNodeId))
            .findFirst().orElse(null);
        if (outEdge == null) return null;

        return config.getNodes().stream()
            .filter(n -> n.getId().equals(outEdge.getTarget()))
            .findFirst().orElse(null);
    }

    /** 完成流程实例 */
    private void completeInstance(WfInstance instance) {
        instance.setStatus(WfInstance.STATUS_APPROVED);
        instance.setFinishedAt(new Date());
        instanceDao.updateById(instance);

        // 通知发起人
        notificationService.sendDefault(
            "\u2705 您的申请已通过全部审批",
            StrUtil.format("标题：{} \n申请时间：{}", instance.getTitle(), instance.getStartedAt())
        );
        log.info("工作流完成 | instanceId={}", instance.getId());
    }

    /** 保存历史记录 */
    private void saveHistory(WfInstance instance, WfTask task, String nodeId, Long operatorId, String action, String opinion) {
        User operator = userDao.selectById(operatorId);
        WfTaskHistory h = new WfTaskHistory();
        h.setTaskId(task != null ? task.getId() : null);
        h.setInstanceId(instance.getId());
        h.setNodeId(nodeId != null ? nodeId : instance.getCurrentNodeId());
        h.setNodeName(task != null ? task.getNodeName() : "");
        h.setOperatorId(operatorId);
        h.setOperatorName(operator != null ? operator.getName() : "");
        h.setAction(action);
        h.setOpinion(opinion);
        h.setOperatedAt(new Date());
        historyDao.insert(h);
    }

    // ==================== 查询 ====================

    @Override
    public IPage<WfInstance> pageInstances(WfInstanceQuery query, com.example.basic.model.query.PageParams pageParams) {
        LambdaQueryWrapper<WfInstance> w = new LambdaQueryWrapper<>();
        w.eq(query.getInitiatorId() != null, WfInstance::getInitiatorId, query.getInitiatorId());
        w.eq(query.getStatus() != null, WfInstance::getStatus, query.getStatus());
        w.eq(StrUtil.isNotBlank(query.getDefinitionCode()), WfInstance::getDefinitionCode, query.getDefinitionCode());
        w.orderByDesc(WfInstance::getStartedAt);
        return instanceDao.selectPage(pageParams.toPage(), w);
    }

    @Override
    public IPage<WfTask> pageMyTasks(Long assigneeId, com.example.basic.model.query.PageParams pageParams) {
        LambdaQueryWrapper<WfTask> w = new LambdaQueryWrapper<>();
        w.eq(WfTask::getAssigneeId, assigneeId);
        w.eq(WfTask::getStatus, WfTask.STATUS_PENDING);
        w.orderByDesc(WfTask::getCreatedTime);
        return taskDao.selectPage(pageParams.toPage(), w);
    }

    @Override
    public WfInstance getInstanceById(Long id) {
        return instanceDao.selectById(id);
    }

    @Override
    public List<WfTaskHistory> getInstanceHistory(Long instanceId) {
        return historyDao.selectList(
            new LambdaQueryWrapper<WfTaskHistory>()
                .eq(WfTaskHistory::getInstanceId, instanceId)
                .orderByAsc(WfTaskHistory::getOperatedAt)
        );
    }

    // ==================== 通知 ====================

    @Async
    private void sendApprovalNotification(WfInstance instance, String nodeName, List<Long> assigneeIds) {
        String content = StrUtil.format(
            "\ud83d\udce3 您有一个待审批任务 \n\n标题：{} \n节点：{} \n申请人：{} \n时间：{}",
            instance.getTitle(),
            nodeName,
            instance.getInitiatorId(),
            instance.getStartedAt()
        );
        for (Long userId : assigneeIds) {
            try {
                notificationService.sendDefault("\ud83d\udce3 待审批任务通知", content);
            } catch (Exception e) {
                log.error("发送审批通知失败 | assigneeId={}", userId, e);
            }
        }
    }
}
