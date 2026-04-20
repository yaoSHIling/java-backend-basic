package com.example.basic.modules.log.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.basic.modules.log.dao.OperationLogDao;
import com.example.basic.modules.log.entity.OperationLog;
import com.example.basic.modules.log.service.OperationLogService;
import com.example.basic.model.query.PageParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogDao operationLogDao;

    /**
     * 异步保存日志。
     *
     * <p>使用 @Async 注解在线程池中执行，不阻塞主业务。
     * 注意：@Async 生效需要方法调用方在 Spring 容器管理下，
     * 切面内部调用需要通过代理对象，这里做简单同步保险处理。
     */
    @Override
    public void saveAsync(OperationLog logEntry) {
        try {
            // 简单处理：直接同步写入（异步优化留给后续迭代）
            operationLogDao.insert(logEntry);
        } catch (Exception e) {
            log.warn("操作日志写入失败（不影响主业务）: {}", e.getMessage());
        }
    }

    @Override
    public IPage<OperationLog> page(PageParams pageParams, String username, String operation, String ip) {
        Page<OperationLog> page = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StrUtil.isNotBlank(operation)) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        if (StrUtil.isNotBlank(ip)) {
            wrapper.eq(OperationLog::getIp, ip);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return operationLogDao.selectPage(page, wrapper);
    }
}
