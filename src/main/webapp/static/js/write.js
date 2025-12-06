// src/main/webapp/static/js/write.js

// ------------------------------------------------------------------
// 🌟 路径优化：定义全局 API 路径常量
//
// 注意：如果你的项目部署名为 "mypen"，并且 Servlet 映射为 "/article"，
// 那么完整的路径就是 /mypen/article。我们这里定义 /mypen/article
// ------------------------------------------------------------------
const ARTICLE_API_URL = "/mypen/article";


// 获取 URL 中的 ID (如果是编辑模式，ID会存在)
const urlParams = new URLSearchParams(window.location.search);
const editArticleId = urlParams.get('id');

$(document).ready(function() {
    // 1. 安全检查：检查是否登录
    const user = localStorage.getItem("user");
    if (!user) {
        alert("请先登录才能发布/编辑文章！");
        window.location.href = "login.html";
        return;
    }

    // 2. 加载分类数据 (必须先加载，编辑时才能选中值)
    loadCategories();

    // 3. 模式初始化与数据加载
    if (editArticleId) {
        // 编辑模式：修改按钮文本，加载文章详情
        $("#btn-publish").text("更新文章").attr("id", "btn-update");
        loadArticleForEdit(editArticleId);

        // 绑定更新事件
        $("#btn-update").click(function() {
            updateArticle(editArticleId);
        });
    } else {
        // 新建模式：绑定发布事件
        $("#btn-publish").click(function() {
            publishArticle();
        });
    }
});

// ------------------------------------------------------------------
// 核心逻辑函数
// ------------------------------------------------------------------

/**
 * 🌟 新增功能：加载文章详情并填充表单 (编辑模式)
 */
function loadArticleForEdit(id) {
    $.ajax({
        // ✅ 优化点：使用常量路径
        url: ARTICLE_API_URL,
        type: "GET",
        data: {
            action: "getDetail",
            id: id
        },
        dataType: "json",
        success: function(resp) {
            if (resp.success && resp.data) {
                const article = resp.data;
                $("#title").val(article.title);
                $("#content").val(article.content);

                // 延时设置选中值，确保 options 已加载
                // 确保 loadCategories 函数先跑完，再设置选中值
                setTimeout(() => {
                    $("#category-select").val(article.categoryId);
                }, 100);

            } else {
                alert("无法加载文章详情: " + resp.message);
                window.location.href = "center.html";
            }
        },
        error: function(xhr) {
            console.error("加载详情网络错误:", xhr.statusText);
            alert("加载文章详情失败，请检查服务器。");
            window.location.href = "center.html";
        }
    });
}

/**
 * 🌟 新增功能：更新文章逻辑 (POST)
 */
function updateArticle(id) {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();
    const categoryIdVal = $("#category-select").val();

    // 校验
    if (!titleVal || !contentVal || !categoryIdVal) {
        alert("标题、内容和分类都不能为空！");
        return;
    }

    // 发送 AJAX 更新请求
    $.ajax({
        // ✅ 优化点：使用常量路径
        url: ARTICLE_API_URL,
        type: "POST",
        data: {
            action: 'updateArticle',
            id: id,
            title: titleVal,
            content: contentVal,
            summary: contentVal.substring(0, 100),
            categoryId: categoryIdVal
        },
        dataType: "json",
        success: function(resp) {
            if (resp.success) {
                alert("🎉 文章更新成功！");
                window.location.href = "center.html";
            } else {
                alert("❌ " + resp.message);
            }
        },
        error: function(xhr) {
            console.error(xhr);
            alert("更新失败，网络或服务器错误");
        }
    });
}

/**
 * 核心发布逻辑 (新建文章)
 */
function publishArticle() {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();
    const categoryIdVal = $("#category-select").val(); // 获取选中的分类ID

    if (!titleVal || !contentVal) {
        alert("标题和内容不能为空");
        return;
    }

    // 校验：分类是否选择
    if (!categoryIdVal) {
        alert("请选择一个文章分类！");
        return;
    }

    // 3. 发送 AJAX 请求
    $.ajax({
        // ✅ 优化点：使用常量路径
        url: ARTICLE_API_URL,
        type: "POST",
        data: {
            action: 'publishArticle',
            title: titleVal,
            content: contentVal,
            summary: contentVal.substring(0, 100),
            categoryId: categoryIdVal
        },
        dataType: "json",
        success: function(resp) {
            if (resp.success) {
                alert("🎉 发布成功！");
                window.location.href = "index.html";
            } else {
                alert("❌ " + resp.message);
                if (resp.message.includes("登录")) {
                    window.location.href = "login.html";
                }
            }
        },
        error: function(xhr) {
            console.error(xhr);
            alert("发布失败，网络或服务器错误");
        }
    });
}


/**
 * 🌟 开放功能：通过 AJAX 加载分类列表
 */
function loadCategories() {
    $.ajax({
        // ✅ 优化点：使用常量路径
        url: ARTICLE_API_URL,
        type: "GET", // ✅ 使用 GET
        data: {
            action: 'loadCategories'
        },
        dataType: "json",
        success: function(resp) {
            if (resp.success && resp.data) {
                const $select = $("#category-select");
                $select.empty().append('<option value="">-- 请选择分类 --</option>');

                // 动态填充分类选项
                $.each(resp.data, function(index, category) {
                    $select.append(`<option value="${category.id}">${category.name}</option>`);
                });
            } else {
                console.error("加载分类列表失败:", resp.message);
            }
        },
        error: function(xhr) {
            console.error("加载分类列表网络错误:", xhr.statusText);
            $("#category-select").append('<option value="">加载失败，请检查服务器</option>');
        }
    });
}