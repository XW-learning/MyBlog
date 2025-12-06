// src/main/webapp/static/js/center.js

// 统一的 Servlet 路径 (假设你的 Servlet 映射是 /article)
// ⚠️ 如果你的项目部署名是 'mypen'，则路径为 /mypen/article
const API_URL = "/mypen/article";

$(document).ready(function () {
    // 1. 检查登录状态并加载文章
    const userJson = localStorage.getItem("user");
    if (!userJson) {
        handleAuthRedirect("未登录，无法访问个人中心！");
        return;
    }

    // 2. 动态显示昵称
    try {
        const user = JSON.parse(userJson);
        $(".nav-actions span").text(`欢迎您，${user.nickname || user.username}`);
    } catch (e) {
        console.error("本地用户数据解析错误", e);
        handleAuthRedirect("本地数据损坏，请重新登录。");
        return;
    }

    // 3. 加载文章列表
    loadMyArticles();

    // 4. 绑定删除事件 (使用事件委托，监听 #article-list-body 上的 .btn-delete 点击)
    $("#article-list-body").on('click', '.btn-delete', function () {
        handleDeleteArticle($(this));
    });
});

/**
 * 【优化点】统一处理权限不足和错误跳转
 */
function handleAuthRedirect(message) {
    alert("❌ " + message);
    localStorage.removeItem("user");
    window.location.href = "login.html";
}


// -------------------------------------------------------------
// 核心功能函数
// -------------------------------------------------------------

/**
 * 通过 AJAX 从 Servlet 获取当前用户的文章列表
 */
function loadMyArticles() {
    const $tableBody = $("#article-list-body");
    $tableBody.html('<tr><td colspan="4">加载中...</td></tr>');

    $.ajax({
        url: API_URL,
        type: "POST", // 个人中心列表查询需登录，使用 POST
        data: {
            action: 'loadArticleList'
        },
        dataType: "json",
        success: function (resp) {
            $tableBody.empty();

            if (resp.success && resp.data && resp.data.length > 0) {
                // 渲染数据
                $.each(resp.data, function (index, article) {
                    const row = `
                        <tr>
                            <td><a href="article_detail.html?id=${article.id}">${article.title}</a></td>
                            <td>${new Date(article.createTime).toLocaleDateString()}</td>
                            <td>${article.views} / ${article.likes}</td>
                            <td>
                                <a href="write.html?id=${article.id}" class="btn-text">编辑</a>
                                <button class="btn-text danger btn-delete" data-id="${article.id}">删除</button>
                            </td>
                        </tr>
                    `;
                    $tableBody.append(row);
                });
            } else if (resp.success) {
                // 列表为空
                $tableBody.html('<tr><td colspan="4">您还没有发布任何文章。</td></tr>');
            } else {
                // 登录过期或后端校验失败
                handleAuthRedirect(resp.message);
            }
        },
        error: function (xhr) {
            // 统一处理网络和服务器错误
            $tableBody.html('<tr><td colspan="4">加载失败，请检查服务器连接或权限。</td></tr>');
            console.error("加载文章列表失败", xhr);
        }
    });
}

/**
 * 处理文章删除逻辑
 * @param $button 当前点击的删除按钮 jQuery 对象
 */
function handleDeleteArticle($button) {
    const articleId = $button.data('id');
    const $row = $button.closest('tr');

    if (confirm(`确定要删除文章ID: ${articleId} 吗？`)) {
        $.ajax({
            url: API_URL,
            type: "POST",
            data: {
                action: "deleteArticle",
                id: articleId
            },
            dataType: "json",
            success: function (resp) {
                if (resp.success) {
                    alert("✅ " + resp.message);
                    // 移除该行，带淡出效果
                    $row.fadeOut(500, function () {
                        $row.remove();
                    });
                } else {
                    // 删除失败通常是权限或文章不存在
                    alert("❌ 删除失败: " + resp.message);
                    if (resp.message.includes("权") || resp.message.includes("登录")) {
                        handleAuthRedirect("权限不足或登录失效。");
                    }
                }
            },
            error: function (xhr) {
                alert("网络错误，无法删除。");
            }
        });
    }
}