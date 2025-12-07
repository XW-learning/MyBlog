package com.personalblog.mapper.impl;

import com.personalblog.mapper.ArticleMapper;
import com.personalblog.model.Article;
import com.personalblog.model.Category;
import com.personalblog.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class ArticleMapperImpl implements ArticleMapper {

    /**
     * 新增文章
     *
     * @param article 文章模型
     * @return 添加成功返回文章ID
     */
    @Override
    public Long save(Article article) {
        String sql = "INSERT INTO t_article " + "(user_id, title, summary, category_id, content, views, likes, status, create_time, update_time) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConnection();
            // 关键：指定 RETURN_GENERATED_KEYS
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, article.getUserId());
            ps.setString(2, article.getTitle());
            ps.setString(3, article.getSummary());
            ps.setObject(4, article.getCategoryId() == 0 ? null : article.getCategoryId()); // 允许草稿无分类
            ps.setString(5, article.getContent());
            ps.setInt(6, article.getViews());
            ps.setInt(7, article.getLikes());
            ps.setInt(8, article.getStatus());
            ps.setTimestamp(9, new java.sql.Timestamp(article.getCreateTime().getTime()));
            ps.setTimestamp(10, new java.sql.Timestamp(article.getUpdateTime().getTime()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // 获取生成的 ID
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return -1L;
    }

    /**
     * 根据用户ID和状态查询文章列表（用于个人中心和草稿箱）
     *
     * @param userId 作者ID
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    @Override
    public List<Article> findListByUserIdAndStatus(Long userId, Integer status) {
        String sql = "SELECT id, title, create_time, views, likes, status " + "FROM t_article WHERE user_id = ? AND status = ? ORDER BY create_time DESC";
        return JDBCUtils.executeQueryList(Article.class, sql, userId, status);
    }

    /**
     * 获取指定状态的文章列表 (用于首页)
     *
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    @Override
    public List<Article> findListByStatus(Integer status) {
        String sql = "SELECT a.id, a.user_id, a.title, a.summary, a.create_time, a.views, a.likes, " + "u.nickname AS authorNickname " + "FROM t_article a " + "JOIN t_user u ON a.user_id = u.id " + "WHERE a.status = ? ORDER BY a.create_time DESC";

        return JDBCUtils.executeQueryList(Article.class, sql, status);
    }

    /**
     * 获取指定状态的文章列表 (用于首页)
     *
     * @param status    状态 (1: 已发布, 0: 草稿)
     * @param sortOrder 排序方式 (new: 最新, hot: 热度)
     * @return 文章列表
     */
    @Override
    public List<Article> findListByStatusAndSort(Integer status, String sortOrder) {
        String orderByClause = "ORDER BY a.create_time DESC";

        if ("hot".equals(sortOrder)) {
            orderByClause = "ORDER BY a.likes DESC, a.views DESC";
        } else if ("new".equals(sortOrder) || "recommend".equals(sortOrder)) {
            orderByClause = "ORDER BY a.create_time DESC";
        }

        String sql = "SELECT a.id, a.user_id, a.title, a.summary, a.create_time, a.views, a.likes, " + "u.nickname AS authorNickname " + "FROM t_article a " + "JOIN t_user u ON a.user_id = u.id " + "WHERE a.status = ? " + orderByClause;
        return JDBCUtils.executeQueryList(Article.class, sql, status);
    }

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<Category> findAllCategories() {
        String sql = "SELECT id, name, create_time FROM t_category ORDER BY id ASC";
        return JDBCUtils.executeQueryList(Category.class, sql);
    }

    /**
     * 根据文章ID查询文章
     *
     * @param id 文章ID
     * @return 文章对象
     */
    @Override
    public Article findById(Long id) {
        String sql = "SELECT * FROM t_article WHERE id = ?";
        return JDBCUtils.executeQuerySingle(Article.class, sql, id);
    }

    /**
     * 增加文章阅读数
     *
     * @param id 文章ID
     */
    @Override
    public void increaseViews(Long id) {
        try {
            String sql = "UPDATE t_article SET views = views + 1 WHERE id = ?";
            JDBCUtils.executeUpdate(sql, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新文章
     *
     * @param article 文章对象
     * @return 更新结果
     */
    @Override
    public int update(Article article) {
        // 增加 status=? 用于草稿转发布
        String sql = "UPDATE t_article SET title=?, summary=?, content=?, category_id=?, status=?, update_time=NOW() " + "WHERE id=? AND user_id=?";
        // 使用 JDBCUtils 需要保证参数顺序一致
        try {
            return JDBCUtils.executeUpdate(sql, article.getTitle(), article.getSummary(), article.getContent(), article.getCategoryId() == 0 ? null : article.getCategoryId(), article.getStatus(), article.getId(), article.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据用户ID查询文章列表（用于个人中心）
     *
     * @param userId 用户ID
     * @return 文章列表
     */
    @Override
    public List<Article> findListByUserId(Long userId) {
        String sql = "SELECT id, title, create_time, views, likes, status " +
                "FROM t_article WHERE user_id = ? ORDER BY create_time DESC";

        return JDBCUtils.executeQueryList(Article.class, sql, userId);
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 删除结果
     */
    @Override
    public int deleteById(Long id) {
        try {
            String sql = "DELETE FROM t_article WHERE id=?";
            return JDBCUtils.executeUpdate(sql, id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 判断用户是否已点赞
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已点赞
     */
    @Override
    public boolean isLiked(Long userId, Long articleId) {
        String sql = "SELECT COUNT(*) FROM t_article_like WHERE user_id = ? AND article_id = ?";
        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, articleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 添加点赞记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    @Override
    public void addLike(Long userId, Long articleId) {
        String sql = "INSERT INTO t_article_like (user_id, article_id) VALUES (?, ?)";
        try {
            JDBCUtils.executeUpdate(sql, userId, articleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除点赞记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    @Override
    public void removeLike(Long userId, Long articleId) {
        String sql = "DELETE FROM t_article_like WHERE user_id = ? AND article_id = ?";
        try {
            JDBCUtils.executeUpdate(sql, userId, articleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新文章点赞数
     *
     * @param articleId 文章ID
     * @param increment 增量
     */
    @Override
    public void updateLikeCount(Long articleId, int increment) {
        String sql = "UPDATE t_article SET likes = likes + ? WHERE id = ?";
        try {
            JDBCUtils.executeUpdate(sql, increment, articleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询最热门的文章
     *
     * @param limit 查询数量
     * @return 最热门的文章列表
     */
    @Override
    public List<Article> findHotArticles(int limit) {
        String sql = "SELECT id, title, views, likes FROM t_article " + "WHERE status = 1 ORDER BY likes DESC, views DESC LIMIT ?";
        return JDBCUtils.executeQueryList(Article.class, sql, limit);
    }

    /**
     * 根据用户ID分页查询文章列表
     *
     * @param userId 用户ID
     * @param offset 起始索引
     * @param limit  每页数量
     * @return 文章列表
     */
    @Override
    public List<Article> findListByUserIdPaginated(Long userId, int offset, int limit) {
        // 使用 MySQL 的 LIMIT 语法进行物理分页
        String sql = "SELECT id, title, create_time, views, likes, status " +
                "FROM t_article WHERE user_id = ? ORDER BY create_time DESC LIMIT ?, ?";
        // 注意参数顺序：userId, offset, limit
        return JDBCUtils.executeQueryList(Article.class, sql, userId, offset, limit);
    }

    /**
     * 根据用户ID查询文章数量
     *
     * @param userId 用户ID
     * @return 文章数量
     */
    @Override
    public Long countArticlesByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM t_article WHERE user_id = ?";
        // JDBCUtils.executeQuerySingle 需要指定返回类型 Long.class
        return JDBCUtils.executeQuerySingle(Long.class, sql, userId);
    }

    /**
     * 获取用户文章总浏览量
     *
     * @param userId 用户ID
     * @return 总浏览量
     */
    @Override
    public Long sumViewsByUserId(Long userId) {
        // 使用 SQL 的 SUM 函数
        String sql = "SELECT SUM(views) FROM t_article WHERE user_id = ?";
        Long sum = JDBCUtils.executeQuerySingle(Long.class, sql, userId);
        // 如果没有文章，SUM 结果可能是 null，需转为 0
        return sum != null ? sum : 0L;
    }

    /**
     * 获取用户文章总点赞数
     *
     * @param userId 用户ID
     * @return 总点赞数
     */
    @Override
    public Long sumLikesByUserId(Long userId) {
        String sql = "SELECT SUM(likes) FROM t_article WHERE user_id = ?";
        Long sum = JDBCUtils.executeQuerySingle(Long.class, sql, userId);
        return sum != null ? sum : 0L;
    }
}