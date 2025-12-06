package com.personalblog.model;

import java.util.Date;

/**
 * 文章实体类 (对应 t_article 表)
 */
public class Article {
    private Long id;            // 文章ID
    private Long userId;        // 作者ID (关联 User.id)
    private Long categoryId;    // 分类ID (关联 Category.id)
    private String authorNickname; // 作者昵称
    private String title;       // 标题
    private String summary;     // 摘要
    private String content;     // 正文
    private Integer views;      // 浏览量
    private Integer likes;      // 点赞数
    private Integer status;     // 状态: 0-草稿, 1-已发布
    private Date createTime;    // 发布时间
    private Date updateTime;    // 更新时间

    public Article() {
    }

    public Article(Long id, Long userId, Long categoryId, String authorNickname, String title, String summary, String content, Integer views, Integer likes, Integer status, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.authorNickname = authorNickname;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.views = views;
        this.likes = likes;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // --- Getters 和 Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}