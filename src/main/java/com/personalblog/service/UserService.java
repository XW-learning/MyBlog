package com.personalblog.service;

import com.personalblog.model.User;

public interface UserService {
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回 null
     */
    User login(String username, String password);

    /**
     * 用户注册 <-- 新增
     *
     * @param user 用户信息
     * @return 注册成功返回 true，失败（如用户名已存在）返回 false
     */
    boolean register(User user);

    /**
     * 检查用户名是否存在 <-- 新增 (辅助方法，也可以直接在 register 里用)
     */
    boolean isUsernameExist(String username);
}