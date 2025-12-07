// src/main/webapp/static/js/register.js

const REGISTER_API_URL = "/mypen/register";

$(document).ready(function () {

    $("#btn-register").click(function () {
        // ... (前面的校验代码不变，showModal 不需要回调) ...
        const username = $("#username").val().trim();
        const nickname = $("#nickname").val().trim();
        const email = $("#email").val().trim();
        const password = $("#password").val();
        const confirmPassword = $("#confirm_password").val();

        if (!username || !nickname || !email || !password) {
            showModal("请填写所有必填项！");
            return;
        }

        if (password !== confirmPassword) {
            showModal("❌ 两次输入的密码不一致，请检查！");
            return;
        }

        const nonAsciiPattern = /[^\x00-\x7F]/;
        if (nonAsciiPattern.test(password) || nonAsciiPattern.test(confirmPassword)) {
            showModal("❌ 密码不能包含中文或特殊符号，请使用英文、数字或常见符号。");
            return;
        }

        const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        if (!emailPattern.test(email)) {
            showModal("❌ 电子邮箱格式不正确！");
            return;
        }

        const $btn = $(this);
        $btn.prop("disabled", true).text("注册中...");

        $.ajax({
            url: REGISTER_API_URL,
            type: "POST",
            data: {
                action: "register",
                username, nickname, email, password
            },
            dataType: "json",
            success: function (resp) {
                if (resp.success) {
                    // 🔥 修改：注册成功后，等待用户点击确定再跳转
                    showModal("✅ 注册成功！点击确定前往登录页面...", function() {
                        window.location.href = "login.html";
                    });
                } else {
                    showModal("❌ 注册失败: " + resp.message);
                }
            },
            error: function (xhr) {
                showModal("服务器错误 (Status: " + xhr.status + ")");
            },
            complete: function () {
                $btn.prop("disabled", false).text("立即注册");
            }
        });
    });
});