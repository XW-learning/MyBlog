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
        String sql = "INSERT INTO t_comment (article_id, user_id, content, parent_id, create_time)" +
                " VALUES (?, ?, ?, ?, NOW())";
        try {
            return JDBCUtils.executeUpdate(sql,
                    comment.getArticleId(),
                    comment.getUserId(),
                    comment.getContent(),
                    comment.getParentId()
            );
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
            // 🔥 关键点：使用 LEFT JOIN 关联 t_user 表，获取评论人的昵称和头像
            String sql = "SELECT c.*, u.nickname, u.avatar " +
                    "FROM t_comment c " +
                    "LEFT JOIN t_user u ON c.user_id = u.id " +
                    "WHERE c.article_id = ? " +
                    "ORDER BY c.create_time DESC";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, articleId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getLong("id"));
                c.setArticleId(rs.getLong("article_id"));
                c.setUserId(rs.getLong("user_id"));
                c.setContent(rs.getString("content"));
                c.setParentId(rs.getLong("parent_id"));
                if (rs.wasNull()) c.setParentId(null); // 处理 bigint null
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

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getLong("id"));
                    c.setArticleId(rs.getLong("article_id"));
                    c.setUserId(rs.getLong("user_id")); // 关键：我们需要这个ID来做对比
                    c.setContent(rs.getString("content"));
                    c.setParentId(rs.getLong("parent_id"));
                    // 其他字段按需设置...
                    return c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}