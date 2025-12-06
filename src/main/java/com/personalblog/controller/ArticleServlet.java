package com.personalblog.controller;

import com.google.gson.Gson;
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
    private final Gson gson = new Gson();

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
            handlePublish(req, resp, currentUser);
        } else if ("deleteArticle".equalsIgnoreCase(action)) {
            handleDelete(req, resp, currentUser);
        } else if ("updateArticle".equalsIgnoreCase(action)) {
            handleUpdate(req, resp, currentUser);
        } else if ("like".equalsIgnoreCase(action)) {
            handleLike(req, resp, currentUser);
        }
        // 🔥 回归：查询个人文章列表 (移回 POST)
        else if ("loadArticleList".equalsIgnoreCase(action)) {
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

        // 1. 首页文章列表 (开放)
        if ("loadIndexArticleList".equalsIgnoreCase(action)) {
            handleIndexArticleList(req, resp);
        }
        // 2. 分类列表 (开放)
        else if ("loadCategories".equalsIgnoreCase(action)) {
            handleLoadCategories(req, resp);
        }
        // 3. 文章详情 (开放)
        else if ("getDetail".equalsIgnoreCase(action)) {
            handleGetDetail(req, resp);
        }
        // 4. 侧边栏热榜 (开放)
        else if ("loadHotArticles".equalsIgnoreCase(action)) {
            handleLoadHotArticles(req, resp);
        }
        // 5. 检查点赞状态 (需Session但属只读，放在GET也行，这里通过判空兼容)
        else if ("checkLikeStatus".equalsIgnoreCase(action)) {
            handleCheckLikeStatus(req, resp);
        } else {
            sendJson(resp, false, "缺失或不支持的 GET 动作: [" + action + "]", null);
        }
    }

    // ----------------------------------------------------------------
    // 处理方法实现
    // ----------------------------------------------------------------

    // 个人文章列表查询
    private void handleArticleList(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            List<Article> articles = articleService.getUserPublishedArticles(currentUser.getId());
            sendJson(resp, true, "获取文章列表成功", articles);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "获取文章列表失败", null);
        }
    }

    // 发布文章
    private void handlePublish(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String title = req.getParameter("title");
        String content = req.getParameter("content");
        String summary = req.getParameter("summary");
        String categoryIdStr = req.getParameter("categoryId");
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty() || categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            sendJson(resp, false, "标题、正文和分类都不能为空", null);
            return;
        }
        try {
            Long categoryId = Long.parseLong(categoryIdStr);
            Article article = new Article();
            article.setTitle(title);
            article.setContent(content);
            article.setSummary(summary);
            article.setUserId(currentUser.getId());
            article.setCategoryId(categoryId);
            boolean success = articleService.publish(article);
            if (success) {
                sendJson(resp, true, "发布成功！", null);
            } else {
                sendJson(resp, false, "发布失败，请稍后重试", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "服务器错误：" + e.getMessage(), null);
        }
    }

    // 删除文章
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

    // 更新文章
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            Article article = new Article();
            article.setId(Long.parseLong(req.getParameter("id")));
            article.setTitle(req.getParameter("title"));
            article.setContent(req.getParameter("content"));
            article.setSummary(req.getParameter("summary"));
            article.setCategoryId(Long.parseLong(req.getParameter("categoryId")));
            article.setUserId(currentUser.getId());

            boolean success = articleService.updateArticle(article);
            if (success) sendJson(resp, true, "更新成功", null);
            else sendJson(resp, false, "更新失败", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "错误", null);
        }
    }

    // 点赞
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
     * 首页文章列表查询逻辑 (GET, 开放)
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

    // 分类列表
    private void handleLoadCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Category> categories = articleService.getAllCategories();
            sendJson(resp, true, "获取成功", categories);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "失败", null);
        }
    }

    // 文章详情
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

    // 侧边栏热榜
    private void handleLoadHotArticles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Article> hots = articleService.getHotArticles();
            sendJson(resp, true, "成功", hots);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "失败", null);
        }
    }

    // 检查点赞状态
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

    // 发送 JSON 工具方法
    private void sendJson(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }
}