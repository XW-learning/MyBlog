package com.personalblog.utils;

import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

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

    @SuppressWarnings("unchecked")
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

                // 判断是否是简单类型
                boolean isScalar = isScalarType(clazz);

                while (rs.next()) {
                    if (isScalar) {
                        Object value = rs.getObject(1);
                        list.add((T) convertValue(value, clazz));
                    } else {
                        T entity = clazz.getDeclaredConstructor().newInstance();

                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object columnValue = rs.getObject(i);

                            String propertyName = convertColumnToProperty(columnName);
                            String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

                            try {
                                Method setter = findSetterMethod(clazz, setterName);
                                if (setter != null && columnValue != null) {
                                    Class<?> paramType = setter.getParameterTypes()[0];
                                    // 🔥 核心修正：加强了类型转换逻辑
                                    Object convertedValue = convertValue(columnValue, paramType);
                                    setter.invoke(entity, convertedValue);
                                }
                            } catch (Exception e) {
                                // 如果这行打印出来了，说明还是有类型不匹配的问题
                                // System.err.println("映射失败: " + propertyName + " -> " + columnValue.getClass().getName());
                            }
                        }
                        list.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> T executeQuerySingle(Class<T> clazz, String sql, Object... params) {
        List<T> list = executeQueryList(clazz, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    // --- 内部辅助方法 ---

    private static boolean isScalarType(Class<?> clazz) {
        return clazz == String.class ||
                Number.class.isAssignableFrom(clazz) ||
                clazz == Boolean.class ||
                clazz.isPrimitive();
    }

    /**
     * 🔥 核心修复：全能类型转换器
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        // 1. 处理 Java 8 新时间类型 (LocalDateTime -> java.util.Date)
        if (value instanceof LocalDateTime) {
            if (targetType == java.util.Date.class || targetType == java.sql.Timestamp.class) {
                return java.sql.Timestamp.valueOf((LocalDateTime) value);
            }
        }
        if (value instanceof LocalDate) {
            if (targetType == java.util.Date.class || targetType == java.sql.Date.class) {
                return java.sql.Date.valueOf((LocalDate) value);
            }
        }
        if (value instanceof LocalTime) {
            if (targetType == java.util.Date.class || targetType == java.sql.Time.class) {
                return java.sql.Time.valueOf((LocalTime) value);
            }
        }

        // 2. 处理数字类型转换
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

        // 3. 处理 String 转换
        if (targetType == String.class) {
            return value.toString();
        }

        return value;
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