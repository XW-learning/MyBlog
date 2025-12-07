package com.personalblog.mapper;

import com.personalblog.model.Article;
import com.personalblog.model.Category;

import java.util.List;

public interface ArticleMapper {
    /**
     * 新增文章并返回文章ID
     *
     * @param article 文章对象
     * @return 文章ID
     */
    Long save(Article article);

    /**
     * 根据用户ID和状态查询文章列表
     *
     * @param userId 作者ID
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    List<Article> findListByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据用户ID查询文章列表
     *
     * @param userId 作者ID
     * @return 文章列表
     */
    List<Article> findListByUserId(Long userId);

    /**
     * 根据状态和排序字段查询文章列表
     *
     * @param status    状态 (1: 已发布, 0: 草稿)
     * @param sortOrder 排序字段 (views: 浏览数, likes: 点赞数)
     * @return 文章列表
     */
    List<Article> findListByStatusAndSort(Integer status, String sortOrder);

    /**
     * 根据状态查询文章列表
     *
     * @param status 状态 (1: 已发布, 0: 草稿)
     * @return 文章列表
     */
    List<Article> findListByStatus(Integer status);

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    List<Category> findAllCategories();

    /**
     * 根据ID查询文章
     *
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
     *
     * @param article 文章
     * @return 更新数量
     */
    int update(Article article);

    /**
     * 根据ID删除文章
     *
     * @param id 文章ID
     * @return 删除数量
     */
    int deleteById(Long id);

    /**
     * 判断用户是否已点赞
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return true 已点赞，false 未点赞
     */
    boolean isLiked(Long userId, Long articleId);

    /**
     * 添加点赞记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    void addLike(Long userId, Long articleId);

    /**
     * 取消点赞记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    void removeLike(Long userId, Long articleId);

    /**
     * 更新文章点赞数 (increment 为正数加，负数减)
     *
     * @param articleId 文章ID
     * @param increment 增量值
     */
    void updateLikeCount(Long articleId, int increment);

    /**
     * 查询热度榜 (按点赞数倒序，取前N条)
     *
     * @param limit 限制数量
     * @return 热度文章列表
     */
    List<Article> findHotArticles(int limit);

    /**
     * 根据用户ID分页查询文章列表
     *
     * @param userId 用户ID
     * @param offset 偏移量（起始位置）
     * @param limit  每页数量
     * @return 文章列表
     */
    List<Article> findListByUserIdPaginated(Long userId, int offset, int limit);

    /**
     * 根据用户ID统计文章总数
     *
     * @param userId 用户ID
     * @return 文章总数
     */
    Long countArticlesByUserId(Long userId);

    /**
     * 获取用户文章总浏览量
     *
     * @param userId 用户ID
     * @return 总浏览量
     */
    Long sumViewsByUserId(Long userId);

    /**
     * 获取用户文章总点赞数
     *
     * @param userId 用户ID
     * @return 总点赞数
     */
    Long sumLikesByUserId(Long userId);
}