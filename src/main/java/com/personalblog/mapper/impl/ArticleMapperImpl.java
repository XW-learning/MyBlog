package com.personalblog.mapper.impl;

import com.personalblog.mapper.ArticleMapper;
import com.personalblog.model.Article;
import com.personalblog.model.Category;
import com.personalblog.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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
        String sql = "INSERT INTO t_article " +
                "(user_id, title, summary, category_id, content, views, likes, status, create_time, update_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        List<Article> articleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtils.getConnection();
            String sql = "SELECT id, title, create_time, views, likes, status " +
                    "FROM t_article WHERE user_id = ? AND status = ? ORDER BY create_time DESC";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setInt(2, status);
            rs = ps.executeQuery();

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setTitle(rs.getString("title"));
                article.setCreateTime(rs.getTimestamp("create_time"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                article.setStatus(rs.getInt("status"));
                articleList.add(article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return articleList;
    }

    /**
     * 获取指定状态的文章列表 (用于首页)
     *
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    @Override
    public List<Article> findListByStatus(Integer status) {
        List<Article> articleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtils.getConnection();
            String sql = "SELECT " +
                    "a.id, a.user_id, a.title, a.summary, a.create_time, a.views, a.likes, " +
                    "u.nickname AS authorNickname " +
                    "FROM t_article a " +
                    "JOIN t_user u ON a.user_id = u.id " +
                    "WHERE a.status = ? ORDER BY a.create_time DESC";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            rs = ps.executeQuery();

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setUserId(rs.getLong("user_id"));
                article.setTitle(rs.getString("title"));
                article.setSummary(rs.getString("summary"));
                article.setCreateTime(rs.getTimestamp("create_time"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                article.setAuthorNickname(rs.getString("authorNickname"));

                articleList.add(article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return articleList;
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
        List<Article> articleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtils.getConnection();

            // 1. 确定排序规则 (防止 SQL 注入，不要直接拼前端传来的字符串)
            String orderByClause = "ORDER BY a.create_time DESC"; // 默认：最新

            if ("hot".equals(sortOrder)) {
                orderByClause = "ORDER BY a.likes DESC, a.views DESC"; // 热榜：优先按点赞，其次按浏览
            } else if ("new".equals(sortOrder)) {
                orderByClause = "ORDER BY a.create_time DESC"; // 最新
            }
            // "recommend" 推荐：这里暂时也按最新，或者你可以改成按 views 排序

            // 2. 拼接 SQL
            String sql = "SELECT a.id, a.user_id, a.title, a.summary, a.create_time, a.views, a.likes, " +
                    "u.nickname AS authorNickname " +
                    "FROM t_article a " +
                    "JOIN t_user u ON a.user_id = u.id " +
                    "WHERE a.status = ? " +
                    orderByClause; // 🔥 拼接排序子句

            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            rs = ps.executeQuery();

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setUserId(rs.getLong("user_id"));
                article.setTitle(rs.getString("title"));
                article.setSummary(rs.getString("summary"));
                article.setCreateTime(rs.getTimestamp("create_time"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                article.setAuthorNickname(rs.getString("authorNickname"));
                articleList.add(article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return articleList;
    }

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<Category> findAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtils.getConnection();
            String sql = "SELECT id, name, create_time FROM t_category ORDER BY id ASC";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setCreateTime(rs.getTimestamp("create_time"));
                categoryList.add(category);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return categoryList;
    }

    /**
     * 根据文章ID查询文章
     *
     * @param id 文章ID
     * @return 文章对象
     */
    @Override
    public Article findById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Article article = null;
        try {
            conn = JDBCUtils.getConnection();
            // 这里为了简单，暂不关联 User 表查作者名，后续可优化
            String sql = "SELECT * FROM t_article WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                article = new Article();
                article.setId(rs.getLong("id"));
                article.setUserId(rs.getLong("user_id"));
                article.setTitle(rs.getString("title"));
                article.setSummary(rs.getString("summary"));
                article.setContent(rs.getString("content"));
                article.setCategoryId(rs.getLong("category_id"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                article.setStatus(rs.getInt("status"));
                article.setCreateTime(rs.getTimestamp("create_time"));
                article.setUpdateTime(rs.getTimestamp("update_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return article;
    }

    /**
     * 增加文章阅读数
     *
     * @param id 文章ID
     */
    @Override
    public void increaseViews(Long id) {
        try {
            // 直接用 SQL 更新，效率更高
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
        String sql = "UPDATE t_article SET title=?, summary=?, content=?, category_id=?, status=?, update_time=NOW() " +
                "WHERE id=? AND user_id=?";
        // 使用 JDBCUtils 需要保证参数顺序一致
        try {
            return JDBCUtils.executeUpdate(sql,
                    article.getTitle(),
                    article.getSummary(),
                    article.getContent(),
                    article.getCategoryId() == 0 ? null : article.getCategoryId(),
                    article.getStatus(), // 新增
                    article.getId(),
                    article.getUserId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 删除文章
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @Override
    public List<Article> findListByUserId(Long userId) {
        List<Article> articleList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtils.getConnection();
            // 去掉 status 条件
            String sql = "SELECT id, title, create_time, views, likes, status " +
                    "FROM t_article WHERE user_id = ? ORDER BY create_time DESC";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setTitle(rs.getString("title"));
                article.setCreateTime(rs.getTimestamp("create_time"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                article.setStatus(rs.getInt("status"));
                articleList.add(article);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn, ps, rs);
        }
        return articleList;
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
        try {
            Connection conn = JDBCUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setLong(2, articleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // 注意：查询操作记得在外部或这里 close 资源，这里简化了
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
        // 这里的 increment 可以是 +1 或 -1
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
        List<Article> list = new ArrayList<>();
        String sql = "SELECT id, title, views, likes FROM t_article " +
                "WHERE status = 1 ORDER BY likes DESC, views DESC LIMIT ?";
        try {
            Connection conn = JDBCUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getLong("id"));
                article.setTitle(rs.getString("title"));
                article.setViews(rs.getInt("views"));
                article.setLikes(rs.getInt("likes"));
                list.add(article);
            }
            JDBCUtils.close(conn, ps, rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
