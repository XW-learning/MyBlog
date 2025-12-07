package com.personalblog.utils;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
     * ✅ 通用增删改方法
     */
    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            return ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 🔥 核心修复：增强版通用查询 - 增加类型自动转换
     */
    public static <T> List<T> executeQueryList(Class<T> clazz, String sql, Object... params) {
        List<T> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object columnValue = rs.getObject(i);

                        // 转换列名: user_id -> userId
                        String propertyName = convertColumnToProperty(columnName);
                        // 拼接Setter: setUserId
                        String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

                        try {
                            // 查找 Setter
                            Method setter = findSetterMethod(clazz, setterName);
                            if (setter != null && columnValue != null) {
                                // 🔥 核心修复点：获取 Setter 的参数类型，并进行手动类型转换
                                Class<?> paramType = setter.getParameterTypes()[0];
                                Object convertedValue = convertValue(columnValue, paramType);

                                setter.invoke(entity, convertedValue);
                            }
                        } catch (Exception e) {
                            // 忽略找不到 Setter 或类型转换失败的错误，保证其他字段能正常赋值
                            // e.printStackTrace();
                        }
                    }
                    list.add(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 通用查询 - 返回单个对象
     */
    public static <T> T executeQuerySingle(Class<T> clazz, String sql, Object... params) {
        List<T> list = executeQueryList(clazz, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    // --- 内部辅助方法 ---

    /**
     * 🔥 核心修复：类型转换器
     * 解决 Integer vs Long, Timestamp vs Date, Boolean vs Integer 等反射不兼容问题
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        // 1. 处理数字类型转换 (Long <-> Integer <-> BigDecimal)
        if (value instanceof Number) {
            Number number = (Number) value;
            if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            } else if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            }
        }

        // 2. 处理 String 转换
        if (targetType == String.class) {
            return value.toString();
        }

        // 3. 处理日期类型 (Timestamp -> Date)
        // java.sql.Timestamp 是 java.util.Date 的子类，通常可以直接赋值。
        // 但如果驱动返回 LocalDateTime，这里可能需要额外处理（目前 MySQL 8 驱动默认返回 Timestamp 应该没问题）

        return value; // 默认直接返回，依靠 Java 多态
    }

    private static String convertColumnToProperty(String columnName) {
        StringBuilder sb = new StringBuilder();
        boolean toUpperCase = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                toUpperCase = true;
            } else if (toUpperCase) {
                sb.append(Character.toUpperCase(c));
                toUpperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Method findSetterMethod(Class<?> clazz, String setterName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equalsIgnoreCase(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}