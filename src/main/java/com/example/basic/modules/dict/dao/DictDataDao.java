package com.example.basic.modules.dict.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.dict.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据字典 Mapper。
 *
 * @author hermes-agent
 */
@Mapper
public interface DictDataDao extends BaseMapper<DictData> {

    /**
     * 根据字典类型查询所有启用的选项。
     *
     * @param dictType 字典类型
     * @return 字典项列表（按 sort 排序）
     */
    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND deleted = 0 AND status = 1 ORDER BY sort ASC")
    List<DictData> selectByType(@Param("dictType") String dictType);
}
