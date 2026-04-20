package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.WfTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfTaskDao extends BaseMapper<WfTask> {
}
