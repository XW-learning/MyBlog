package com.personalblog.service.impl;

import com.personalblog.mapper.UserMapper;
import com.personalblog.mapper.impl.UserMapperImpl;
import com.personalblog.model.User;
import com.personalblog.service.UserService;

import java.util.Date;


public class UserServiceImpl implements UserService {
    private final UserMapper userMapper = new UserMapperImpl();

    /**
     * 用户登录
     *
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

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册成功返回 true，失败（如用户名已存在）返回 false
     */
    @Override
    public boolean register(User user) {
        // 1. 业务校验：用户名是否已存在
        if (userMapper.checkUsername(user.getUsername())) {
            System.out.println("注册失败：用户名 [" + user.getUsername() + "] 已存在");
            return false;
        }

        // 2. 补全信息
        user.setCreateTime(new Date());
        // 默认头像设为 null，让数据库使用默认值或前端使用占位符
        if (user.getAvatar() == null) {
            user.setAvatar(null);
        }

        // 3. 调用 Mapper 保存
        return userMapper.save(user);
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 用户名已存在返回 true，不存在返回 false
     */
    @Override
    public boolean isUsernameExist(String username) {
        // 简单转发到 Mapper
        return userMapper.checkUsername(username);
    }


}