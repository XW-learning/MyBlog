package com.personalblog.service.impl;

import com.personalblog.mapper.CommentMapper;
import com.personalblog.mapper.impl.CommentMapperImpl;
import com.personalblog.model.Comment;
import com.personalblog.service.CommentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper = new CommentMapperImpl();

    /**
     * 发表评论
     *
     * @param comment 评论对象
     * @return 插入成功返回 true
     */
    @Override
    public boolean publishComment(Comment comment) {
        return commentMapper.saveComment(comment) > 0;
    }

    /**
     * 根据文章ID查询评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    @Override
    public List<Comment> getCommentsByArticle(Long articleId) {
        // 1. 查询出该文章下的所有评论 (扁平列表)
        List<Comment> allComments = commentMapper.findListByArticleId(articleId);

        // 如果没有评论，直接返回空
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 准备一个容器 (根节点列表)
        List<Comment> rootComments = new ArrayList<>();

        // 3. 准备一个 Map 用于快速查找 (Key: CommentId, Value: Comment对象)
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment c : allComments) {
            commentMap.put(c.getId(), c);
        }

        // 4. 遍历所有评论，进行归位
        for (Comment comment : allComments) {
            Long parentId = comment.getParentId();

            if (parentId == null || parentId == 0) {
                // A. 如果没有父ID，它就是顶级评论 (根节点)
                rootComments.add(comment);
            } else {
                // B. 如果有父ID，找到它的父亲
                Comment parent = commentMap.get(parentId);
                if (parent != null) {
                    // 设置 "回复 @某某" 的昵称 (用于前端展示)
                    comment.setParentNickname(parent.getUserNickname());
                    // 把它加入到父亲的 children 列表中
                    parent.getChildren().add(comment);
                }
            }
        }

        return rootComments;
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 删除成功返回 true
     */
    @Override
    public boolean deleteComment(Long commentId) {
        return commentMapper.deleteCommentById(commentId) > 0;
    }

    /**
     * 根据 ID 查询单条评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    @Override
    public Comment getCommentById(Long id) {
        return commentMapper.findCommentById(id);
    }
}