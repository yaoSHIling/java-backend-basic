package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WfDefinitionDao extends BaseMapper<WfDefinition> {
}

@Mapper
public interface WfInstanceDao extends BaseMapper<WfInstance> {
}

@Mapper
public interface WfTaskDao extends BaseMapper<WfTask> {
}

@Mapper
public interface WfTaskHistoryDao extends BaseMapper<WfTaskHistory> {
}
