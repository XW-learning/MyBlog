// src/main/webapp/static/js/center.js

const API_URL = "/mypen/article";

$(document).ready(function () {
    // 1. 权限校验与用户信息渲染
    const userJson = localStorage.getItem("user");
    if (!userJson) {
        handleAuthRedirect("未登录，无法访问个人中心！"); // 确保未登录直接跳转
        return;
    }

    try {
        const user = JSON.parse(userJson);

        // 填充顶部导航
        $(".nav-actions").html(`
            <span class="nav-username">欢迎您，${user.nickname}</span>
            <a href="javascript:void(0)" id="btn-logout" class="btn-logout">退出</a>
        `);

        // 处理头像占位和加载
        const $avatarPlaceholder = $("#center-avatar-placeholder");
        if (user.avatar) {
            // 如果有头像 URL，动态创建 img 标签并替换占位符
            $avatarPlaceholder.replaceWith(`<img src="${user.avatar}" class="user-avatar-big" id="center-avatar" onerror="this.onerror=null; this.src='../static/img/default-avatar.png';">`);
        }

        // 填充个人中心大 Banner 信息
        $("#center-nickname").text(user.nickname || user.username);
        $("#center-id").text("ID: " + user.id);
        if(user.createTime) $("#center-join-time").text("加入于: " + new Date(user.createTime).toLocaleDateString());

        // 绑定退出事件
        $("#btn-logout").click(function(){
            // 我们简化了退出逻辑，直接清除本地缓存并刷新页面
            localStorage.removeItem("user");
            window.location.href = "index.html";
        });

    } catch (e) {
        console.error(e);
        localStorage.removeItem("user");
        window.location.href = "login.html";
        return;
    }

    // 2. 加载文章列表
    loadMyArticles();

    // 3. 绑定删除事件委托
    $("#article-list-container").on('click', '.btn-delete', function () {
        handleDeleteArticle($(this));
    });
});

function handleAuthRedirect(message) {
    alert("❌ " + message);
    localStorage.removeItem("user");
    window.location.href = "login.html";
}

function loadMyArticles() {
    const $container = $("#article-list-container");
    $container.html('<p style="padding:20px;">加载中...</p>');

    $.ajax({
        url: API_URL,
        type: "POST",
        data: { action: 'loadArticleList' },
        dataType: "json",
        success: function (resp) {
            // <-- 修改在这里：增强错误处理，确保会话过期能被捕获
            if (!resp.success && resp.message && resp.message.includes("登录")) {
                handleAuthRedirect(resp.message);
                return;
            }

            $container.empty();

            if (resp.success && resp.data && resp.data.length > 0) {
                let totalViews = 0;
                let totalLikes = 0;

                // 遍历渲染文章
                $.each(resp.data, function (index, article) {
                    // 累加数据用于左侧“个人成就”展示
                    totalViews += (article.views || 0);
                    totalLikes += (article.likes || 0);

                    // 状态徽章
                    let statusBadge = "";
                    if (article.status === 1) {
                        statusBadge = '<span class="status-badge status-published">已发布</span>';
                    } else {
                        statusBadge = '<span class="status-badge status-draft">草稿</span>';
                    }

                    // 生成 HTML (复刻 CSDN 列表样式)
                    const itemHtml = `
                        <div class="my-article-item">
                            <a href="write.html?id=${article.id}" class="my-article-title">${article.title}</a>
                            <div class="my-article-info">
                                <div class="info-left">
                                    ${statusBadge}
                                    <span>${new Date(article.createTime).toLocaleString()}</span>
                                    <span>👁️ ${article.views}</span>
                                    <span>👍 ${article.likes}</span>
                                </div>
                                <div class="action-buttons">
                                    <a href="write.html?id=${article.id}" class="btn-icon">编辑</a>
                                    <button class="btn-icon delete btn-delete" data-id="${article.id}">删除</button>
                                </div>
                            </div>
                        </div>
                    `;
                    $container.append(itemHtml);
                });

                // 更新左侧成就卡片的数据
                $("#total-articles").text(resp.data.length);
                $("#total-views").text(totalViews);
                $("#total-likes").text(totalLikes);

            } else if (resp.success) {
                // 后端返回成功，但数据为空，说明真的没有文章。
                $container.html('<div style="text-align:center; padding:40px; color:#999;">您还没有发布过文章，快去创作吧！</div>');
                $("#total-articles").text(0);
            } else {
                // 后端返回失败，但不是登录过期，可能是其他错误
                $container.html('<p style="padding:20px; color:red;">加载失败: ' + (resp.message || "未知错误") + '</p>');
            }
        },
        error: function () {
            $container.html('<p style="padding:20px; color:red;">网络错误，请刷新重试</p>');
        }
    });
}

function handleDeleteArticle($button) {
    const articleId = $button.data('id');
    // 找到该按钮所在的 .my-article-item 父容器
    const $item = $button.closest('.my-article-item');

    if (confirm(`确定要删除这篇文章吗？(ID: ${articleId})`)) {
        $.ajax({
            url: API_URL,
            type: "POST",
            data: {action: "deleteArticle", id: articleId},
            dataType: "json",
            success: function (resp) {
                if (resp.success) {
                    // 优雅的淡出动画
                    $item.fadeOut(300, function () {
                        $item.remove();
                        loadMyArticles(); // 重新加载列表以更新左侧统计数据
                    });
                } else {
                    alert("❌ 删除失败: " + resp.message);
                }
            }
        });
    }
}