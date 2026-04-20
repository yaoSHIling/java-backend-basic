package com.example.basic.modules.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.workflow.entity.WfInstanceLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfInstanceLogDao extends BaseMapper<WfInstanceLog> {
}