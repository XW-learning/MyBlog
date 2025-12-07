package com.personalblog.service.impl;

import com.personalblog.mapper.ArticleMapper;
import com.personalblog.mapper.impl.ArticleMapperImpl;
import com.personalblog.model.Article;
import com.personalblog.model.Category;
import com.personalblog.service.ArticleService;
import com.personalblog.utils.JDBCUtils;

import java.util.Date;
import java.util.List;

public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper = new ArticleMapperImpl();

    /**
     * 发布文章业务
     *
     * @param article 文章对象
     * @return 成功返回 true
     */
    @Override
    public Long publish(Article article) {
        // 1. 补全默认数据
        article.setViews(0);
        article.setLikes(0);
        // 注意：status 已由 Controller 设置，不再此处硬编码为 1

        Date now = new Date();
        article.setCreateTime(now);
        article.setUpdateTime(now);

        // 2. 摘要处理
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String content = article.getContent();
            if (content != null) {
                if (content.length() > 100) {
                    article.setSummary(content.substring(0, 100) + "...");
                } else {
                    article.setSummary(content);
                }
            } else {
                article.setSummary("");
            }
        }

        // 3. 保存并返回 ID
        return articleMapper.save(article);
    }

    @Override
    public List<Article> getAllUserArticles(Long userId) {
        return articleMapper.findListByUserId(userId);
    }

    /**
     * 获取指定用户已发布的文章（用于个人中心）
     *
     * @param userId 作者ID
     * @return 文章列表
     */
    @Override
    public List<Article> getUserPublishedArticles(Long userId) {
        final int PUBLISHED_STATUS = 1; // 1 表示已发布
        return articleMapper.findListByUserIdAndStatus(userId, PUBLISHED_STATUS);
    }

    /**
     * 获取所有已发布的文章（首页展示）
     *
     * @return 文章列表
     */
    @Override
    public List<Article> getPublishedArticles(String sort) {
        final int PUBLISHED_STATUS = 1; // 1 表示已发布
        // TODO: 这里将来可以做缓存
        return articleMapper.findListByStatusAndSort(PUBLISHED_STATUS, sort);
    }

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<Category> getAllCategories() {
        return articleMapper.findAllCategories();
    }

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public Article getArticleDetail(Long id) {
        // 1. 增加浏览量
        articleMapper.increaseViews(id);
        // 2. 查询文章详情
        return articleMapper.findById(id);
    }

    /**
     * 更新文章
     *
     * @param article 文章对象
     * @return 更新成功返回 true
     */
    @Override
    public boolean updateArticle(Article article) {
        // 假设文章ID和UserID已在 Servlet 中校验
        return articleMapper.update(article) > 0;
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 删除成功返回 true
     */
    @Override
    public boolean deleteArticle(Long articleId, Long userId) {
        // 业务逻辑：首先查询文章是否存在，并校验作者是否是当前用户
        Article existingArticle = articleMapper.findById(articleId);

        // 只有文章存在 且 文章的作者ID等于当前登录用户ID时，才允许删除
        if (existingArticle != null && existingArticle.getUserId().equals(userId)) {
            return articleMapper.deleteById(articleId) > 0;
        }
        return false;
    }

    /**
     * 切换点赞状态
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 切换成功返回 true
     */
    @Override
    public boolean toggleLike(Long userId, Long articleId) {
        // 1. 检查是否已经点赞
        boolean isLiked = articleMapper.isLiked(userId, articleId);

        if (isLiked) {
            // 2. 如果已赞，则取消赞，文章赞数 -1
            articleMapper.removeLike(userId, articleId);
            articleMapper.updateLikeCount(articleId, -1);
            return false; // 返回 false 表示当前状态为“未赞”
        } else {
            // 3. 如果未赞，则添加赞，文章赞数 +1
            articleMapper.addLike(userId, articleId);
            articleMapper.updateLikeCount(articleId, 1);
            return true; // 返回 true 表示当前状态为“已赞”
        }
    }

    /**
     * 获取文章点赞数
     *
     * @param articleId 文章ID
     * @return 点赞数
     */
    @Override
    public int getLikeCount(Long articleId) {
        Article article = articleMapper.findById(articleId);
        return article != null ? article.getLikes() : 0;
    }

    /**
     * 获取文章点赞数
     *
     * @return 热门文章列表
     */
    @Override
    public List<Article> getHotArticles() {
        return articleMapper.findHotArticles(5); // 取前5名
    }

    /**
     * 检查用户是否已点赞
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return true 表示已点赞，false 表示未点赞
     */
    @Override
    public boolean hasUserLiked(Long userId, Long articleId) {
        return articleMapper.isLiked(userId, articleId);
    }

    /**
     * 分页获取用户文章
     *
     * @param userId   用户ID
     * @param pageNum  当前页码 (从1开始)
     * @param pageSize 每页显示数量
     * @return 文章列表
     */
    @Override
    public List<Article> getUserArticlesPage(Long userId, int pageNum, int pageSize) {
        // 核心算法：计算数据库的 offset
        // 第1页: (1-1)*10 = 0
        // 第2页: (2-1)*10 = 10
        int offset = (pageNum - 1) * pageSize;
        return articleMapper.findListByUserIdPaginated(userId, offset, pageSize);
    }

    /**
     * 获取用户文章总数
     *
     * @param userId 用户ID
     * @return 文章总数
     */
    @Override
    public Long getUserArticlesCount(Long userId) {
        return articleMapper.countArticlesByUserId(userId);
    }

    /**
     * 获取用户总浏览量
     *
     * @param userId 用户ID
     * @return 总浏览量
     */
    @Override
    public Long getTotalViews(Long userId) {
        return articleMapper.sumViewsByUserId(userId);
    }

    /**
     * 获取用户总点赞数
     *
     * @param userId 用户ID
     * @return 总点赞数
     */
    @Override
    public Long getTotalLikes(Long userId) {
        return articleMapper.sumLikesByUserId(userId);
    }

}