package com.example.basic.modules.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.config.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置 Mapper。
 *
 * @author hermes-agent
 */
@Mapper
public interface SysConfigDao extends BaseMapper<SysConfig> {

    /** 根据 key 查询配置 */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0 AND status = 1")
    SysConfig selectByKey(@Param("configKey") String configKey);

    /** 根据分组查询配置列表 */
    @Select("SELECT * FROM sys_config WHERE group_name = #{groupName} AND deleted = 0 AND status = 1 ORDER BY sort")
    List<SysConfig> selectByGroup(@Param("groupName") String groupName);
}
