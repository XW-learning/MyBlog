package com.personalblog.model;

import java.util.Date;

/**
 * 用户实体类 (对应 t_user 表)
 */
public class User {
    private Long id;            // 主键ID
    private String username;    // 账号
    private String password;    // 密码
    private String nickname;    // 昵称
    private String avatar;      // 头像URL
    private Date createTime;    // 注册时间

    // 无参构造器 (必须有)
    public User() {
    }

    // 全参构造器 (方便创建对象)
    public User(Long id, String username, String password, String nickname, String avatar, Date createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.createTime = createTime;
    }

    // --- Getters 和 Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', nickname='" + nickname + "'}";
    }
}