package com.personalblog.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.personalblog.model.Comment;
import com.personalblog.model.User;
import com.personalblog.service.CommentService;
import com.personalblog.service.impl.CommentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(value = "/comment")
public class CommentServlet extends HttpServlet {

    private final CommentService commentService = new CommentServiceImpl();
    // 配置 Gson 以处理日期格式
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");

        User currentUser = (User) req.getSession().getAttribute("currentUser");

        // --- 1. 发布评论 (需登录) ---
        if ("publish".equalsIgnoreCase(action)) {
            if (currentUser == null) {
                sendJson(resp, false, "请先登录", null);
                return;
            }
            handlePublish(req, resp, currentUser);
        }
        // --- 2. 删除评论 (需登录) [新增部分] ---
        else if ("delete".equalsIgnoreCase(action)) {
            if (currentUser == null) {
                sendJson(resp, false, "请先登录", null);
                return;
            }
            // 删除评论
            handleDelete(req, resp, currentUser);
        }
        else {
            sendJson(resp, false, "不支持的 POST 动作: " + action, null);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");

        // 获取评论列表 (无需登录)
        if ("list".equalsIgnoreCase(action)) {
            handleList(req, resp);
        } else {
            sendJson(resp, false, "comment不支持的 GET 动作: " + action, null);
        }
    }

    // ---------------------------------------------------------
    // 核心处理方法
    // ---------------------------------------------------------

    /**
     * 处理获取评论列表
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String articleIdStr = req.getParameter("articleId");

        if (articleIdStr == null || articleIdStr.isEmpty()) {
            sendJson(resp, false, "缺少文章ID参数", null);
            return;
        }

        try {
            Long articleId = Long.parseLong(articleIdStr);
            // 调用 Service 查询
            List<Comment> comments = commentService.getCommentsByArticle(articleId);
            sendJson(resp, true, "获取评论成功", comments);
        } catch (NumberFormatException e) {
            sendJson(resp, false, "文章ID格式错误", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "获取评论失败", null);
        }
    }

    /**
     * 处理发表评论
     */
    private void handlePublish(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String articleIdStr = req.getParameter("articleId");
        String content = req.getParameter("content");
        String parentIdStr = req.getParameter("parentId");

        if (articleIdStr == null || content == null || content.trim().isEmpty()) {
            sendJson(resp, false, "参数不完整", null);
            return;
        }

        try {
            Comment comment = new Comment();
            comment.setArticleId(Long.parseLong(articleIdStr));
            comment.setUserId(user.getId());
            comment.setContent(content);

            if (parentIdStr != null && !parentIdStr.isEmpty() && !"null".equals(parentIdStr)) {
                comment.setParentId(Long.parseLong(parentIdStr));
            }

            boolean success = commentService.publishComment(comment);
            if (success) {
                sendJson(resp, true, "评论成功", null);
            } else {
                sendJson(resp, false, "评论失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "系统错误", null);
        }
    }

    /**
     * [新增] 处理删除评论
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String commentIdStr = req.getParameter("commentId");

        if (commentIdStr == null || commentIdStr.isEmpty()) {
            sendJson(resp, false, "缺少评论ID", null);
            return;
        }

        try {
            // 获取评论ID
            Long commentId = Long.parseLong(commentIdStr);

            // 1. 先查询评论是否存在
            Comment comment = commentService.getCommentById(commentId);
            if (comment == null) {
                sendJson(resp, false, "评论不存在或已被删除", null);
                return;
            }

            // 2. 权限校验 (核心逻辑)
            // 规则：只有 "评论的作者" 或者 "管理员(ID=1)" 可以删除
            // 注意：Long类型比较建议用 equals，或者拆箱对比
            boolean isOwner = user.getId().equals(comment.getUserId());
            boolean isAdmin = user.getId() == 1L; // 假设 ID为1 的是管理员

            if (!isOwner && !isAdmin) {
                // 如果既不是作者，也不是管理员，直接拒绝
                sendJson(resp, false, "你无权删除这条评论", null);
                return;
            }

            // 3. 校验通过，执行删除
            boolean success = commentService.deleteComment(commentId);

            if (success) {
                sendJson(resp, true, "删除成功", null);
            } else {
                sendJson(resp, false, "删除失败，系统异常", null);
            }

        } catch (NumberFormatException e) {
            sendJson(resp, false, "ID格式错误", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "系统错误", null);
        }
    }

    private void sendJson(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }
}