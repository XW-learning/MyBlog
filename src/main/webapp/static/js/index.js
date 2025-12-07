// src/main/webapp/static/js/app/index.js

// ------------------------------------------------------------------
// 🌟 路径优化：定义全局 API 路径常量
// ------------------------------------------------------------------
const ARTICLE_API_URL = "/mypen/article";
const USER_API_URL = "/mypen/logout";


// ----------------------------------------------------
// 初始化与缓存恢复逻辑
// ----------------------------------------------------
function initializeHomePage() {
    // 1. 检查登录状态
    checkLoginStatus();

    // 2. 加载文章列表 (默认加载“推荐”或“最新”)
    //    这里不传参数，loadArticleList 内部会自动找当前高亮的标签
    loadArticleList();

    // 3. 绑定“写博客”按钮事件
    bindWriteButtonEvent();

    // 4. 加载热门文章 (侧边栏)
    loadHotArticles();

    // 5. 🔥 新增：绑定筛选条点击事件 (推荐/最新/热榜)
    bindFilterEvent();
}


$(document).ready(function () {
    initializeHomePage();
});

// 监听 BFCache 恢复 (后退按钮)
window.addEventListener('pageshow', function (event) {
    if (event.persisted) {
        console.log("页面从缓存恢复，强制更新数据...");
        initializeHomePage();
    }
});


// ------------------------------------------------------------------
// 核心功能函数
// ------------------------------------------------------------------

/**
 * 🔥 新增：绑定筛选条点击事件
 */
function bindFilterEvent() {
    $(".filter-bar .filter-item").click(function() {
        // 1. 切换高亮样式
        $(this).addClass("active").siblings().removeClass("active");

        // 2. 获取排序类型 (data-sort属性)
        const sortType = $(this).data("sort");

        // 3. 重新加载列表
        loadArticleList(sortType);
    });
}

/**
 * 加载文章列表 (支持排序)
 * @param sortType 排序类型 (recommend, new, hot)
 */
function loadArticleList(sortType) {
    // 如果未传入参数，则尝试获取当前高亮的标签 data-sort，默认为 'new'
    if (!sortType) {
        sortType = $(".filter-bar .active").data("sort") || "new";
    }

    const $listContainer = $("#article-list");
    $listContainer.html('<p style="padding: 20px; color: #999;">正在加载文章...</p>');

    $.ajax({
        url: ARTICLE_API_URL,
        type: "GET",
        data: {
            action: 'loadIndexArticleList',
            sort: sortType // 🔥 关键：将排序参数传给 Servlet
        },
        dataType: "json",
        success: function (resp) {
            $listContainer.empty();

            if (resp.success && resp.data && resp.data.length > 0) {
                $.each(resp.data, function (index, article) {

                    let authorName = article.authorNickname || "匿名博主";

                    // 🔥 仅在“热榜”模式下显示红色排名数字
                    let rankHtml = "";
                    if (sortType === 'hot') {
                        // 前三名加红，后面普通色
                        let color = index < 3 ? "#ff4d4f" : "#999";
                        rankHtml = `<span style="color:${color}; font-weight:bold; margin-right:8px; font-size:16px;">${index + 1}.</span>`;
                    }

                    let html = `
                        <div class="article-item">
                            <h2>
                                ${rankHtml} <a href="article_detail.html?id=${article.id}">${article.title}</a>
                            </h2>
                            <p class="article-summary">${article.summary}</p>
                            <div class="article-meta">
                                <span>👤 ${authorName}</span>  
                                <span>👁️ ${article.views}</span>
                                <span>👍 ${article.likes}</span>
                                <span>📅 ${new Date(article.createTime).toLocaleDateString()}</span>
                            </div>
                        </div>
                    `;
                    $listContainer.append(html);
                });
            } else if (resp.success) {
                $listContainer.html('<p style="padding: 20px;">暂无数据</p>');
            } else {
                $listContainer.html('<p style="padding: 20px; color: red;">加载失败: ' + resp.message + '</p>');
            }
        },
        error: function (xhr) {
            console.error(xhr);
            $listContainer.html('<p style="padding: 20px; color: red;">网络错误，无法连接服务器。</p>');
        }
    });
}

/**
 * 检查登录状态
 */
function checkLoginStatus() {
    const userJson = localStorage.getItem("user");

    if (userJson) {
        try {
            const user = JSON.parse(userJson);
            const $navActions = $(".nav-actions");

            let loggedInHtml = `
                <span class="nav-username">您好，${user.nickname || user.username}</span>
                <a href="center.html" class="btn-user-center">个人中心</a>
                <button class="btn-write" id="btn-write-article">🖊️ 写博客</button>  
                <a href="javascript:void(0)" id="btn-logout" class="btn-logout">退出登录</a>
            `;

            $navActions.empty().append(loggedInHtml);

            $("#btn-logout").click(function () {
                handleLogout();
            });

        } catch (e) {
            console.error("解析用户数据失败:", e);
            localStorage.removeItem("user");
        }
    }
}

/**
 * 退出登录
 */
function handleLogout() {
    // 🔥 [核心修改] 将原生 confirm 替换为自定义 showConfirm
    showConfirm(
        "确定要退出登录吗？",
        function() {
            $.ajax({
                url: USER_API_URL,
                type: "POST",
                data: { action: 'logout' },
                success: function (resp) {
                    localStorage.removeItem("user");
                    // 退出成功后提示，点击确定再刷新
                    showModal("您已安全退出！", function() {
                        window.location.reload();
                    });
                },
                error: function () {
                    // 即使请求失败（服务器错误），也要清除本地信息并刷新
                    localStorage.removeItem("user");
                    window.location.reload();
                }
            });
        },
        // onCancel (用户点击“取消”)：不执行任何操作，直接关闭模态框
        null
    );
}

/**
 * 写文章按钮事件
 */
function bindWriteButtonEvent() {
    $(document).on("click", "#btn-write-article", function () {
        const user = localStorage.getItem("user");
        if (user) {
            window.location.href = "write.html";
        } else {
            // 🔥 修改：未登录提示后，点击确定跳转登录页
            showModal("请先登录才能发布文章哦！", function() {
                window.location.href = "login.html";
            });
        }
    });
}

/**
 * 加载侧边栏热榜
 */
function loadHotArticles() {
    const $hotList = $(".hot-list ul");
    $.ajax({
        url: ARTICLE_API_URL,
        type: "GET",
        data: { action: "loadHotArticles" },
        dataType: "json",
        success: function(resp) {
            if (resp.success && resp.data) {
                $hotList.empty();
                $.each(resp.data, function(index, article) {
                    let li = `<li><a href="article_detail.html?id=${article.id}">${index+1}. ${article.title} (${article.likes}赞)</a></li>`;
                    $hotList.append(li);
                });
            }
        }
    });
}