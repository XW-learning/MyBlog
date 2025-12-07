// src/main/webapp/static/js/login.js

const LOGIN_API_URL = "/mypen/login";

// 提取核心登录逻辑为一个函数，方便按钮和回车调用
function performLogin() {
    let usernameVal = $("#username").val().trim();
    let passwordVal = $("#password").val();

    if (!usernameVal || !passwordVal) {
        alert("账号和密码不能为空！");
        return;
    }

    // 检查密码是否包含非 ASCII 字符 (如中文)
    const nonAsciiPattern = /[^\x00-\x7F]/;
    if (nonAsciiPattern.test(passwordVal)) {
        alert("❌ 密码不能包含中文或特殊符号，请使用英文、数字或常见符号。");
        return;
    }

    // 禁用按钮，防止重复提交
    const $btn = $("#btn-login");
    $btn.prop("disabled", true).text("登录中...");

    $.ajax({
        url: LOGIN_API_URL,
        type: "POST",
        data: {
            username: usernameVal,
            password: passwordVal
        },
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                localStorage.setItem("user", JSON.stringify(resp.data));
                window.location.href = "index.html";
            } else {
                alert("❌ " + resp.message);
                $btn.prop("disabled", false).text("立即登录"); // 失败后恢复按钮
            }
        },
        error: function (xhr) {
            alert("请求失败，状态码：" + xhr.status);
            $btn.prop("disabled", false).text("立即登录"); // 失败后恢复按钮
        }
    });
}

$(document).ready(function () {

    // 1. 按钮点击事件：调用核心登录函数
    $("#btn-login").click(function () {
        performLogin();
    });

    // 2. 🔥 新增：键盘回车事件绑定
    // 监听用户名和密码输入框的按键抬起事件
    $("#username, #password").on("keyup", function (event) {
        // keyCode 13 代表回车键
        if (event.keyCode === 13) {
            performLogin();
            // 阻止默认行为（比如提交表单或页面跳转）
            event.preventDefault();
        }
    });
});