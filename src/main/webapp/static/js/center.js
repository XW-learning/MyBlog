// src/main/webapp/static/js/center.js

// 统一的 Servlet 路径 (假设你的 Servlet 映射是 /article)
// ⚠️ 如果你的项目部署名是 'mypen'，则路径为 /mypen/article
const API_URL = "/mypen/article";

$(document).ready(function () {
    const userJson = localStorage.getItem("user");
    if (!userJson) {
        handleAuthRedirect("未登录，无法访问个人中心！");
        return;
    }
    try {
        const user = JSON.parse(userJson);
        $(".nav-actions span").text(`欢迎您，${user.nickname || user.username}`);
    } catch (e) {
        handleAuthRedirect("本地数据损坏");
        return;
    }

    loadMyArticles();

    $("#article-list-body").on('click', '.btn-delete', function () {
        handleDeleteArticle($(this));
    });
});

function handleAuthRedirect(message) {
    alert("❌ " + message);
    localStorage.removeItem("user");
    window.location.href = "login.html";
}

function loadMyArticles() {
    const $tableBody = $("#article-list-body");
    $tableBody.html('<tr><td colspan="5">加载中...</td></tr>');

    $.ajax({
        url: API_URL,
        type: "POST",
        data: {
            action: 'loadArticleList' // 后端需修改此接口以返回所有状态的文章
        },
        dataType: "json",
        success: function (resp) {
            $tableBody.empty();

            if (resp.success && resp.data && resp.data.length > 0) {
                $.each(resp.data, function (index, article) {
                    // 状态判断
                    let statusHtml = "";
                    if (article.status === 1) {
                        statusHtml = '<span style="color:green;">已发布</span>';
                    } else {
                        statusHtml = '<span style="color:#999;">草稿</span>';
                    }

                    const row = `
                        <tr>
                            <td><a href="write.html?id=${article.id}">${article.title}</a></td>
                            <td>${statusHtml}</td>
                            <td>${article.createTime}</td>
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
                $tableBody.html('<tr><td colspan="5">您还没有文章。</td></tr>');
            } else {
                handleAuthRedirect(resp.message);
            }
        },
        error: function (xhr) {
            $tableBody.html('<tr><td colspan="5">加载失败。</td></tr>');
        }
    });
}

function handleDeleteArticle($button) {
    const articleId = $button.data('id');
    const $row = $button.closest('tr');
    if (confirm(`确定要删除文章ID: ${articleId} 吗？`)) {
        $.ajax({
            url: API_URL, type: "POST",
            data: {action: "deleteArticle", id: articleId},
            dataType: "json",
            success: function (resp) {
                if (resp.success) {
                    $row.fadeOut(500, function () {
                        $row.remove();
                    });
                } else {
                    alert("❌ 删除失败: " + resp.message);
                }
            }
        });
    }
}
