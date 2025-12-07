package com.personalblog.service.impl;

import com.personalblog.mapper.UserMapper;
import com.personalblog.mapper.impl.UserMapperImpl;
import com.personalblog.model.User;
import com.personalblog.service.UserService;
import com.personalblog.utils.PasswordUtils;

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
        // 1. 先根据用户名查出用户对象（包含加密后的密码）
        User user = userMapper.findByUsername(username);
        // 2. 如果用户不存在
        if (user == null) {
            return null;
        }
        // 3. 🔥 核心修改：使用 BCrypt 校验密码
        // user.getPassword() 是数据库里的密文，password 是用户输入的明文
        boolean isMatched = PasswordUtils.check(password, user.getPassword());
        if (!isMatched) {
            return null; // 密码错误
        }
        // 4. 登录成功，出于安全考虑，把内存中的密码擦除再返回
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

        // 2. 🔥 核心修改：对密码进行加密
        String rawPassword = user.getPassword();
        String hashedPassword = PasswordUtils.hash(rawPassword);
        user.setPassword(hashedPassword); // 替换为密文

        // 3. 补全信息
        user.setCreateTime(new Date());
//        if (user.getAvatar() == null) {
//            // 随便给个默认头像
//            user.setAvatar("https://cdn.icon-icons.com/icons2/1378/PNG/512/avatardefault_92824.png");
//        }

        // 4. 调用 Mapper 保存
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