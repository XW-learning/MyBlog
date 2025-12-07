package com.personalblog.mapper.impl;

import com.personalblog.mapper.UserMapper;
import com.personalblog.model.User;
import com.personalblog.utils.JDBCUtils;

public class UserMapperImpl implements UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username = ?";
        return JDBCUtils.executeQuerySingle(User.class, sql, username);
    }

    /**
     * 保存用户
     *
     * @param user 用户对象
     * @return 是否保存成功
     */
    @Override
    public boolean save(User user) {
        try {
            String sql = "INSERT INTO t_user(username, password, nickname, create_time) VALUES(?, ?, ?, ?)";
            int rows = JDBCUtils.executeUpdate(sql, user.getUsername(), user.getPassword(), user.getNickname(), user.getCreateTime());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}