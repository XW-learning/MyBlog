package com.personalblog.controller;

import com.google.gson.Gson;
import com.personalblog.model.User;
import com.personalblog.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/login")
public class UserLoginServlet extends HttpServlet {

    private final UserServiceImpl userServiceImpl = new UserServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendJson(resp, false, "账号或密码不能为空！", null);
            return;
        }
        try {
            User user = userServiceImpl.login(username, password);
            if (user != null) {
                req.getSession().setAttribute("currentUser", user);
                sendJson(resp, true, "登录成功", user);
            } else {
                sendJson(resp, false, "账号或密码错误", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(resp, false, "系统繁忙，请稍后再试", null);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(405, "GET method not supported for login");
    }

    /**
     * 💡 封装一个发 JSON 的小工具方法，避免重复写代码
     */
    private void sendJson(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }
}
