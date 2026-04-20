package com.example.basic.modules.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.dict.entity.DictData;

import java.util.List;

/**
 * 数据字典服务接口。
 *
 * @author hermes-agent
 */
public interface DictService {

    /** 根据类型获取字典项（供前端下拉使用） */
    List<DictData> getByType(String dictType);

    /** 分页查询字典数据 */
    IPage<DictData> page(PageParams pageParams, String dictType, String dictLabel, Integer status);

    /** 新增字典项 */
    void save(DictData dictData);

    /** 更新字典项 */
    void update(DictData dictData);

    /** 删除字典项 */
    void delete(Long id);
}
