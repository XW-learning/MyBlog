package com.personalblog.mapper.impl;

import com.personalblog.mapper.UserMapper;
import com.personalblog.model.User;
import com.personalblog.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 用户数据访问实现类
 */
public class UserMapperImpl implements UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public User findByUsername(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;
        try {
            conn = JDBCUtils.getConnection();
            String sql = "SELECT * FROM t_user WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setAvatar(rs.getString("avatar"));
                user.setCreateTime(rs.getTimestamp("create_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return user;
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
            int rows = JDBCUtils.executeUpdate(sql, user.getUsername(), user.getPassword(), user.getNickname(),
                    user.getCreateTime());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}