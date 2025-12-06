package com.personalblog.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/logout")
public class UserLogoutServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // 1. 获取当前 Session (getSession(false) 表示如果 Session 不存在，则返回 null)
            HttpSession session = req.getSession(false);

            // 2. 核心操作：如果 Session 存在，则销毁它
            if (session != null) {
                session.invalidate(); // 销毁 Session，清除所有存储的用户状态
            }

            // 3. 无论 Session 是否存在，都返回成功状态
            sendJson(resp, true, "退出成功", null);

        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "服务器内部错误，退出失败", null);
        }
    }

    // 登出操作不应通过 URL GET 方式访问
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(405, "GET method not supported for logout.");
    }

    // -------------------------------------------------------------
    // UTILITY METHOD (保持与其它 Servlets 一致的 JSON 响应格式)
    // -------------------------------------------------------------
    private void sendJson(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }
}