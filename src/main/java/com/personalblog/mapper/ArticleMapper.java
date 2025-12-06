package com.personalblog.mapper;

import com.personalblog.model.Article;
import com.personalblog.model.Category;

import java.util.List;

public interface ArticleMapper {
    /**
     * 新增文章
     * @return 成功返回 true
     */
//    boolean save(Article article);

    /**
     * 新增文章并返回文章ID
     * @return 文章ID
     */
    Long save(Article article);

    /**
     * 根据用户ID和状态查询文章列表
     * @param userId 作者ID
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    List<Article> findListByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据用户ID查询文章列表
     * @param userId 作者ID
     * @return 文章列表
     */
    List<Article> findListByUserId(Long userId);

    /**
     * 根据状态和排序字段查询文章列表
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @param sortOrder 排序字段 (views: 浏览数, likes: 点赞数)
     * @return 文章列表
     */
    List<Article> findListByStatusAndSort(Integer status, String sortOrder);

    /**
     * 根据状态查询文章列表
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    List<Article> findListByStatus(Integer status);

    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<Category> findAllCategories();

    /**
     * 根据ID查询文章
     * @param id 文章ID
     * @return 文章
     */
    Article findById(Long id);

    /**
     * 根据ID更新文章阅读数
     *
     * @param id 文章ID
     */
    void increaseViews(Long id);

    /**
     * 更新文章
     * @param article 文章
     * @return 更新数量
     */
    int update(Article article);

    /**
     * 根据ID删除文章
     * @param id 文章ID
     * @return 删除数量
     */
    int deleteById(Long id);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long userId, Long articleId);

    /**
     * 添加点赞记录
     */
    void addLike(Long userId, Long articleId);

    /**
     * 取消点赞记录
     */
    void removeLike(Long userId, Long articleId);

    /**
     * 更新文章点赞数 (increment 为正数加，负数减)
     */
    void updateLikeCount(Long articleId, int increment);

    /**
     * 查询热度榜 (按点赞数倒序，取前N条)
     */
    List<Article> findHotArticles(int limit);
}