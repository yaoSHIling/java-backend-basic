package com.example.basic.modules.config.service;

import com.example.basic.modules.config.entity.SysConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口。
 *
 * @author hermes-agent
 */
public interface SysConfigService {

    /** 根据 key 获取单个配置值（字符串） */
    String getValue(String configKey);

    /** 根据 key 获取配置实体 */
    SysConfig getByKey(String configKey);

    /** 根据分组获取配置列表 */
    List<SysConfig> getByGroup(String groupName);

    /** 获取所有配置（Map形式） */
    Map<String, String> getAllConfig();

    /** 保存或更新配置 */
    void saveOrUpdate(SysConfig config);

    /** 删除配置 */
    void delete(Long id);
}
