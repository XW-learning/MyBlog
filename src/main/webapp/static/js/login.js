// src/main/webapp/static/js/login.js

// 🌟 路径优化：定义全局 API 路径常量
// 请确保这里的 /mypen/login 与你的 UserServlet 映射路径一致
const LOGIN_API_URL = "/mypen/login";

$("#btn-login").click(function() {

    let usernameVal = $("#username").val();
    let passwordVal = $("#password").val();

    if(!usernameVal || !passwordVal) {
        alert("账号和密码不能为空！");
        return;
    }

    $.ajax({
        // ✅ 优化点：使用常量路径
        url: LOGIN_API_URL,
        type: "POST",
        data: {
            username: usernameVal,
            password: passwordVal
        },
        dataType: "json",
        success: function(resp) {
            console.log("后端返回:", resp);
            if(resp.success) {
                // 存储用户信息到本地
                localStorage.setItem("user", JSON.stringify(resp.data));

                // 💡 优化路径：如果 index.html 和 login.html 在同一个 pages 目录下，
                // 直接使用 index.html 即可。
                window.location.href = "index.html";
            } else {
                alert("❌ " + resp.message);
            }
        },
        error: function(xhr) {
            // 404/500 等错误
            alert("请求失败，状态码：" + xhr.status);
        }
    });
});