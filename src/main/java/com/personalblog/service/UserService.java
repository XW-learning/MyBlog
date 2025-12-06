package com.personalblog.service;

import com.personalblog.model.User;

public interface UserService {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回 null
     */
    User login(String username, String password);
}