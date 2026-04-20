package com.example.basic.modules.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.log.entity.OperationLog;

public interface OperationLogService {

    /** 保存日志（异步） */
    void saveAsync(OperationLog logEntry);

    /** 分页查询日志 */
    IPage<OperationLog> page(PageParams pageParams, String username, String operation, String ip);
}
