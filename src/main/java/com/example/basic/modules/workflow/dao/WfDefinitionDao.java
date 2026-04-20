package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.WfDefinition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfDefinitionDao extends BaseMapper<WfDefinition> {
}