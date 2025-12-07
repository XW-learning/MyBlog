// src/main/webapp/static/js/register.js

// 定义注册接口路径 (稍后我们在后端创建)
const REGISTER_API_URL = "/mypen/register";

$(document).ready(function () {
    $("#btn-register").click(function () {
        // 1. 获取表单数据
        const username = $("#username").val().trim();
        const nickname = $("#nickname").val().trim();
        const email = $("#email").val().trim();
        const password = $("#password").val();
        const confirmPassword = $("#confirm_password").val();

        // 2. 前端基础校验
        if (!username || !nickname || !email || !password) {
            alert("请填写所有必填项！");
            return;
        }

        if (password !== confirmPassword) {
            alert("❌ 两次输入的密码不一致，请检查！");
            return;
        }

        // 简单的邮箱格式校验 (可选)
        const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        if (!emailPattern.test(email)) {
            alert("❌ 电子邮箱格式不正确！");
            return;
        }

        // 3. 发送 AJAX 请求
        // 按钮置灰防止重复点击
        const $btn = $(this);
        $btn.prop("disabled", true).text("注册中...");

        $.ajax({
            url: REGISTER_API_URL,
            type: "POST",
            data: {
                action: "register", // 明确告诉 Servlet 这是一个注册动作
                username: username,
                nickname: nickname,
                email: email,
                password: password
            },
            dataType: "json",
            success: function (resp) {
                if (resp.success) {
                    alert("✅ 注册成功！即将跳转到登录页面...");
                    window.location.href = "login.html";
                } else {
                    alert("❌ 注册失败: " + resp.message);
                }
            },
            error: function (xhr) {
                alert("请求失败，服务器错误 (Status: " + xhr.status + ")");
            },
            complete: function () {
                // 恢复按钮状态
                $btn.prop("disabled", false).text("立即注册");
            }
        });
    });
});