package com.personalblog.service;

import com.personalblog.model.Article;
import com.personalblog.model.Category;

import java.util.List;

public interface ArticleService {
    /**
     * 新增文章
     * @param article 文章对象
     * @return 添加成功返回 true
     */
    boolean publish(Article article);

    /**
     * 获取指定用户已发布的文章（用于个人中心）
     * @param userId 用户ID
     * @return 文章列表
     */
    List<Article> getUserPublishedArticles(Long userId);

    /**
     * 获取已发布的文章列表（用于首页）
     * @return 文章列表
     */
    List<Article> getPublishedArticles(String sort);

    /**
     * 获取所有分类
     * @return 分类列表
     */
    List<Category> getAllCategories();

    /**
     * 获取文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    Article getArticleDetail(Long id);

    /**
     * 更新文章
     * @param article 文章对象
     * @return 更新成功返回 true
     */
    boolean updateArticle(Article article);

    /**
     * 删除文章
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 删除成功返回 true
     */
    boolean deleteArticle(Long articleId, Long userId);

    /**
     * 点赞功能
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 点赞成功返回 true
     */
    boolean toggleLike(Long userId, Long articleId);

    /**
     * 获取文章点赞数
     * @param articleId 文章ID
     * @return 点赞数
     */
    int getLikeCount(Long articleId);

    /**
     * 获取最热门的文章
     * @return 最热门的文章列表
     */
    List<Article> getHotArticles();

    /**
     * 检查用户是否已点赞
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return true 表示已点赞，false 表示未点赞
     */
    boolean hasUserLiked(Long userId, Long articleId);
}