package com.personalblog.service.impl;

import com.personalblog.mapper.UserMapper;
import com.personalblog.mapper.impl.UserMapperImpl;
import com.personalblog.model.User;
import com.personalblog.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserMapper userMapper = new UserMapperImpl();

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        user.setPassword(null);
        return user;
    }
}