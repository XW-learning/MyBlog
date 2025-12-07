package com.personalblog.mapper;

import com.personalblog.model.User;

/**
 * 用户数据访问接口
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByUsername(String username);

    /**
     * 保存用户
     *
     * @param user 用户
     * @return 保存成功返回 true
     */
    boolean save(User user);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回 true
     */
    boolean checkUsername(String username);
}