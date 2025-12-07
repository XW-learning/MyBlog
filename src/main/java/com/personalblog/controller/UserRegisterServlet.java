package com.personalblog.controller;

import com.google.gson.Gson;
import com.personalblog.model.User;
import com.personalblog.service.UserService;
import com.personalblog.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/register")
public class UserRegisterServlet extends HttpServlet {
    private final UserService userService = new UserServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 设置编码和响应类型
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 设置编码和响应类型
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 获取action
        String action = req.getParameter("action");
        if ("register".equals(action)) {
            handleRegister(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取表单数据
        String username = req.getParameter("username");
        String nickname = req.getParameter("nickname");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // 后端非空校验
        if (username == null || username.trim().isEmpty() || nickname == null || nickname.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendJson(resp, false, "请填写完整的注册信息");
            return; // 添加return避免继续执行
        }

        // 检查用户名是否已存在
        if(userService.isUsernameExist(username)){
            sendJson(resp, false, "用户名已存在");
            return;
        }

        // 创建user对象
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(password);

        // 调用service
        boolean success = userService.register(user);
        if(success){
            sendJson(resp, true, "注册成功");
        } else {
            sendJson(resp, false, "注册失败，请稍后重试");
        }
    }


    // 辅助发送 JSON
    private void sendJson(HttpServletResponse resp, boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }
}
