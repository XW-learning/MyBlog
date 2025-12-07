package com.personalblog.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.personalblog.model.Article;
import com.personalblog.model.Category;
import com.personalblog.model.User;
import com.personalblog.service.ArticleService;
import com.personalblog.service.impl.ArticleServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(value = "/article")
public class ArticleServlet extends HttpServlet {

    private final ArticleService articleService = new ArticleServiceImpl();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    // ----------------------------------------------------------------
    // POST: 处理所有【需登录】的业务 (发布、删除、修改、点赞、查询个人列表)
    // ----------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");

        // 1. 全局登录检查
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            sendJson(resp, false, "登录已过期，请重新登录！", null);
            return;
        }

        // 2. 路由分发
        if ("publishArticle".equalsIgnoreCase(action)) {
            // 发布文章
            handlePublish(req, resp, currentUser);
        } else if ("deleteArticle".equalsIgnoreCase(action)) {
            // 删除文章
            handleDelete(req, resp, currentUser);
        } else if ("updateArticle".equalsIgnoreCase(action)) {
            // 修改文章
            handleUpdate(req, resp, currentUser);
        } else if ("like".equalsIgnoreCase(action)) {
            // 点赞文章
            handleLike(req, resp, currentUser);
        } else if ("loadArticleList".equalsIgnoreCase(action)) {
            // 查询个人列表
            handleArticleList(req, resp, currentUser);
        } else {
            sendJson(resp, false, "缺失或不支持的 POST 动作: " + action, null);
        }
    }

    // ----------------------------------------------------------------
    // GET: 处理所有【开放】的查询业务 (首页列表、分类、详情、热榜)
    // ----------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");
        if (action != null) action = action.trim();

        if ("loadIndexArticleList".equalsIgnoreCase(action)) {
            // 首页列表
            handleIndexArticleList(req, resp);
        } else if ("loadCategories".equalsIgnoreCase(action)) {
            // 分类
            handleLoadCategories(req, resp);
        } else if ("getDetail".equalsIgnoreCase(action)) {
            // 详情
            handleGetDetail(req, resp);
        } else if ("loadHotArticles".equalsIgnoreCase(action)) {
            // 热榜
            handleLoadHotArticles(req, resp);
        } else if ("checkLikeStatus".equalsIgnoreCase(action)) {
            // 检查点赞状态
            handleCheckLikeStatus(req, resp);
        } else {
            sendJson(resp, false, "缺失或不支持的 GET 动作: [" + action + "]", null);
        }
    }

    // ----------------------------------------------------------------
    // 处理方法实现
    // ----------------------------------------------------------------

    /**
     * 获取文章列表
     *
     * @param req         请求
     * @param resp        响应
     * @param currentUser 当前用户
     * @throws IOException IO 异常
     */
    private void handleArticleList(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            List<Article> articles = articleService.getAllUserArticles(currentUser.getId());
            sendJson(resp, true, "获取文章列表成功", articles);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "获取文章列表失败", null);
        }
    }

    /**
     * 发布文章
     *
     * @param req         请求
     * @param resp        响应
     * @param currentUser 当前用户
     * @throws IOException IO 异常
     */
    private void handlePublish(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String title = req.getParameter("title");
        String content = req.getParameter("content");
        String categoryIdStr = req.getParameter("categoryId");
        String statusStr = req.getParameter("status"); // 获取状态参数

        // 草稿允许分类为空，发布不允许
        int status = (statusStr != null) ? Integer.parseInt(statusStr) : 1;

        if (title == null || title.trim().isEmpty()) {
            sendJson(resp, false, "标题不能为空", null);
            return;
        }

        try {
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ? Long.parseLong(categoryIdStr) : 0L;

            Article article = new Article();
            article.setTitle(title);
            article.setContent(content);
            article.setUserId(currentUser.getId());
            article.setCategoryId(categoryId);
            article.setStatus(status); // 设置状态

            Long newId = articleService.publish(article);
            if (newId > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("newId", newId);
                sendJson(resp, true, "操作成功", data);
            } else {
                sendJson(resp, false, "操作失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "服务器错误：" + e.getMessage(), null);
        }
    }

    /**
     * 删除文章
     *
     * @param req         请求
     * @param resp        响应
     * @param currentUser 当前用户
     * @throws IOException IO 错误
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String articleIdStr = req.getParameter("id");
        try {
            Long articleId = Long.parseLong(articleIdStr);
            boolean success = articleService.deleteArticle(articleId, currentUser.getId());
            if (success) sendJson(resp, true, "删除成功", null);
            else sendJson(resp, false, "删除失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "错误", null);
        }
    }

    /**
     * 更新文章
     *
     * @param req         请求
     * @param resp        响应
     * @param currentUser 当前用户
     * @throws IOException IO 错误
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            String statusStr = req.getParameter("status");
            int status = (statusStr != null) ? Integer.parseInt(statusStr) : 1;
            String categoryIdStr = req.getParameter("categoryId");
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ? Long.parseLong(categoryIdStr) : 0L;

            Article article = new Article();
            article.setId(Long.parseLong(req.getParameter("id")));
            article.setTitle(req.getParameter("title"));
            article.setContent(req.getParameter("content"));
            // 自动截取摘要
            String content = req.getParameter("content");
            article.setSummary(content.length() > 100 ? content.substring(0, 100) : content);
            article.setCategoryId(categoryId);
            article.setUserId(currentUser.getId());
            article.setStatus(status); // 设置状态

            boolean success = articleService.updateArticle(article);
            if (success) sendJson(resp, true, "更新成功", null);
            else sendJson(resp, false, "更新失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "错误", null);
        }
    }

    /**
     * 点赞
     *
     * @param req         请求
     * @param resp        响应
     * @param currentUser 当前用户
     * @throws IOException IO 错误
     */
    private void handleLike(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            Long aid = Long.parseLong(req.getParameter("id"));
            boolean liked = articleService.toggleLike(currentUser.getId(), aid);
            int count = articleService.getLikeCount(aid);
            Map<String, Object> d = new HashMap<>();
            d.put("isLiked", liked);
            d.put("newCount", count);
            sendJson(resp, true, "OK", d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取首页文章列表
     *
     * @param req  请求
     * @param resp 响应
     * @throws IOException IO 错误
     */
    private void handleIndexArticleList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. 获取排序参数 (recommend, new, hot)
            String sort = req.getParameter("sort");

            // 默认处理：如果为空，则是推荐(默认最新)
            if (sort == null || sort.isEmpty()) {
                sort = "new";
            }

            // 2. 调用 Service (传入 sort)
            List<Article> articles = articleService.getPublishedArticles(sort);

            sendJson(resp, true, "获取首页文章列表成功", articles);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "获取首页文章列表失败", null);
        }
    }

    /**
     * 加载所有分类
     *
     * @param req  请求
     * @param resp 响应
     * @throws IOException IO 错误
     */
    private void handleLoadCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Category> categories = articleService.getAllCategories();
            sendJson(resp, true, "获取成功", categories);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "失败", null);
        }
    }

    /**
     * 获取文章详情
     *
     * @param req  请求
     * @param resp 响应
     * @throws IOException IO 错误
     */
    private void handleGetDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        try {
            Long id = Long.parseLong(idStr);
            Article article = articleService.getArticleDetail(id);
            if (article != null) sendJson(resp, true, "成功", article);
            else sendJson(resp, false, "不存在", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "错误", null);
        }
    }

    /**
     * 获取热门文章
     *
     * @param req  请求
     * @param resp 响应
     * @throws IOException IO 错误
     */
    private void handleLoadHotArticles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Article> hots = articleService.getHotArticles();
            sendJson(resp, true, "成功", hots);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "失败", null);
        }
    }

    /**
     * 检查用户是否点赞
     *
     * @param req  请求
     * @param resp 响应
     * @throws IOException IO 错误
     */
    private void handleCheckLikeStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String idStr = req.getParameter("id");
        if (currentUser == null || idStr == null) {
            sendJson(resp, true, "", false);
            return;
        }
        boolean liked = articleService.hasUserLiked(currentUser.getId(), Long.parseLong(idStr));
        sendJson(resp, true, "查询成功", liked);
    }

    /**
     * 发送 JSON 数据
     *
     * @param resp    响应
     * @param success 是否成功
     * @param message 消息
     * @param data    数据
     * @throws IOException IO 错误
     */
    private void sendJson(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }
}