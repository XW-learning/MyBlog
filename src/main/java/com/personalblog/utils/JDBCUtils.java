package com.personalblog.utils;

import java.sql.*;

public class JDBCUtils {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    // ⚠️ 请确认数据库名是 blog_system
    private static final String URL = "jdbc:mysql://localhost:3306/blog_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取新连接 (每次调用都返回新的，确保线程安全)
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * ✅ 您喜欢的通用增删改方法 (自动处理连接关闭)
     * 适用于：INSERT, UPDATE, DELETE
     */
    public static int executeUpdate(String sql, Object... params) {
        // 使用 try-with-resources 自动关闭连接和语句，无需手动 close
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 填充参数
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            return ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0; // 失败返回0
        }
    }

    /**
     * ⚠️ 特殊说明：通用查询方法
     * 因为 ResultSet 依赖 Connection，所以这里不能在方法内关闭连接。
     * 调用者（Mapper）必须在使用完 ResultSet 后，手动调用 JDBCUtils.close(...)
     */
    public static ResultSet executeQuery(Connection conn, PreparedStatement ps, String sql, Object... params) throws SQLException {
        // 注意：这里不创建连接，而是由外部传入连接对象，方便外部控制关闭时机
        ps = conn.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
        return ps.executeQuery();
    }

    /**
     * 统一资源关闭方法
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}