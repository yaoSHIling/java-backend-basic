package com.example.basic.modules.log.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper。
 *
 * @author hermes-agent
 */
@Mapper
public interface OperationLogDao extends BaseMapper<OperationLog> {
}
