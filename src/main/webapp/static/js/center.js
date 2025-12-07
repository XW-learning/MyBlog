// src/main/webapp/static/js/center.js

const API_URL = "/mypen/article";

// 🌟 全局变量：记录当前页码，默认为 1
let currentPage = 1;

$(document).ready(function () {
    // 1. 权限校验与用户信息渲染
    const userJson = localStorage.getItem("user");
    if (!userJson) {
        handleAuthRedirect("未登录，无法访问个人中心！");
        return;
    }

    try {
        const user = JSON.parse(userJson);

        // 填充顶部导航
        $(".nav-actions").html(`
            <span class="nav-username">欢迎您，${user.nickname || user.username}</span>
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
        if (user.createTime) $("#center-join-time").text("加入于: " + new Date(user.createTime).toLocaleDateString());

        // 绑定退出事件
        $("#btn-logout").click(function () {
            localStorage.removeItem("user");
            window.location.href = "index.html";
        });

    } catch (e) {
        console.error(e);
        localStorage.removeItem("user");
        window.location.href = "login.html";
        return;
    }

    // 2. 初始加载文章列表 (加载当前全局页码)
    loadMyArticles(currentPage);

    // 3. 绑定删除事件委托
    $("#article-list-container").on('click', '.btn-delete', function () {
        handleDeleteArticle($(this));
    });

    // 4. 绑定分页点击事件
    $("#pagination-container").on('click', '.page-btn', function () {
        // 如果是禁用状态或当前页，不处理
        if ($(this).hasClass('disabled') || $(this).hasClass('active')) return;

        const newPage = $(this).data('page');
        if (newPage) {
            // 切换页面后，自动滚动到列表顶部
            $('.profile-main').get(0).scrollIntoView({behavior: 'smooth'});
            loadMyArticles(newPage);
        }
    });
});

function handleAuthRedirect(message) {
    alert("❌ " + message);
    localStorage.removeItem("user");
    window.location.href = "login.html";
}

/**
 * 加载指定页码的文章列表
 */
function loadMyArticles(page) {
    // 更新全局页码
    currentPage = page;

    const $container = $("#article-list-container");
    $container.html('<p style="padding:20px; text-align:center;">加载中...</p>');

    $.ajax({
        url: API_URL, type: "POST", data: {
            action: 'loadArticleList', pageNum: page  // 传递页码给后端
        }, dataType: "json", success: function (resp) {
            $container.empty();

            if (resp.success) {
                const data = resp.data; // 后端返回的Map结构
                const articles = data.articles;
                const totalPages = data.totalPages;

                // --- 🔥 核心修复：更新左侧统计数据 ---
                // 这里不再通过前端累加，而是直接显示后端计算好的总数
                $("#total-articles").text(data.totalCount || 0);
                $("#total-views").text(data.totalViews || 0);
                $("#total-likes").text(data.totalLikes || 0);
                // -------------------------------------

                if (articles && articles.length > 0) {
                    // 1. 渲染文章列表
                    $.each(articles, function (index, article) {
                        let statusBadge = article.status === 1 ? '<span class="status-badge status-published">已发布</span>' : '<span class="status-badge status-draft">草稿</span>';

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

                    // 2. 渲染分页条
                    renderPagination(totalPages, currentPage);

                } else {
                    $container.html('<div style="text-align:center; padding:40px; color:#999;">您还没有发布过文章，快去创作吧！</div>');
                    $("#pagination-container").empty();
                }
            } else {
                if (resp.message && resp.message.includes("登录")) {
                    alert("登录已过期");
                    window.location.href = "login.html";
                } else {
                    $container.html(`<p style="color:red;padding:20px;text-align:center;">${resp.message}</p>`);
                }
            }
        }, error: function () {
            $container.html('<p style="padding:20px; color:red; text-align:center;">网络错误，请刷新重试</p>');
        }
    });
}

/**
 * 渲染分页控件
 * @param totalPages 总页数
 * @param current 当前页码
 */
function renderPagination(totalPages, current) {
    const $box = $("#pagination-container");
    $box.empty();

    if (totalPages <= 1) return; // 只有1页就不显示分页条

    // 上一页
    if (current > 1) {
        $box.append(`<span class="page-btn" data-page="${current - 1}">« 上一页</span>`);
    } else {
        $box.append(`<span class="page-btn disabled">« 上一页</span>`);
    }

    // 智能显示页码 (防止页码过多)
    // 逻辑：始终显示第一页、最后一页、当前页附近的页码
    const delta = 2; // 当前页前后显示的页码数
    const range = [];
    const rangeWithDots = [];

    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= current - delta && i <= current + delta)) {
            range.push(i);
        }
    }

    let l;
    for (let i of range) {
        if (l) {
            if (i - l === 2) {
                rangeWithDots.push(l + 1);
            } else if (i - l !== 1) {
                rangeWithDots.push('...');
            }
        }
        rangeWithDots.push(i);
        l = i;
    }

    // 渲染页码按钮
    rangeWithDots.forEach(page => {
        if (page === '...') {
            $box.append(`<span class="page-btn disabled">...</span>`);
        } else {
            if (page === current) {
                $box.append(`<span class="page-btn active">${page}</span>`);
            } else {
                $box.append(`<span class="page-btn" data-page="${page}">${page}</span>`);
            }
        }
    });

    // 下一页
    if (current < totalPages) {
        $box.append(`<span class="page-btn" data-page="${current + 1}">下一页 »</span>`);
    } else {
        $box.append(`<span class="page-btn disabled">下一页 »</span>`);
    }
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
                        // 删除成功后重新加载当前页，确保列表和统计数据刷新
                        loadMyArticles(currentPage);
                    });
                } else {
                    alert("❌ 删除失败: " + resp.message);
                }
            }
        });
    }
}