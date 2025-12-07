// src/main/webapp/static/js/profile.js

// 假设更新用户信息的接口 (暂时还没有，先写上)
const USER_API_URL = "/mypen/user";

$(document).ready(function () {
    // 1. 检查登录
    const userJson = localStorage.getItem("user");
    if (!userJson) {
        alert("请先登录！");
        window.location.href = "login.html";
        return;
    }

    // 2. 回显数据
    try {
        const user = JSON.parse(userJson);

        // 顶部导航
        $(".nav-actions span").text("欢迎您，" + (user.nickname || user.username));

        // 页面头部
        $("#profile-nickname").text(user.nickname || user.username);
        if (user.avatar) {
            $("#profile-avatar").attr("src", user.avatar);
        }

        // 表单回显
        $("#input-nickname").val(user.nickname || "");
        $("#text-id").text(user.id); // ID 通常不可修改
        $("#input-email").val(user.email || "");

        // 如果你的 User 对象有 bio 或 gender，也可以在这里回显
        // $("#input-bio").val(user.bio);

    } catch (e) {
        console.error("数据解析失败", e);
    }

    // 3. 绑定保存按钮事件
    $("#btn-save-profile").click(function() {
        const newNickname = $("#input-nickname").val().trim();
        const newEmail = $("#input-email").val().trim();
        const newBio = $("#input-bio").val().trim();

        if (!newNickname) {
            alert("昵称不能为空");
            return;
        }

        // --- 模拟保存逻辑 (因为后端还没有 updateUserInfo 接口) ---
        // 真实开发中，这里应该用 $.ajax 发送 POST 请求到后端

        const currentUser = JSON.parse(localStorage.getItem("user"));
        currentUser.nickname = newNickname;
        currentUser.email = newEmail;
        // currentUser.bio = newBio;

        // 更新本地缓存，模拟“保存成功”
        localStorage.setItem("user", JSON.stringify(currentUser));

        alert("✅ 保存成功！(演示效果，仅更新本地缓存)");

        // 刷新页面显示新数据
        window.location.reload();
    });
});