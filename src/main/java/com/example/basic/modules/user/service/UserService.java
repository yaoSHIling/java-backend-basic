package com.example.basic.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.user.entity.User;

import java.util.List;

/**
 * 用户管理服务接口。
 *
 * @author hermes-agent
 */
public interface UserService {

    // ========== CRUD 基本操作 ==========

    User getById(Long id);

    User getByUsername(String username);

    void save(User user);

    void update(User user);

    void delete(Long id);

    // ========== 分页查询 ==========

    /**
     * 分页查询用户列表。
     *
     * @param pageParams 分页参数（pageNum=1, pageSize=10）
     * @param username  可选：用户名（模糊匹配）
     * @param status     可选：状态筛选
     * @return 分页结果（包含 total 总数 + records 当前页数据）
     */
    IPage<User> page(PageParams pageParams, String username, Integer status);

    // ========== 批量操作 ==========

    List<User> list();

    void updateStatus(Long id, Integer status);

    void updatePassword(Long id, String newPassword);

    void resetPassword(Long id, String newPassword);
}
