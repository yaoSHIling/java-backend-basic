package com.example.basic.modules.workflow.controller;

import cn.hutool.json.JSONUtil;
import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.PageResult;
import com.example.basic.common.result.Result;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.workflow.engine.WorkflowEngine;
import com.example.basic.modules.workflow.engine.WfGraph;
import com.example.basic.modules.workflow.entity.WfDefinition;
import com.example.basic.modules.workflow.entity.WfInstance;
import com.example.basic.modules.workflow.entity.WfInstanceLog;
import com.example.basic.modules.workflow.entity.WfTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.basic.modules.workflow.dao.WfDefinitionDao;
import com.example.basic.modules.workflow.dao.WfInstanceDao;
import com.example.basic.modules.workflow.dao.WfInstanceLogDao;
import com.example.basic.modules.workflow.dao.WfTaskDao;
import com.example.basic.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流接口（参考 Coze 工作流 API）
 *
 * <p>提供：
 * <ul>
 *   <li>工作流定义管理（CRUD + 发布/禁用）</li>
 *   <li>工作流执行（同步/异步触发）</li>
 *   <li>执行结果查询</li>
 *   <li>审批回调（/workflow/callback/approve）</li>
 * </ul>
 */
@Tag(name = "12. 工作流引擎（Coze 风格）", description = "可视化流程编排 + 执行引擎")
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WfDefinitionDao definitionDao;
    private final WfInstanceDao instanceDao;
    private final WfInstanceLogDao logDao;
    private final WfTaskDao taskDao;
    private final WorkflowEngine workflowEngine;

    // ==================== 定义管理 ====================

    @Operation(summary = "工作流定义分页")
    @GetMapping("/definition/page")
    @Login
    public Result<PageResult<WfDefinition>> pageDefinitions(
            WfDefinition query,
            PageParams pageParams) {
        LambdaQueryWrapper<WfDefinition> w = new LambdaQueryWrapper<>();
        if (query.getName() != null) w.like(WfDefinition::getName, query.getName());
        if (query.getCode() != null) w.eq(WfDefinition::getCode, query.getCode());
        if (query.getStatus() != null) w.eq(WfDefinition::getStatus, query.getStatus());
        w.orderByDesc(WfDefinition::getCreatedTime);
        return Result.success(PageResult.of(definitionDao.selectPage(pageParams.toPage(), w)));
    }

    @Operation(summary = "获取工作流定义详情")
    @GetMapping("/definition/{id}")
    @Login
    public Result<WfDefinition> getDefinition(@PathVariable Long id) {
        WfDefinition def = definitionDao.selectById(id);
        if (def == null) return Result.fail(500, "工作流不存在");
        return Result.success(def);
    }

    @Operation(summary = "创建/更新工作流定义")
    @PostMapping("/definition")
    @Login
    @LogOperation("保存工作流")
    public Result<Long> saveDefinition(@RequestBody SaveDefDTO dto, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        WfDefinition def;
        if (dto.getId() != null) {
            def = definitionDao.selectById(dto.getId());
        } else {
            def = new WfDefinition();
            def.setCreatedBy(userId);
            def.setCreatedTime(new Date());
            def.setVersion(1);
            def.setStatus(WfDefinition.STATUS_DRAFT);
        }
        def.setName(dto.getName());
        def.setCode(dto.getCode());
        def.setDescription(dto.getDescription());
        def.setGraphData(JSONUtil.toJsonStr(dto.getGraphData()));
        def.setVariables(dto.getVariables() != null ? JSONUtil.toJsonStr(dto.getVariables()) : null);
        def.setUpdatedTime(new Date());
        if (def.getId() == null) {
            definitionDao.insert(def);
        } else {
            definitionDao.updateById(def);
        }
        return Result.success(def.getId());
    }

    @Operation(summary = "发布工作流")
    @PostMapping("/definition/{id}/publish")
    @Login
    @LogOperation("发布工作流")
    public Result<Void> publish(@PathVariable Long id) {
        WfDefinition def = definitionDao.selectById(id);
        if (def == null) return Result.fail(500, "工作流不存在");
        def.setStatus(WfDefinition.STATUS_PUBLISHED);
        definitionDao.updateById(def);
        return Result.success(null, "发布成功");
    }

    @Operation(summary = "禁用工作流")
    @PostMapping("/definition/{id}/disable")
    @Login
    @LogOperation("禁用工作流")
    public Result<Void> disable(@PathVariable Long id) {
        WfDefinition def = definitionDao.selectById(id);
        if (def == null) return Result.fail(500, "工作流不存在");
        def.setStatus(WfDefinition.STATUS_DISABLED);
        definitionDao.updateById(def);
        return Result.success(null, "禁用成功");
    }

    @Operation(summary = "删除工作流")
    @DeleteMapping("/definition/{id}")
    @Login
    @LogOperation("删除工作流")
    public Result<Void> deleteDefinition(@PathVariable Long id) {
        definitionDao.deleteById(id);
        return Result.success(null, "删除成功");
    }

    // ==================== 流程执行 ====================

    @Operation(summary = "触发工作流（同步执行）")
    @PostMapping("/trigger/{definitionCode}")
    @Login
    @LogOperation("触发工作流")
    public Result<Long> trigger(
            @PathVariable String definitionCode,
            @RequestBody TriggerDTO dto,
            HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();

        WfDefinition def = definitionDao.selectOne(
            new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getCode, definitionCode)
                .eq(WfDefinition::getStatus, WfDefinition.STATUS_PUBLISHED)
        );
        if (def == null) return Result.fail(500, "工作流不存在或未发布");

        WfGraph graph = JSONUtil.toBean(def.getGraphData(), WfGraph.class);
        Long instanceId = workflowEngine.startSync(def.getId(), def.getCode(), graph, dto.getData(), userId);
        return Result.success(instanceId);
    }

    @Operation(summary = "查询执行实例")
    @GetMapping("/instance/{id}")
    @Login
    public Result<WfInstance> getInstance(@PathVariable Long id) {
        return Result.success(instanceDao.selectById(id));
    }

    @Operation(summary = "查询执行日志")
    @GetMapping("/instance/{id}/logs")
    @Login
    public Result<List<WfInstanceLog>> getLogs(@PathVariable Long id) {
        return Result.success(logDao.selectList(
            new LambdaQueryWrapper<WfInstanceLog>()
                .eq(WfInstanceLog::getInstanceId, id)
                .orderByAsc(WfInstanceLog::getStartedAt)
        ));
    }

    @Operation(summary = "我的执行实例")
    @GetMapping("/instance/my")
    @Login
    public Result<PageResult<WfInstance>> myInstances(
            @Parameter(description = "状态") Integer status,
            @Parameter(description = "关键字") String keyword,
            PageParams pageParams,
            HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        LambdaQueryWrapper<WfInstance> w = new LambdaQueryWrapper<>();
        w.eq(WfInstance::getInitiatorId, userId);
        if (status != null) w.eq(WfInstance::getStatus, status);
        if (keyword != null && !keyword.isBlank()) w.like(WfInstance::getDefinitionCode, keyword);
        w.orderByDesc(WfInstance::getStartedAt);
        return Result.success(PageResult.of(instanceDao.selectPage(pageParams.toPage(), w)));
    }

    @Operation(summary = "查询实例下的审批任务")
    @GetMapping("/task/instance/{instanceId}")
    @Login
    public Result<List<WfTask>> listTasksByInstance(@PathVariable Long instanceId, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        WfInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) return Result.fail(404, "实例不存在");
        boolean canView = Objects.equals(instance.getInitiatorId(), userId)
            || taskDao.selectCount(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, instanceId)
                .eq(WfTask::getAssigneeId, userId)) > 0;
        if (!canView) return Result.fail(403, "无权查看该实例审批任务");
        return Result.success(taskDao.selectList(
            new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, instanceId)
                .orderByAsc(WfTask::getCreatedTime)
        ));
    }

    @Operation(summary = "我的待审批任务")
    @GetMapping("/task/my")
    @Login
    public Result<PageResult<WfTask>> myTasks(
            @Parameter(description = "状态") Integer status,
            @Parameter(description = "关键字") String keyword,
            PageParams pageParams,
            HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        LambdaQueryWrapper<WfTask> w = new LambdaQueryWrapper<>();
        w.eq(WfTask::getAssigneeId, userId);
        if (status != null) w.eq(WfTask::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(WfTask::getTitle, keyword)
                .or().like(WfTask::getNodeName, keyword)
                .or().like(WfTask::getDefinitionCode, keyword)
                .or().like(WfTask::getAssigneeName, keyword));
        }
        w.orderByDesc(WfTask::getCreatedTime);
        return Result.success(PageResult.of(taskDao.selectPage(pageParams.toPage(), w)));
    }

    // ==================== 审批回调 ====================

    @Operation(summary = "审批回调（审批人审批后触发）")
    @PostMapping("/callback/approve")
    @Login
    public Result<Void> approveCallback(@RequestBody ApproveCallbackDTO dto, HttpServletRequest request) {
        Long loginUserId = JwtUtil.getLoginUser(request).getUserId();
        Long operatorId = loginUserId;
        WfTask task = taskDao.selectById(dto.getTaskId());
        if (task == null) return Result.fail(404, "审批任务不存在");
        if (task.getAssigneeId() != null && !Objects.equals(task.getAssigneeId(), loginUserId)) {
            return Result.fail(403, "当前用户无权审批该任务");
        }
        workflowEngine.resumeFromApproval(
            dto.getInstanceId(),
            dto.getTaskId(),
            dto.isApproved(),
            dto.getOpinion(),
            operatorId,
            dto.getOperatorName()
        );
        return Result.success(null, "回调成功");
    }

    // ==================== DTO ====================

    @Data
    public static class SaveDefDTO {
        private Long id;
        private String name;
        private String code;
        private String description;
        private WfGraph graphData;
        private Map<String, Object> variables;
    }

    @Data
    public static class TriggerDTO {
        private Map<String, Object> data;
    }

    @Data
    public static class ApproveCallbackDTO {
        private Long instanceId;
        private Long taskId;
        private boolean approved;
        private String opinion;
        private Long operatorId;
        private String operatorName;
    }
}
