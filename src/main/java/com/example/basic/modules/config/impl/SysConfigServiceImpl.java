package com.example.basic.modules.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
    import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
    import com.example.basic.common.result.ResultCode;
    import com.example.basic.modules.config.dao.SysConfigDao;
    import com.example.basic.modules.config.entity.SysConfig;
    import com.example.basic.modules.config.service.SysConfigService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class SysConfigServiceImpl implements SysConfigService {

        private final SysConfigDao sysConfigDao;

        @Override
        public String getValue(String configKey) {
            SysConfig config = sysConfigDao.selectByKey(configKey);
            return config != null ? config.getConfigValue() : null;
        }

        @Override
        public SysConfig getByKey(String configKey) {
            return sysConfigDao.selectByKey(configKey);
        }

        @Override
        public List<SysConfig> getByGroup(String groupName) {
            return sysConfigDao.selectByGroup(groupName);
        }

        @Override
        public Map<String, String> getAllConfig() {
            Map<String, String> result = new HashMap<>();
            sysConfigDao.selectList(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getDeleted, 0)
                            .eq(SysConfig::getStatus, 1)
            ).forEach(c -> result.put(c.getConfigKey(), c.getConfigValue()));
            return result;
        }

        @Override
        @Transactional
        public void saveOrUpdate(SysConfig config) {
            SysConfig existing = sysConfigDao.selectByKey(config.getConfigKey());
            config.setUpdateTime(LocalDateTime.now());
            if (existing != null) {
                // 系统内置配置只允许更新值，不允许改key
                if (existing.getReadonly() != null && existing.getReadonly() == 1) {
                    if (!existing.getConfigKey().equals(config.getConfigKey())
                            || !existing.getGroupName().equals(config.getGroupName())) {
                        throw new BizException(ResultCode.FORBIDDEN, "系统内置配置不可修改");
                    }
                }
                config.setId(existing.getId());
                config.setCreateTime(existing.getCreateTime());
                sysConfigDao.updateById(config);
            } else {
                config.setCreateTime(LocalDateTime.now());
                sysConfigDao.insert(config);
            }
            log.info("保存配置 | key={} | value={}", config.getConfigKey(), config.getConfigValue());
        }

        @Override
        @Transactional
        public void delete(Long id) {
            SysConfig config = sysConfigDao.selectById(id);
            if (config != null && config.getReadonly() != null && config.getReadonly() == 1) {
                throw new BizException(ResultCode.FORBIDDEN, "系统内置配置不可删除");
            }
            sysConfigDao.deleteById(id);
        }
    }
