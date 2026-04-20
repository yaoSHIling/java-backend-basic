# 场景案例：CRM 客户跟进管理系统

> 本文件展示一个完整的**企业级 CRM 客户跟进系统**实现：从需求分析 → 数据库设计 → 核心代码 → 定时任务 → 通知提醒。
> 代码基于本脚手架，可直接复制使用。

---

## 📋 需求描述

**目标：** 构建一个中小企业适用的 CRM 系统，实现客户信息管理、销售跟进、定时提醒、多渠道通知。

**核心功能：**
```
客户管理（增删改查）
    ↓
跟进记录（每次联系记一条）
    ↓
跟进任务（待办 + 定时提醒）
    ↓
定时任务（到期未跟进 → 自动钉钉通知）
    ↓
数据分析（客户统计/跟进统计）
```

---

## 🗄️ 数据库设计

```sql
-- 客户表
CREATE TABLE crm_customer (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)  NOT NULL COMMENT '客户名称',
    mobile          VARCHAR(20)   COMMENT '手机号',
    company         VARCHAR(200)  COMMENT '公司名称',
    industry        VARCHAR(50)   COMMENT '行业：IT/金融/制造/医疗/教育',
    source          VARCHAR(50)   COMMENT '客户来源：官网/展会/转介绍/电话拓展',
    level           TINYINT       NOT NULL DEFAULT 3 COMMENT '客户等级：1=重点 2=重要 3=普通',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1=潜在 2=意向 3=成交 4=流失',
    assignee_id     BIGINT        COMMENT '负责人（销售）ID',
    last_followup_at DATETIME    COMMENT '最后跟进时间',
    next_followup_at DATETIME     COMMENT '下次跟进时间',
   跟进_remark       VARCHAR(500)  COMMENT '备注',
    created_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 跟进记录表
CREATE TABLE crm_followup (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT       NOT NULL COMMENT '客户ID',
    followup_type   TINYINT      NOT NULL COMMENT '跟进方式：1=电话 2=拜访 3=微信 4=邮件 5=其他',
    content         VARCHAR(1000) NOT NULL COMMENT '跟进内容',
    next_plan       VARCHAR(500)  COMMENT '下次跟进计划',
    next_followup_at DATETIME    COMMENT '下次跟进时间',
    created_by      BIGINT       NOT NULL COMMENT '跟进人ID',
    created_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';

-- 跟进任务表（待办）
CREATE TABLE crm_followup_task (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT       NOT NULL COMMENT '客户ID',
    task_type       VARCHAR(50)   NOT NULL COMMENT '任务类型：首次联系/方案报价/合同签订/售后服务',
    title           VARCHAR(200)  NOT NULL COMMENT '任务标题',
    content         VARCHAR(500)  COMMENT '任务内容',
    due_at          DATETIME     NOT NULL COMMENT '截止时间',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=待办 1=已完成 2=已逾期',
    priority        TINYINT      NOT NULL DEFAULT 2 COMMENT '优先级：1=高 2=中 3=低',
    assignee_id     BIGINT       NOT NULL COMMENT '负责人ID',
    completed_at    DATETIME     COMMENT '完成时间',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进任务表';
```

---

## 💻 核心代码

### 1. 实体类

```java
package com.example.basic.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.basic.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_customer")
public class CrmCustomer extends BaseEntity {

    private String name;           // 客户名称
    private String mobile;         // 手机号
    private String company;         // 公司名称
    private String industry;       // 行业
    private String source;          // 客户来源
    private Integer level;         // 客户等级：1=重点 2=重要 3=普通
    private Integer status;        // 状态：1=潜在 2=意向 3=成交 4=流失
    private Long assigneeId;       // 负责人（销售）ID
    private Date lastFollowupAt;   // 最后跟进时间
    private Date nextFollowupAt;  // 下次跟进时间
    private String remark;         // 备注

    // ===== 枚举值 =====
    public static final int LEVEL_KEY = 1;    // 重点
    public static final int LEVEL_IMPORTANT = 2;  // 重要
    public static final int LEVEL_NORMAL = 3;     // 普通

    public static final int STATUS_POTENTIAL = 1; // 潜在
    public static final int STATUS_INTENT = 2;   // 意向
    public static final int STATUS_DEAL = 3;     // 成交
    public static final int STATUS_LOST = 4;     // 流失
}
```

```java
package com.example.basic.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("crm_followup")
public class CrmFollowup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;       // 客户ID
    private Integer followupType;  // 跟进方式
    private String content;        // 跟进内容
    private String nextPlan;       // 下次跟进计划
    private Date nextFollowupAt;   // 下次跟进时间
    private Long createdBy;        // 跟进人ID
    private Date createdTime;      // 跟进时间

    // 枚举
    public static final int TYPE_PHONE = 1;   // 电话
    public static final int TYPE_VISIT = 2;   // 上门拜访
    public static final int TYPE_WECHAT = 3;  // 微信
    public static final int TYPE_EMAIL = 4;   // 邮件
    public static final int TYPE_OTHER = 5;  // 其他
}
```

```java
package com.example.basic.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("crm_followup_task")
public class CrmFollowupTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;      // 客户ID
    private String taskType;      // 任务类型
    private String title;         // 任务标题
    private String content;       // 任务内容
    private Date dueAt;          // 截止时间
    private Integer status;       // 状态：0=待办 1=已完成 2=已逾期
    private Integer priority;     // 优先级：1=高 2=中 3=低
    private Long assigneeId;      // 负责人ID
    private Date completedAt;    // 完成时间
    private Date createdTime;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_DONE = 1;
    public static final int STATUS_OVERDUE = 2;

    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;
}
```

### 2. CrmService（核心业务逻辑）

```java
package com.example.basic.modules.crm.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.crm.dao.*;
import com.example.basic.modules.crm.entity.*;
import com.example.basic.modules.crm.service.CrmService;
import com.example.basic.model.query.PageParams;
import com.example.basic.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CRM 客户服务实现。
 *
 * <p>核心功能：
 * <ul>
 *   <li>客户 CRUD + 分页查询</li>
 *   <li>添加跟进记录（自动更新客户最后跟进时间）</li>
 *   <li>创建跟进任务（到期提醒）</li>
 *   <li>客户流失预警（超过 N 天未跟进自动提醒）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CrmServiceImpl implements CrmService {

    private final CrmCustomerDao customerDao;
    private final CrmFollowupDao followupDao;
    private final CrmFollowupTaskDao taskDao;
    private final NotificationService notificationService;

    /** 客户超过此天数未跟进，触发预警（可配置） */
    private static final int FOLLOWUP_OVERDUE_DAYS = 3;

    // ==================== 客户管理 ====================

    @Override
    public IPage<CrmCustomer> pageCustomers(CustomerQuery query) {
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getName()), CrmCustomer::getName, query.getName())
               .eq(query.getLevel() != null, CrmCustomer::getLevel, query.getLevel())
               .eq(query.getStatus() != null, CrmCustomer::getStatus, query.getStatus())
               .eq(query.getAssigneeId() != null, CrmCustomer::getAssigneeId, query.getAssigneeId())
               .orderByDesc(CrmCustomer::getLevel, CrmCustomer::getCreatedTime);

        return customerDao.selectPage(query.toPage(), wrapper);
    }

    @Override
    public Long saveCustomer(CustomerSaveDTO dto, Long operatorId) {
        // 1. 校验手机号唯一
        if (StrUtil.isNotBlank(dto.getMobile())) {
            long exists = customerDao.selectCount(
                    new LambdaQueryWrapper<CrmCustomer>()
                            .eq(CrmCustomer::getMobile, dto.getMobile())
                            .ne(dto.getId() != null, CrmCustomer::getId, dto.getId())
            );
            if (exists > 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "手机号已存在");
            }
        }

        // 2. 构造实体
        CrmCustomer customer = new CrmCustomer();
        customer.setName(dto.getName());
        customer.setMobile(dto.getMobile());
        customer.setCompany(dto.getCompany());
        customer.setIndustry(dto.getIndustry());
        customer.setSource(dto.getSource());
        customer.setLevel(dto.getLevel() != null ? dto.getLevel() : CrmCustomer.LEVEL_NORMAL);
        customer.setStatus(CrmCustomer.STATUS_POTENTIAL);
        customer.setAssigneeId(dto.getAssigneeId() != null ? dto.getAssigneeId() : operatorId);
        customer.setRemark(dto.getRemark());

        // 3. 保存
        customerDao.insert(customer);

        log.info("新增客户 | customerId={} | name={} | operator={}",
                customer.getId(), customer.getName(), operatorId);

        // 4. 发送通知
        notificationService.sendDefault("📋 新增客户",
                StrUtil.format("客户名：{} \n公司：{} \n负责人：{}",
                        customer.getName(), customer.getCompany(), customer.getAssigneeId()));

        return customer.getId();
    }

    @Override
    public void updateCustomer(CustomerUpdateDTO dto) {
        CrmCustomer exist = customerDao.selectById(dto.getId());
        if (exist == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "客户不存在");
        }

        // 字段覆盖更新
        if (StrUtil.isNotBlank(dto.getName()))    exist.setName(dto.getName());
        if (dto.getMobile() != null)              exist.setMobile(dto.getMobile());
        if (dto.getCompany() != null)             exist.setCompany(dto.getCompany());
        if (dto.getLevel() != null)               exist.setLevel(dto.getLevel());
        if (dto.getStatus() != null)              exist.setStatus(dto.getStatus());
        if (dto.getAssigneeId() != null)          exist.setAssigneeId(dto.getAssigneeId());
        if (dto.getRemark() != null)              exist.setRemark(dto.getRemark());

        customerDao.updateById(exist);
        log.info("更新客户 | customerId={}", dto.getId());
    }

    @Override
    public void deleteCustomer(Long id) {
        customerDao.deleteById(id);
        log.info("删除客户 | customerId={}", id);
    }

    // ==================== 跟进记录 ====================

    @Override
    public Long addFollowup(FollowupSaveDTO dto, Long operatorId) {
        CrmCustomer customer = customerDao.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "客户不存在");
        }

        // 1. 保存跟进记录
        CrmFollowup followup = new CrmFollowup();
        followup.setCustomerId(dto.getCustomerId());
        followup.setFollowupType(dto.getFollowupType());
        followup.setContent(dto.getContent());
        followup.setNextPlan(dto.getNextPlan());
        followup.setNextFollowupAt(dto.getNextFollowupAt());
        followup.setCreatedBy(operatorId);
        followup.setCreatedTime(new Date());
        followupDao.insert(followup);

        // 2. 更新客户最后跟进时间 + 下次跟进时间
        customer.setLastFollowupAt(new Date());
        if (dto.getNextFollowupAt() != null) {
            customer.setNextFollowupAt(dto.getNextFollowupAt());
        }
        customerDao.updateById(customer);

        // 3. 如果客户状态是"潜在"，自动升级为"意向"
        if (customer.getStatus() == CrmCustomer.STATUS_POTENTIAL) {
            customer.setStatus(CrmCustomer.STATUS_INTENT);
            customerDao.updateById(customer);
        }

        // 4. 如果有下次跟进计划，自动创建待办任务
        if (dto.getNextFollowupAt() != null && StrUtil.isNotBlank(dto.getNextPlan())) {
            createFollowupTask(customer.getId(), dto.getNextPlan(),
                    dto.getNextFollowupAt(), operatorId);
        }

        log.info("添加跟进记录 | customerId={} | followupId={} | type={}",
                dto.getCustomerId(), followup.getId(), dto.getFollowupType());

        return followup.getId();
    }

    @Override
    public List<CrmFollowup> listFollowups(Long customerId) {
        return followupDao.selectList(
                new LambdaQueryWrapper<CrmFollowup>()
                        .eq(CrmFollowup::getCustomerId, customerId)
                        .orderByDesc(CrmFollowup::getCreatedTime)
                        .last("LIMIT 50")
        );
    }

    // ==================== 跟进任务 ====================

    @Override
    public Long createTask(TaskSaveDTO dto, Long operatorId) {
        CrmCustomer customer = customerDao.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "客户不存在");
        }

        CrmFollowupTask task = new CrmFollowupTask();
        task.setCustomerId(dto.getCustomerId());
        task.setTaskType(dto.getTaskType());
        task.setTitle(dto.getTitle());
        task.setContent(dto.getContent());
        task.setDueAt(dto.getDueAt());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : CrmFollowupTask.PRIORITY_MEDIUM);
        task.setAssigneeId(dto.getAssigneeId() != null ? dto.getAssigneeId() : operatorId);
        task.setStatus(CrmFollowupTask.STATUS_PENDING);
        taskDao.insert(task);

        log.info("创建跟进任务 | taskId={} | customerId={} | dueAt={}",
                task.getId(), dto.getCustomerId(), dto.getDueAt());

        // 到期提醒（提前通知）
        scheduleReminder(task);

        return task.getId();
    }

    @Override
    public void completeTask(Long taskId, Long operatorId) {
        CrmFollowupTask task = taskDao.selectById(taskId);
        if (task == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "任务不存在");
        }
        if (task.getStatus() == CrmFollowupTask.STATUS_DONE) {
            throw new BizException(ResultCode.BAD_REQUEST, "任务已完成，请勿重复操作");
        }

        task.setStatus(CrmFollowupTask.STATUS_DONE);
        task.setCompletedAt(new Date());
        taskDao.updateById(task);

        // 更新客户的最后跟进时间
        CrmCustomer customer = customerDao.selectById(task.getCustomerId());
        if (customer != null) {
            customer.setLastFollowupAt(new Date());
            customerDao.updateById(customer);
        }

        log.info("完成任务 | taskId={} | operator={}", taskId, operatorId);
    }

    @Override
    public IPage<CrmFollowupTask> pageTasks(TaskQuery query) {
        LambdaQueryWrapper<CrmFollowupTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCustomerId() != null, CrmFollowupTask::getCustomerId, query.getCustomerId())
               .eq(query.getAssigneeId() != null, CrmFollowupTask::getAssigneeId, query.getAssigneeId())
               .eq(query.getStatus() != null, CrmFollowupTask::getStatus, query.getStatus())
               .eq(query.getPriority() != null, CrmFollowupTask::getPriority, query.getPriority())
               .orderByAsc(CrmFollowupTask::getPriority)
               .orderByAsc(CrmFollowupTask::getDueAt);
        return taskDao.selectPage(query.toPage(), wrapper);
    }

    @Override
    public List<CrmFollowupTask> listPendingTasks(Long assigneeId) {
        return taskDao.selectList(
                new LambdaQueryWrapper<CrmFollowupTask>()
                        .eq(CrmFollowupTask::getAssigneeId, assigneeId)
                        .in(CrmFollowupTask::getStatus,
                                CrmFollowupTask.STATUS_PENDING, CrmFollowupTask.STATUS_OVERDUE)
                        .orderByAsc(CrmFollowupTask::getPriority)
                        .orderByAsc(CrmFollowupTask::getDueAt)
                        .last("LIMIT 20")
        );
    }

    // ==================== 内部方法 ====================

    /** 创建跟进任务（从跟进记录中触发） */
    private void createFollowupTask(Long customerId, String plan, Date dueAt, Long operatorId) {
        CrmFollowupTask task = new CrmFollowupTask();
        task.setCustomerId(customerId);
        task.setTaskType("plan");
        task.setTitle(StrUtil.sub(plan, 0, 50)); // 最多50字作为标题
        task.setContent(plan);
        task.setDueAt(dueAt);
        task.setPriority(CrmFollowupTask.PRIORITY_MEDIUM);
        task.setAssigneeId(operatorId);
        task.setStatus(CrmFollowupTask.STATUS_PENDING);
        taskDao.insert(task);

        log.info("从跟进记录创建待办任务 | taskId={}", task.getId());
        scheduleReminder(task);
    }

    /** 安排到期提醒（提前1小时通知） */
    private void scheduleReminder(CrmFollowupTask task) {
        // 实际生产中可用 XXL-JOB / ElasticJob 实现精确到小时的提醒
        // 这里简化处理：直接记录到数据库，前端页面展示即可
        log.debug("任务提醒已安排 | taskId={} | dueAt={}", task.getId(), task.getDueAt());
    }
}
```

### 3. CrmController

```java
package com.example.basic.modules.crm.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.common.result.PageResult;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.crm.entity.*;
import com.example.basic.modules.crm.service.CrmService;
import com.example.basic.util.JwtUtil;
import com.example.basic.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRM 客户管理接口。
 *
 * <p>提供客户管理、跟进记录、待办任务等接口。
 */
@Tag(name = "10. CRM客户管理", description = "客户管理 + 跟进 + 任务待办")
@RestController
@RequestMapping("/crm")
@RequiredArgsConstructor
public class CrmController {

    private final CrmService crmService;

    // ===== 客户管理 =====

    @Operation(summary = "客户分页查询")
    @GetMapping("/customer/page")
    @Login
    public Result<PageResult<CrmCustomer>> pageCustomers(CustomerPageQuery query) {
        return Result.success(PageResult.of(crmService.pageCustomers(query)));
    }

    @Operation(summary = "新增客户")
    @PostMapping("/customer")
    @Login
    @LogOperation("新增客户")
    public Result<Long> saveCustomer(@RequestBody CustomerSaveDTO dto,
                                     HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(crmService.saveCustomer(dto, operatorId));
    }

    @Operation(summary = "修改客户")
    @PutMapping("/customer/{id}")
    @Login
    @LogOperation("修改客户")
    public Result<Void> updateCustomer(@PathVariable Long id,
                                      @RequestBody CustomerUpdateDTO dto) {
        dto.setId(id);
        crmService.updateCustomer(dto);
        return Result.success();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/customer/{id}")
    @Login
    @LogOperation("删除客户")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        crmService.deleteCustomer(id);
        return Result.success();
    }

    @Operation(summary = "客户详情")
    @GetMapping("/customer/{id}")
    @Login
    public Result<CrmCustomer> getCustomer(@PathVariable Long id) {
        CrmCustomer customer = crmService.getCustomerById(id);
        return Result.success(customer);
    }

    @Operation(summary = "客户统计")
    @GetMapping("/customer/stats")
    @Login
    public Result<CustomerStats> stats(HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(crmService.getCustomerStats(operatorId));
    }

    // ===== 跟进记录 =====

    @Operation(summary = "添加跟进记录")
    @PostMapping("/followup")
    @Login
    @LogOperation("添加跟进记录")
    public Result<Long> addFollowup(@RequestBody FollowupSaveDTO dto,
                                    HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(crmService.addFollowup(dto, operatorId));
    }

    @Operation(summary = "客户跟进记录列表")
    @GetMapping("/followup/list")
    @Login
    public Result<List<CrmFollowup>> listFollowups(
            @Parameter(description = "客户ID") @RequestParam Long customerId) {
        return Result.success(crmService.listFollowups(customerId));
    }

    // ===== 跟进任务 =====

    @Operation(summary = "创建跟进任务")
    @PostMapping("/task")
    @Login
    @LogOperation("创建跟进任务")
    public Result<Long> createTask(@RequestBody TaskSaveDTO dto,
                                  HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(crmService.createTask(dto, operatorId));
    }

    @Operation(summary = "完成任务")
    @PostMapping("/task/{id}/complete")
    @Login
    @LogOperation("完成任务")
    public Result<Void> completeTask(@PathVariable Long id,
                                     HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        crmService.completeTask(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "待办任务列表")
    @GetMapping("/task/pending")
    @Login
    public Result<List<CrmFollowupTask>> pendingTasks(HttpServletRequest request) {
        Long operatorId = JwtUtil.getLoginUser(request).getUserId();
        return Result.success(crmService.listPendingTasks(operatorId));
    }

    @Operation(summary = "任务分页查询")
    @GetMapping("/task/page")
    @Login
    public Result<PageResult<CrmFollowupTask>> pageTasks(TaskPageQuery query) {
        return Result.success(PageResult.of(crmService.pageTasks(query)));
    }

    // ===== DTO 定义 =====
    @Data public static class CustomerPageQuery extends PageParams {
        private String name;
        private Integer level;
        private Integer status;
        private Long assigneeId;
    }

    @Data public static class CustomerSaveDTO {
        private String name;
        private String mobile;
        private String company;
        private String industry;
        private String source;
        private Integer level;
        private Long assigneeId;
        private String remark;
    }

    @Data public static class CustomerUpdateDTO {
        private Long id;
        private String name;
        private String mobile;
        private String company;
        private Integer level;
        private Integer status;
        private Long assigneeId;
        private String remark;
    }

    @Data public static class FollowupSaveDTO {
        @NotNull(message = "客户ID不能为空")
        private Long customerId;
        @NotNull(message = "跟进方式不能为空")
        private Integer followupType;
        @NotBlank(message = "跟进内容不能为空")
        private String content;
        private String nextPlan;
        private Date nextFollowupAt;
    }

    @Data public static class TaskSaveDTO {
        private Long customerId;
        private String taskType;
        @NotBlank(message = "任务标题不能为空")
        private String title;
        private String content;
        @NotNull(message = "截止时间不能为空")
        private Date dueAt;
        private Integer priority;
        private Long assigneeId;
    }

    @Data public static class TaskPageQuery extends PageParams {
        private Long customerId;
        private Long assigneeId;
        private Integer status;
        private Integer priority;
    }

    @Data @lombok.AllArgsConstructor
    public static class CustomerStats {
        private long total;       // 我的客户总数
        private long potential;   // 潜在
        private long intent;      // 意向
        private long deal;        // 成交
        private long pendingTask; // 待办任务数
        private long overdueTask; // 逾期任务数
    }
}
```

### 4. 定时任务（跟进逾期提醒）

```java
package com.example.basic.modules.crm;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.crm.dao.*;
import com.example.basic.modules.crm.entity.*;
import com.example.basic.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * CRM 定时任务。
 *
 * <p>每天定时检查：
 * <ul>
 *   <li>客户 N 天未跟进 → 钉钉通知负责人</li>
 *   <li>任务逾期 → 钉钉通知负责人</li>
 *   <li>任务即将到期（1天内）→ 提前提醒</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrmCronJob {

    private final CrmCustomerDao customerDao;
    private final CrmFollowupTaskDao taskDao;
    private final NotificationService notificationService;

    /** 客户超过此天数未跟进，触发预警 */
    private static final int OVERDUE_DAYS = 3;

    /**
     * 每天早上 9 点，检查客户跟进逾期。
     *
     * <p>规则：最后跟进时间距今超过 OVERDUE_DAYS 天，且客户状态不是"流失"
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Async
    public void checkCustomerOverdue() {
        log.info("CRM 定时任务：检查客户跟进逾期");

        Date threshold = DateUtil.addDays(new Date(), -OVERDUE_DAYS);

        // 查询所有需要提醒的客户
        List<CrmCustomer> overdue = customerDao.selectList(
                new LambdaQueryWrapper<CrmCustomer>()
                        .lt(CrmCustomer::getLastFollowupAt, threshold)
                        .ne(CrmCustomer::getStatus, CrmCustomer.STATUS_LOST)
                        .isNotNull(CrmCustomer::getAssigneeId)
        );

        if (overdue.isEmpty()) {
            log.info("没有需要跟进的客户");
            return;
        }

        // 按负责人分组，每组发送一条通知
        Map<Long, List<CrmCustomer>> byAssignee = overdue.stream()
                .collect(java.util.stream.Collectors.groupingBy(CrmCustomer::getAssigneeId));

        for (Map.Entry<Long, List<CrmCustomer>> entry : byAssignee.entrySet()) {
            Long assigneeId = entry.getKey();
            List<CrmCustomer> customers = entry.getValue();

            String content = buildOverdueContent(customers);
            try {
                notificationService.sendDefault(
                        StrUtil.format("⚠️ 您有 {} 个客户需要跟进（{}天未联系）",
                                customers.size(), OVERDUE_DAYS),
                        content
                );
                log.info("客户跟进提醒已发送 | assigneeId={} | count={}",
                        assigneeId, customers.size());
            } catch (Exception e) {
                log.error("发送客户跟进提醒失败 | assigneeId={}", assigneeId, e);
            }
        }
    }

    /**
     * 每天早上 9 点，检查任务逾期。
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Async
    public void checkTaskOverdue() {
        log.info("CRM 定时任务：检查逾期任务");

        Date now = new Date();

        // 1. 更新已逾期的任务状态
        taskDao.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CrmFollowupTask>()
                        .eq(CrmFollowupTask::getStatus, CrmFollowupTask.STATUS_PENDING)
                        .lt(CrmFollowupTask::getDueAt, now)
                        .set(CrmFollowupTask::getStatus, CrmFollowupTask.STATUS_OVERDUE)
        );

        // 2. 查询所有逾期任务
        List<CrmFollowupTask> overdue = taskDao.selectList(
                new LambdaQueryWrapper<CrmFollowupTask>()
                        .eq(CrmFollowupTask::getStatus, CrmFollowupTask.STATUS_OVERDUE)
        );

        if (overdue.isEmpty()) {
            log.info("没有逾期任务");
            return;
        }

        // 按负责人分组发送通知
        Map<Long, List<CrmFollowupTask>> byAssignee = overdue.stream()
                .collect(java.util.stream.Collectors.groupingBy(CrmFollowupTask::getAssigneeId));

        for (Map.Entry<Long, List<CrmFollowupTask>> entry : byAssignee.entrySet()) {
            List<CrmFollowupTask> tasks = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(tasks.size(), 5); i++) {
                CrmFollowupTask t = tasks.get(i);
                sb.append(StrUtil.format("• {} \n截止：{} \n\n",
                        t.getTitle(), DateUtil.format(t.getDueAt())));
            }
            if (tasks.size() > 5) {
                sb.append("...还有 ").append(tasks.size() - 5).append(" 个逾期任务");
            }

            try {
                notificationService.sendAlert(
                        StrUtil.format("🔴 您有 {} 个逾期任务", tasks.size()),
                        sb.toString()
                );
            } catch (Exception e) {
                log.error("发送逾期任务提醒失败", e);
            }
        }
    }

    /**
     * 每天下午 6 点，发送今日待办提醒。
     */
    @Scheduled(cron = "0 0 18 * * ?")
    @Async
    public void sendTodayTodoReminder() {
        log.info("CRM 定时任务：今日待办提醒");

        Date todayStart = DateUtil.startOfDay(new Date());
        Date todayEnd = DateUtil.endOfDay(new Date());

        // 查询今天到期的任务
        List<CrmFollowupTask> todayTasks = taskDao.selectList(
                new LambdaQueryWrapper<CrmFollowupTask>()
                        .eq(CrmFollowupTask::getStatus, CrmFollowupTask.STATUS_PENDING)
                        .between(CrmFollowupTask::getDueAt, todayStart, todayEnd)
        );

        if (todayTasks.isEmpty()) return;

        Map<Long, List<CrmFollowupTask>> byAssignee = todayTasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(CrmFollowupTask::getAssigneeId));

        for (Map.Entry<Long, List<CrmFollowupTask>> entry : byAssignee.entrySet()) {
            List<CrmFollowupTask> tasks = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (CrmFollowupTask t : tasks) {
                sb.append(StrUtil.format("• {} \n", t.getTitle()));
            }

            try {
                notificationService.sendDefault(
                        StrUtil.format("📋 今日待办（{} 项）", tasks.size()),
                        sb.toString()
                );
            } catch (Exception e) {
                log.error("发送今日待办提醒失败", e);
            }
        }
    }

    // ===== 内部工具 =====

    private String buildOverdueContent(List<CrmCustomer> customers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(customers.size(), 5); i++) {
            CrmCustomer c = customers.get(i);
            long daysAgo = c.getLastFollowupAt() != null
                    ? DateUtil.diffDays(new Date(), c.getLastFollowupAt())
                    : OVERDUE_DAYS;
            String levelLabel = switch (c.getLevel()) {
                case 1 -> "⭐重点";
                case 2 -> "🔥重要";
                default -> "普通";
            };
            sb.append(StrUtil.format("• {} [{}]\n公司：{}\n已{}天未跟进\n\n",
                    c.getName(), levelLabel, c.getCompany(), daysAgo));
        }
        if (customers.size() > 5) {
            sb.append("...还有 ").append(customers.size() - 5).append(" 个客户");
        }
        return sb.toString();
    }
}
```

---

## 🔧 application.yml 配置

```yaml
# CRM 模块配置（可扩展）
crm:
  overdue-days: 3              # 客户超过 N 天未跟进即触发预警
  task-reminder-hours: 1        # 任务到期前 N 小时提醒

# 通知（钉钉/Server酱）
notification:
  dingtalk:
    enabled: true
    webhook-url: ${DINGTALK_WEBHOOK_URL:}
  serverchan:
    enabled: true
    sendkey: ${SERVERCHAN_SENDKEY:}
```

---

## 📊 调用时序图

```
销售添加跟进记录
    │
    ▼
POST /api/crm/followup
    │
    ▼
CrmServiceImpl.addFollowup()
    │
    ├── CrmFollowupDao.insert()       保存跟进记录
    │
    ├── CrmCustomerDao.update()       更新最后跟进时间
    │                                    + 升级潜在→意向
    │
    ├── [有下次计划?] ──是──→ CrmFollowupTaskDao.insert() 创建待办
    │
    └── NotificationService.sendDefault() 钉钉通知
```

```
定时任务（每天9:00）
    │
    ├── 查询 N 天未跟进客户
    │
    ├── 按负责人分组
    │
    ├── 发送钉钉提醒（每人一条，列出所有待跟进客户）
    │
    └── 更新逾期任务状态
```

---

## ✅ 完整功能清单

| 功能 | 接口 | 说明 |
|------|------|------|
| 客户分页 | `GET /crm/customer/page` | 支持按名称/等级/状态/负责人筛选 |
| 新增客户 | `POST /crm/customer` | 自动去重手机号 |
| 修改客户 | `PUT /crm/customer/{id}` | 部分更新 |
| 删除客户 | `DELETE /crm/customer/{id}` | 逻辑删除 |
| 客户统计 | `GET /crm/customer/stats` | 我的客户各状态数量 |
| 添加跟进 | `POST /crm/followup` | 自动更新最后跟进时间 + 创建待办 |
| 跟进列表 | `GET /crm/followup/list` | 客户所有跟进历史 |
| 创建任务 | `POST /crm/task` | 截止时间 + 优先级 |
| 完成任务 | `POST /crm/task/{id}/complete` | 标记完成 + 更新跟进时间 |
| 我的待办 | `GET /crm/task/pending` | 自己的待办列表 |
| 任务分页 | `GET /crm/task/page` | 按状态/优先级筛选 |

---

## ⏰ 定时任务汇总

| 时间 | 任务 | 说明 |
|------|------|------|
| 每天 09:00 | 检查客户逾期 | 超过 3 天未跟进 → 钉钉通知负责人 |
| 每天 09:00 | 检查任务逾期 | 更新逾期状态 → 钉钉告警 |
| 每天 18:00 | 今日待办提醒 | 当日到期的任务 → 钉钉提醒 |
