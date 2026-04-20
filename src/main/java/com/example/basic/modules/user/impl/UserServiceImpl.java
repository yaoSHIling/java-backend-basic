package com.example.basic.modules.user.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.user.dao.UserDao;
import com.example.basic.modules.user.entity.User;
import com.example.basic.modules.user.service.UserService;
import com.example.basic.model.query.PageParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理服务实现。
 *
 * <p>所有写操作开启事务保证一致性。
 *
 * @author hermes-agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public User getById(Long id) {
        User user = userDao.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return userDao.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)  // 逻辑删除过滤（MyBatis-Plus 全局已配置，此处保险加一层）
        );
    }

    @Override
    public void save(User user) {
        userDao.insert(user);
    }

    @Override
    public void update(User user) {
        if (user.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
        // 清除敏感字段：不允许通过 update 接口修改密码
        user.setPassword(null);
        user.setDeleted(null);  // 不允许手动修改删除标记
        userDao.updateById(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 逻辑删除（deleted=1），MyBatis-Plus 自动处理
        userDao.deleteById(id);
        log.info("删除用户 | userId={}", id);
    }

    @Override
    public IPage<User> page(PageParams pageParams, String username, Integer status) {
        Page<User> page = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 用户名模糊搜索
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(User::getUsername, username);
        }
        // 状态精确筛选
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        // 按创建时间倒序（最新的在前）
        wrapper.orderByDesc(User::getCreateTime);

        return userDao.selectPage(page, wrapper);
    }

    @Override
    public List<User> list() {
        return userDao.selectList(
                new LambdaQueryWrapper<User>()
                        .orderByDesc(User::getCreateTime)
        );
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = User.builder().id(id).status(status).build();
        userDao.updateById(user);
        log.info("更新用户状态 | userId={} | status={}", id, status);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new BizException(ResultCode.BAD_REQUEST, "密码长度至少6位");
        }
        User user = User.builder()
                .id(id)
                .password(cn.hutool.crypto.SecureUtil.md5(newPassword))
                .build();
        userDao.updateById(user);
        log.info("用户修改密码 | userId={}", id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (StrUtil.isBlank(newPassword)) {
            newPassword = "123456"; // 默认重置密码
        }
        User user = User.builder()
                .id(id)
                .password(cn.hutool.crypto.SecureUtil.md5(newPassword))
                .build();
        userDao.updateById(user);
        log.info("管理员重置用户密码 | userId={} | newPwd={}", id, newPassword);
    }
}
