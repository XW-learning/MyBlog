package com.personalblog.mapper.impl;

import com.personalblog.mapper.CommentMapper;
import com.personalblog.model.Comment;
import com.personalblog.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CommentMapperImpl implements CommentMapper {

    /**
     * 添加评论
     *
     * @param comment 评论模型
     * @return 影响行数
     */
    @Override
    public int saveComment(Comment comment) {
        String sql = "INSERT INTO t_comment (article_id, user_id, content, parent_id, create_time)" + " VALUES (?, ?, ?, ?, NOW())";
        try {
            return JDBCUtils.executeUpdate(sql, comment.getArticleId(), comment.getUserId(), comment.getContent(), comment.getParentId());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据文章ID查询评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    @Override
    public List<Comment> findListByArticleId(Long articleId) {
        List<Comment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConnection();
            String sql = "SELECT c.*, u.nickname, u.avatar " + "FROM t_comment c " + "LEFT JOIN t_user u ON c.user_id = u.id " + "WHERE c.article_id = ? " + "ORDER BY c.create_time DESC";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, articleId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Comment c = new Comment();

                c.setId(rs.getLong("id"));
                c.setArticleId(rs.getLong("article_id"));
                c.setUserId(rs.getLong("user_id"));
                c.setContent(rs.getString("content"));

                Long parentId = rs.getLong("parent_id");
                if (rs.wasNull()) {
                    c.setParentId(null);
                } else {
                    c.setParentId(parentId);
                }
                c.setCreateTime(rs.getTimestamp("create_time"));
                // 填充用户信息
                c.setUserNickname(rs.getString("nickname"));
                c.setUserAvatar(rs.getString("avatar"));

                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return list;
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 影响行数
     */
    @Override
    public int deleteCommentById(Long id) {
        String sql = "DELETE FROM t_comment WHERE id = ?";
        return JDBCUtils.executeUpdate(sql, id);
    }

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论模型
     */
    @Override
    public Comment findCommentById(Long id) {
        String sql = "SELECT * FROM t_comment WHERE id = ?";
        return JDBCUtils.executeQuerySingle(Comment.class, sql, id);
    }
}