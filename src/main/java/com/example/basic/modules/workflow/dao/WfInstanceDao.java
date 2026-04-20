package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.WfInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfInstanceDao extends BaseMapper<WfInstance> {
}