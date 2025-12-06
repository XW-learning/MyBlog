// src/main/webapp/static/js/write.js

// ------------------------------------------------------------------
// 🌟 路径优化：定义全局 API 路径常量
//
// 注意：如果你的项目部署名为 "mypen"，并且 Servlet 映射为 "/article"，
// 那么完整的路径就是 /mypen/article。我们这里定义 /mypen/article
// ------------------------------------------------------------------
const ARTICLE_API_URL = "/mypen/article";
const urlParams = new URLSearchParams(window.location.search);
let editArticleId = urlParams.get('id');
let currentStatus = 0;

$(document).ready(function () {
    const user = localStorage.getItem("user");
    if (!user) {
        alert("请先登录才能发布/编辑文章！");
        window.location.href = "login.html";
        return;
    }

    // 1. 优先加载分类数据
    loadCategories();

    // 2. 模式初始化 (需要在分类加载后执行，或者在加载编辑数据时处理回显)
    if (editArticleId) {
        $("#btn-publish").text("更新发布").attr("id", "btn-update");
        // 注意：loadArticleForEdit 可能会在 loadCategories 完成前就执行
        // 我们需要在 loadArticleForEdit 里处理好分类的回显
        loadArticleForEdit(editArticleId);

        $("#btn-update").click(function () {
            saveArticle(1, false);
        });
    } else {
        $("#btn-publish").click(function () {
            saveArticle(1, false);
        });
    }

    $("#btn-draft").click(function () {
        saveArticle(0, false);
    });
    // 定时自动保存 1 分钟
    setInterval(autoSave, 60000);
});

// ------------------------------------------------------------------
// 核心逻辑函数
// ------------------------------------------------------------------

/**
 * 自动保存功能
 */
function autoSave() {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();
    if (titleVal && contentVal) {
        console.log("执行自动保存...");
        saveArticle(0, true);
    }
}

function saveArticle(status, isSilent) {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();
    // --- 修改：从隐藏域获取选中的分类ID ---
    const categoryIdVal = $("#selected-category-id").val();

    if (!titleVal) {
        if (!isSilent) alert("标题不能为空");
        return;
    }
    // 发布状态下，必须选择分类
    if (status === 1 && (!categoryIdVal || categoryIdVal === "")) {
        if (!isSilent) alert("发布文章请务必选择一个分类！");
        return;
    }

    const payload = {
        title: titleVal,
        content: contentVal,
        summary: contentVal.substring(0, 100),
        categoryId: categoryIdVal || 0,
        status: status
    };

    let action = 'publishArticle';
    if (editArticleId) {
        action = 'updateArticle';
        payload.id = editArticleId;
    }
    payload.action = action;

    $.ajax({
        url: ARTICLE_API_URL,
        type: "POST",
        data: payload,
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                if (status === 0) {
                    if (!isSilent) alert("✅ 草稿保存成功！");
                    if (resp.data && resp.data.newId) {
                        editArticleId = resp.data.newId;
                        const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?id=' + editArticleId;
                        window.history.replaceState({path: newUrl}, '', newUrl);
                        $("#btn-publish").text("更新发布").off('click').click(function () {
                            saveArticle(1, false);
                        });
                    }
                } else {
                    alert("🎉 文章发布成功！");
                    window.location.href = "index.html";
                }
            } else {
                if (!isSilent) alert("❌ 操作失败: " + resp.message);
                if (resp.message && resp.message.includes("登录")) window.location.href = "login.html";
            }
        },
        error: function (xhr) {
            console.error(xhr);
            if (!isSilent) alert("网络错误");
        }
    });
}

function loadArticleForEdit(id) {
    $.ajax({
        url: ARTICLE_API_URL, type: "GET",
        data: {action: "getDetail", id: id},
        dataType: "json",
        success: function (resp) {
            if (resp.success && resp.data) {
                const article = resp.data;
                $("#title").val(article.title);
                $("#content").val(article.content);
                currentStatus = article.status;

                // --- 修改：分类回显逻辑 ---
                // 设置隐藏域的值
                $("#selected-category-id").val(article.categoryId);
                // 尝试根据ID高亮对应的标签
                // 我们使用一个定时器尝试几次，以应对分类数据加载比文章详情慢的情况
                let attempt = 0;
                const highlightInterval = setInterval(() => {
                    attempt++;
                    const $targetTag = $(`.category-tag[data-id='${article.categoryId}']`);
                    if ($targetTag.length > 0) {
                        // 找到了标签，触发点击以高亮
                        $targetTag.trigger('click');
                        clearInterval(highlightInterval);
                    } else if (attempt > 10) {
                        // 尝试10次（约1秒）后仍未找到，停止尝试
                        console.warn("分类回显失败：未找到对应的分类标签 ID=" + article.categoryId);
                        clearInterval(highlightInterval);
                    }
                }, 100);

            } else {
                alert("无法加载文章: " + resp.message);
                window.location.href = "center.html";
            }
        }
    });
}

// --- 修改：重写加载分类函数 ---
function loadCategories() {
    const $container = $("#category-tags-container");
    $.ajax({
        url: ARTICLE_API_URL, type: "GET", data: {action: 'loadCategories'}, dataType: "json",
        success: function (resp) {
            $container.empty(); // 清空加载提示
            if (resp.success && resp.data && resp.data.length > 0) {
                $.each(resp.data, function (index, category) {
                    // 创建标签元素
                    const $tag = $(`<div class="category-tag">${category.name}</div>`);
                    // 绑定数据ID
                    $tag.data("id", category.id);

                    // 绑定点击事件
                    $tag.click(function () {
                        // 1. 视觉交互：移除兄弟元素的选中状态，给自己加上
                        $(this).siblings(".category-tag").removeClass("active");
                        $(this).addClass("active");
                        // 2. 数据绑定：将ID填入隐藏域
                        $("#selected-category-id").val($(this).data("id"));
                    });

                    $container.append($tag);
                });
            } else {
                $container.html('<span style="color:#999;">暂无分类数据</span>');
            }
        },
        error: function () {
            $container.html('<span style="color:red;">加载失败</span>');
        }
    });
}