package com.example.basic.modules.workflow.engine;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.basic.modules.workflow.dao.WfInstanceDao;
import com.example.basic.modules.workflow.dao.WfInstanceLogDao;
import com.example.basic.modules.workflow.engine.WfGraph.*;
import com.example.basic.modules.workflow.engine.WfNodeResult;
import com.example.basic.modules.workflow.entity.WfInstance;
import com.example.basic.modules.workflow.entity.WfInstanceLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 工作流执行引擎（参考 Coze 图执行模型）
 *
 * <p>核心流程：
 * <pre>
 * 1. 根据 definitionId 加载 WfGraph（nodes + edges）
 * 2. 找到 start 节点，从 start 开始执行
 * 3. 每个节点调用对应的 NodeExecutor.execute()
 * 4. 根据返回值中的 nextNodeId 决定下一个节点
 * 5. 审批节点特殊处理：创建任务后暂停，等待人工回调
 * 6. end 节点：流程结束，记录输出
 * 7. 全程记录执行日志（WfInstanceLog）
 * </pre>
 *
 * <p>回调触发（审批完成后）：
 * <pre>
 * /workflow/callback/approve → resumeExecution(instanceId, nodeId, approved, opinion)
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final Map<String, NodeExecutor> executorMap = new HashMap<>();
    private final WfInstanceDao instanceDao;
    private final WfInstanceLogDao logDao;

    // 注入所有节点执行器
    public WorkflowEngine(
            WfInstanceDao instanceDao,
            WfInstanceLogDao logDao,
            List<NodeExecutor> executors
    ) {
        this.instanceDao = instanceDao;
        this.logDao = logDao;
        for (NodeExecutor e : executors) {
            executorMap.put(e.nodeType(), e);
            log.info("注册节点执行器: type={}", e.nodeType());
        }
    }

    /**
     * 启动工作流（同步执行，简单流程）
     */
    @Transactional
    public Long startSync(WfGraph graph, Map<String, Object> inputData, Long initiatorId) {
        // 1. 创建实例
        WfInstance instance = new WfInstance();
        instance.setDefinitionId(0L); // TODO: 关联 definition
        instance.setGraphData(JSONUtil.toJson(graph));
        instance.setStatus(WfInstance.STATUS_RUNNING);
        instance.setInputData(JSONUtil.toJson(inputData));
        instance.setInitiatorId(initiatorId);
        instance.setStartedAt(new Date());
        instanceDao.insert(instance);

        // 2. 构建上下文
        ExecutionContext ctx = new ExecutionContext();
        ctx.setGraph(graph);
        ctx.setInstanceId(instance.getId());
        ctx.setInitiatorId(initiatorId);
        ctx.setVariables(new HashMap<>(inputData));

        // 3. 注入审批任务创建回调
        ctx.setApprovalTaskCallback((nodeId, nodeName, assigneeType, assigneeExpr, title, content) -> {
            // TODO: 创建审批任务 WfTask，入库，返回 taskId
            return 0L;
        });

        // 4. 执行
        try {
            executeGraph(instance, graph, ctx, inputData);
            instance.setStatus(WfInstance.STATUS_SUCCESS);
            instance.setOutputData(JSONUtil.toJson(ctx.getFinalOutput()));
        } catch (Exception e) {
            instance.setStatus(WfInstance.STATUS_FAILED);
            instance.setErrorMsg(e.getMessage());
            log.error("工作流执行失败 | instanceId={}", instance.getId(), e);
        }

        instance.setFinishedAt(new Date());
        instanceDao.updateById(instance);
        return instance.getId();
    }

    /**
     * 异步执行（适用于有审批节点的复杂流程）
     */
    @Async
    public void startAsync(Long instanceId, WfGraph graph, Map<String, Object> inputData, ExecutionContext ctx) {
        try {
            executeGraph(instanceDao.selectById(instanceId), graph, ctx, inputData);
        } catch (Exception e) {
            log.error("异步工作流执行失败 | instanceId={}", instanceId, e);
            WfInstance inst = new WfInstance();
            inst.setId(instanceId);
            inst.setStatus(WfInstance.STATUS_FAILED);
            inst.setErrorMsg(e.getMessage());
            inst.setFinishedAt(new Date());
            instanceDao.updateById(inst);
        }
    }

    /**
     * 图执行主循环
     */
    private void executeGraph(WfInstance instance, WfGraph graph, ExecutionContext ctx, Map<String, Object> inputData) throws Exception {
        // 找到 start 节点
        Node startNode = graph.getNodes().stream()
            .filter(n -> "start".equals(n.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("工作流缺少 start 节点"));

        String currentNodeId = startNode.getId();
        Map<String, Object> currentData = inputData != null ? new HashMap<>(inputData) : new HashMap<>();

        // 主循环：最多 1000 个节点，防止死循环
        int loopCount = 0;
        while (currentNodeId != null && loopCount++ < 1000) {
            Node node = graph.getNodes().stream()
                .filter(n -> n.getId().equals(currentNodeId))
                .findFirst()
                .orElse(null);

            if (node == null) break;

            // 找到出边中的下一跳（优先用节点返回值里的 nextNodeId）
            String nextFromResult = ctx.getCurrentNodeId(); // 从上下文取（condition 等节点会设置）
            if (nextFromResult != null) {
                currentNodeId = nextFromResult;
                ctx.setCurrentNodeId(null);
                continue;
            }

            // 记录当前节点
            instance.setCurrentNodeId(currentNodeId);
            instanceDao.updateById(instance);

            // 找到执行器
            NodeExecutor executor = executorMap.get(node.getType());
            if (executor == null) {
                throw new IllegalStateException("未找到节点执行器: " + node.getType());
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            WfInstanceLog log = new WfInstanceLog();
            log.setInstanceId(instance.getId());
            log.setNodeId(node.getId());
            log.setNodeName(node.getData() != null ? node.getData().getName() : node.getId());
            log.setNodeType(node.getType());
            log.setStatus(WfInstanceLog.STATUS_RUNNING);
            log.setInputData(JSONUtil.toJson(currentData));
            log.setStartedAt(new Date());
            logDao.insert(log);

            // 执行节点
            WfNodeResult result = executor.execute(node, currentData, ctx);

            long elapsed = System.currentTimeMillis() - startTime;
            log.setElapsedMs((int) elapsed);
            log.setOutputData(JSONUtil.toJson(result.getOutputData()));
            log.setFinishedAt(new Date());

            if ("failure".equals(result.getStatus())) {
                log.setStatus(WfInstanceLog.STATUS_FAILED);
                log.setErrorMsg(result.getErrorMsg());
                logDao.updateById(log);
                throw new RuntimeException("节点执行失败: " + result.getErrorMsg());
            }

            log.setStatus(WfInstanceLog.STATUS_SUCCESS);
            logDao.updateById(log);

            // 更新当前数据
            if (result.getOutputData() != null) {
                currentData.putAll(result.getOutputData());
            }

            // 特殊处理：end 节点
            if ("end".equals(node.getType())) {
                ctx.setFinalOutput(currentData);
                break;
            }

            // 特殊处理：审批节点（waiting 状态，暂停）
            if ("waiting".equals(result.getStatus())) {
                instance.setStatus(WfInstance.STATUS_PAUSED);
                instance.setCurrentNodeId(currentNodeId);
                instanceDao.updateById(instance);
                log.info("工作流暂停，等待审批回调 | instanceId={} | nodeId={}", instance.getId(), currentNodeId);
                return; // 退出，等待回调
            }

            // 推进到下一个节点
            currentNodeId = result.getNextNodeId();
            if (currentNodeId == null) {
                currentNodeId = ctx.findNextNodeId(node.getId());
            }
        }

        instance.setStatus(WfInstance.STATUS_SUCCESS);
        instance.setFinalOutput(JSONUtil.toJson(ctx.getFinalOutput()));
        instance.setFinishedAt(new Date());
        instanceDao.updateById(instance);
    }

    /**
     * 审批回调：审批通过/拒绝后，驱动流程继续执行
     */
    @Transactional
    public void resumeFromApproval(Long instanceId, Long taskId, boolean approved, String opinion) {
        WfInstance instance = instanceDao.selectById(instanceId);
        if (instance == null) throw new IllegalArgumentException("实例不存在: " + instanceId);

        WfGraph graph = JSONUtil.toJson(instance.getGraphData(), WfGraph.class);
        ExecutionContext ctx = new ExecutionContext();
        ctx.setGraph(graph);
        ctx.setInstanceId(instanceId);
        ctx.setInitiatorId(instance.getInitiatorId());
        ctx.setVariables(new HashMap<>());

        // 审批通过/拒绝逻辑
        // TODO: 记录审批意见到日志

        // 找到当前节点（审批节点），继续执行下一个节点
        // ...
    }
}
