package com.personalblog.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Long parentId; // 父评论ID
    private Date createTime;
    private String parentNickname;
    private List<Comment> children = new ArrayList<>();

    // 🔥 辅助字段 (数据库表中没有，通过 JOIN 查询填充)
    private String userNickname;
    private String userAvatar;

    public Comment() {
    }

    public Comment(Long id, Long articleId, Long userId, String content, Long parentId, Date createTime, String parentNickname, List<Comment> children, String userNickname, String userAvatar) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        this.createTime = createTime;
        this.parentNickname = parentNickname;
        this.children = children;
        this.userNickname = userNickname;
        this.userAvatar = userAvatar;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getParentNickname() {
        return parentNickname;
    }

    public void setParentNickname(String parentNickname) {
        this.parentNickname = parentNickname;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public void setChildren(List<Comment> children) {
        this.children = children;
    }
}