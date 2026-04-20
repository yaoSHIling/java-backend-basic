package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.*;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfDefinitionDao extends BaseMapper<WfDefinition> {}
@Mapper
public interface WfInstanceDao extends BaseMapper<WfInstance> {}
@Mapper
public interface WfInstanceLogDao extends BaseMapper<WfInstanceLog> {}
