package com.example.basic.modules.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.workflow.controller.WorkflowController.*;
import com.example.basic.modules.workflow.entity.*;

import java.util.List;

public interface WorkflowService {

    // ===== 定义管理 =====
    IPage<WfDefinition> pageDefinitions(WfDefinition query, PageParams pageParams);
    WfDefinition getDefinitionById(Long id);
    Long saveDefinition(WfDefinitionSaveDTO dto, Long operatorId);
    void publishDefinition(Long id);
    void disableDefinition(Long id);

    // ===== 流程操作 =====
    Long submit(WfSubmitDTO dto, Long initiatorId);
    void approve(WfApproveDTO dto, Long operatorId);
    void revoke(WfRevokeDTO dto, Long operatorId);

    // ===== 查询 =====
    IPage<WfInstance> pageInstances(WfInstanceQuery query, PageParams pageParams);
    IPage<WfTask> pageMyTasks(Long assigneeId, PageParams pageParams);
    WfInstance getInstanceById(Long id);
    List<WfTaskHistory> getInstanceHistory(Long instanceId);
}
