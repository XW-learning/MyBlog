package com.personalblog.service;

import com.personalblog.model.Comment;

import java.util.List;

public interface CommentService {

    /**
     * 发表评论
     *
     * @param comment 评论对象
     * @return 是否成功
     */
    boolean publishComment(Comment comment);

    /**
     * 根据文章ID获取评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<Comment> getCommentsByArticle(Long articleId);

    /**
     * [新增] 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId);

    /**
     * [新增] 根据 ID 获取单条评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    Comment getCommentById(Long id);
}