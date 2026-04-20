package com.example.basic.modules.workflow.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.PageResult;
import com.example.basic.common.result.Result;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.user.entity.User;
import com.example.basic.modules.workflow.entity.*;
import com.example.basic.modules.workflow.impl.WorkflowServiceImpl;
import com.example.basic.modules.workflow.model.*;
import com.example.basic.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流接口。
 *
 * <p>分为两大部分：
 * <ul>
 *   <li>管理端：工作流定义 CRUD + 发布/禁用</li>
 *   <li>用户端：提交申请 / 我的待办 / 我的已办 / 审批操作</li>
 * </ul>
 */
@Tag(name = "11. 工作流引擎", description = "可配置审批流程引擎")
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowServiceImpl workflowService;
    private final NotificationService notificationService;

    // ==================== 管理端：定义管理 ====================

    @Operation(summary = "工作流定义分页")
    @GetMapping("/definition/page")
    @Login
    public Result<PageResult<WfDefinition>> pageDefinitions(
            @Parameter(description = "定义ID") WfDefinition query,
            PageParams pageParams) {
        return Result.success(PageResult.of(workflowService.pageDefinitions(query, pageParams)));
    }

    @Operation(summary = "获取工作流定义详情")
    @GetMapping("/definition/{id}")
    @Login
    public Result<WfDefinition> getDefinition(@PathVariable Long id) {
        return Result.success(workflowService.getDefinitionById(id));
    }

    @Operation(summary = "创建/更新工作流定义")
    @PostMapping("/definition")
    @Login
    @LogOperation("创建工作流")
    public Result<Long> saveDefinition(@RequestBody WfDefinitionSaveDTO dto, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(workflowService.saveDefinition(dto, userId));
    }

    @Operation(summary = "发布工作流")
    @PostMapping("/definition/{id}/publish")
    @Login
    @LogOperation("发布工作流")
    public Result<Void> publish(@PathVariable Long id) {
        workflowService.publishDefinition(id);
        return Result.success("发布成功");
    }

    @Operation(summary = "禁用工作流")
    @PostMapping("/definition/{id}/disable")
    @Login
    @LogOperation("禁用工作流")
    public Result<Void> disable(@PathVariable Long id) {
        workflowService.disableDefinition(id);
        return Result.success("禁用成功");
    }

    // ==================== 用户端：流程操作 ====================

    @Operation(summary = "提交工作流申请")
    @PostMapping("/submit")
    @Login
    @LogOperation("提交工作流申请")
    public Result<Long> submit(@RequestBody WfSubmitDTO dto, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(workflowService.submit(dto, userId));
    }

    @Operation(summary = "我的待审批任务列表")
    @GetMapping("/task/my")
    @Login
    public Result<PageResult<WfTask>> myTasks(PageParams pageParams, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(PageResult.of(workflowService.pageMyTasks(userId, pageParams)));
    }

    @Operation(summary = "审批操作（同意/拒绝/转交）")
    @PostMapping("/task/approve")
    @Login
    @LogOperation("审批操作")
    public Result<Void> approve(@RequestBody WfApproveDTO dto, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        workflowService.approve(dto, userId);
        return Result.success("审批完成");
    }

    @Operation(summary = "撤回申请")
    @PostMapping("/instance/revoke")
    @Login
    @LogOperation("撤回申请")
    public Result<Void> revoke(@RequestBody WfRevokeDTO dto, HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        workflowService.revoke(dto, userId);
        return Result.success("撤回成功");
    }

    @Operation(summary = "我的申请记录")
    @GetMapping("/instance/my")
    @Login
    public Result<PageResult<WfInstance>> myInstances(
            @Parameter(description = "实例ID") Long instanceId,
            @Parameter(description = "状态") Integer status,
            PageParams pageParams,
            HttpServletRequest request) {
        Long userId = JwtUtil.getLoginUser(request).getUserId();
        WfInstanceQuery query = new WfInstanceQuery();
        query.setInitiatorId(userId);
        query.setStatus(status);
        return Result.success(PageResult.of(workflowService.pageInstances(query, pageParams)));
    }

    @Operation(summary = "查看流程实例详情")
    @GetMapping("/instance/{id}")
    @Login
    public Result<WfInstance> getInstance(@PathVariable Long id) {
        return Result.success(workflowService.getInstanceById(id));
    }

    @Operation(summary = "查看流程审批历史")
    @GetMapping("/instance/{id}/history")
    @Login
    public Result<List<WfTaskHistory>> getHistory(@PathVariable Long id) {
        return Result.success(workflowService.getInstanceHistory(id));
    }

    // ==================== 内部 DTO（放这里简化） ====================

    @lombok.Data
    public static class WfDefinitionSaveDTO {
        private Long id;
        private String name;
        private String code;
        private String description;
        private String formCode;
        private WfDefinitionConfig config;
    }

    @lombok.Data
    public static class WfSubmitDTO {
        private String definitionCode;
        private String businessId;
        private String businessType;
        private String title;
        private java.util.Map<String, Object> formData;
        private Long assigneeId; // 审批人自选时传入
    }

    @lombok.Data
    public static class WfApproveDTO {
        private Long taskId;
        private String action;   // agree/reject/transfer
        private String opinion;
        private Long transferTo; // 转交目标

        public boolean isAgree() { return "agree".equals(action); }
    }

    @lombok.Data
    public static class WfRevokeDTO {
        private Long instanceId;
        private String opinion;
    }

    @lombok.Data
    public static class WfInstanceQuery {
        private Long initiatorId;
        private Integer status;
        private String definitionCode;
    }
}
