package com.example.basic.modules.dict.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.dict.dao.DictDataDao;
import com.example.basic.modules.dict.entity.DictData;
import com.example.basic.modules.dict.service.DictService;
import com.example.basic.model.query.PageParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictDataDao dictDataDao;

    @Override
    public List<DictData> getByType(String dictType) {
        // 先从缓存取（省略 Redis 缓存实现，保持代码简洁）
        return dictDataDao.selectByType(dictType);
    }

    @Override
    public IPage<DictData> page(PageParams pageParams, String dictType, String dictLabel, Integer status) {
        Page<DictData> page = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(dictType)) {
            wrapper.eq(DictData::getDictType, dictType);
        }
        if (StrUtil.isNotBlank(dictLabel)) {
            wrapper.like(DictData::getDictLabel, dictLabel);
        }
        if (status != null) {
            wrapper.eq(DictData::getStatus, status);
        }
        wrapper.orderByAsc(DictData::getDictType, DictData::getSort);
        return dictDataDao.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void save(DictData dictData) {
        dictData.setCreateTime(LocalDateTime.now());
        dictData.setUpdateTime(LocalDateTime.now());
        dictDataDao.insert(dictData);
    }

    @Override
    @Transactional
    public void update(DictData dictData) {
        if (dictData.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "ID不能为空");
        }
        dictData.setUpdateTime(LocalDateTime.now());
        dictDataDao.updateById(dictData);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        dictDataDao.deleteById(id);
    }
}
