package com.example.basic.modules.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动获得：
 * <ul>
 *   <li>CRUD 基本操作（insert / deleteById / updateById / selectById 等）</li>
 *   <li>分页查询（需配合 MybatisPlusConfig 中的分页插件）</li>
 *   <li>条件构造器（QueryWrapper / LambdaQueryWrapper）</li>
 * </ul>
 *
 * <p>MyBatis-Plus 会根据实体类注解自动映射 SQL，无需手写 XML。
 *
 * @author hermes-agent
 */
@Mapper
public interface UserDao extends BaseMapper<User> {
    // 继承 BaseMapper 后已拥有全部基础 CRUD 方法
    // 如需自定义 SQL，在 src/main/resources/mapper/UserDao.xml 中编写
}
