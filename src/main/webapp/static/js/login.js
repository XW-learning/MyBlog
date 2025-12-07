// src/main/webapp/static/js/login.js

const LOGIN_API_URL = "/mypen/login";

function performLogin() {
    let usernameVal = $("#username").val().trim();
    let passwordVal = $("#password").val();

    if (!usernameVal || !passwordVal) {
        showModal("请填写所有必填项！");
        return;
    }

    const nonAsciiPattern = /[^\x00-\x7F]/;
    if (nonAsciiPattern.test(passwordVal)) {
        showModal("❌ 密码不能包含中文或特殊符号，请使用英文、数字或常见符号。");
        return;
    }

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
                // 🔥 修改：跳转逻辑放入回调
                // 只有用户在模态框点“确定”后，才会执行这个函数
                /* showModal("✅ 登录成功！", function() {
                     window.location.href = "index.html";
                 });*/
                // 既然是登录，通常直接跳也可以，但为了体验一致：
                window.location.href = "index.html"; // 登录成功通常不需要弹窗确认，直接跳更流畅
            } else {
                showModal("❌ " + resp.message);
                $btn.prop("disabled", false).text("立即登录");
            }
        },
        error: function (xhr) {
            showModal("请求失败，状态码：" + xhr.status);
            $btn.prop("disabled", false).text("立即登录");
        }
    });
}

$(document).ready(function () {
    $("#btn-login").click(function () {
        performLogin();
    });

    $("#username, #password").on("keyup", function (event) {
        if (event.keyCode === 13) {
            performLogin();
            event.preventDefault();
        }
    });
});