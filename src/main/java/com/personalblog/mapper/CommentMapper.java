package com.personalblog.mapper;

import com.personalblog.model.Comment;
import java.util.List;

public interface CommentMapper {
    /**
     * 保存评论
     * @param comment 评论对象
     * @return 影响的行数
     */
    int saveComment(Comment comment);

    /**
     * 根据文章ID查询评论列表
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<Comment> findListByArticleId(Long articleId);

    /**
     * 删除评论
     * @param id 评论ID
     * @return 影响的行数
     */
    int deleteCommentById(Long id);

    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论对象
     */
    Comment findCommentById(Long id);
}