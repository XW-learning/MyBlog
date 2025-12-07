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
            // <-- 修改在这里：增加了 email 字段
            String sql = "INSERT INTO t_user(username, password, nickname, email, avatar, create_time) VALUES(?, ?, ?, ?, ?, ?)";

            // 注意参数顺序要和 SQL 对应
            int rows = JDBCUtils.executeUpdate(sql,
                    user.getUsername(),
                    user.getPassword(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getCreateTime()
            );
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    public boolean checkUsername(String username) {
        String sql = "SELECT COUNT(*) FROM t_user WHERE username = ?";
        Long count = JDBCUtils.executeQuerySingle(Long.class, sql, username);
        // 如果查询结果大于 0，则说明存在
        return count != null && count > 0;
    }
}