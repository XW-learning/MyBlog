// src/main/webapp/static/js/write.js

// 🌟 全局 API 路径常量
const ARTICLE_API_URL = "/mypen/article";
const urlParams = new URLSearchParams(window.location.search);
let editArticleId = urlParams.get("id");
let currentStatus = 0;

// 🔥 记录初始状态
let originalTitle = "";
let originalContent = "";

// 🔥 防重复保存
let isSaving = false;

// 🔥 [新增] 放行令牌：如果是我们代码主动控制的跳转，就不触发浏览器原生弹窗
let isNavigatingAway = false;

$(document).ready(function () {
    const user = localStorage.getItem("user");
    if (!user) {
        showModal("请先登录！", function() {
            window.location.href = "login.html";
        });
        return;
    }

    // 1. 加载分类
    loadCategories();

    // 2. 初始化：新建 / 编辑模式
    if (editArticleId) {
        $("#btn-publish").text("更新发布").attr("id", "btn-update");
        loadArticleForEdit(editArticleId);

        $("#btn-update").click(() => saveArticle(1, false));
    } else {
        originalTitle = $("#title").val() || "";
        originalContent = $("#content").val() || "";

        $("#btn-publish").click(() => saveArticle(1, false));
    }

    $("#btn-draft").click(() => saveArticle(0, false));

    // 自动保存（1 分钟）
    setInterval(autoSave, 60000);

    // -------------------------------------------------------
    // ✅ 修复：同时拦截 click 和 mousedown，确保 100% 拦截跳转
    // -------------------------------------------------------
    $(document).on("click mousedown", ".back-link", function (e) {
        handleBackClick(e);
    });

    // ✅ 额外防护：拦截浏览器刷新/关闭按钮
    window.addEventListener('beforeunload', function (e) {
        // 🔥 [修改] 如果是我们主动跳转（已拿到令牌），直接放行，不弹窗
        if (isNavigatingAway) return;

        const currentTitle = $("#title").val() || "";
        const currentContent = $("#content").val() || "";

        const titleChanged = currentTitle.trim() !== originalTitle.trim();
        const contentChanged = currentContent.trim() !== originalContent.trim();

        if (titleChanged || contentChanged) {
            // 标准浏览器提示（无法自定义文案）
            e.preventDefault();
            e.returnValue = '您有未保存的内容，确定要离开吗？';
        }
    });

    // 输入监听
    $("#title, #content").on("input", function () {
        // console.log("内容变化检测中...");
    });
});

// 🔥 单独封装
function handleBackClick(e) {
    e.preventDefault();
    e.stopImmediatePropagation();
    e.stopPropagation();

    const targetUrl = $(e.currentTarget).attr("href") || "index.html";

    console.log("返回检测 → 当前 URL:", targetUrl);

    const currentTitle = $("#title").val() || "";
    const currentContent = $("#content").val() || "";

    const titleChanged = currentTitle.trim() !== originalTitle.trim();
    const contentChanged = currentContent.trim() !== originalContent.trim();

    if (titleChanged || contentChanged) {
        console.log("检测到未保存的内容！");

        // 使用自定义模态框 confirm
        showConfirm(
            "⚠️ 您有未保存的内容，是否保存为草稿？\n(点击确定保存，点击取消直接离开)",

            // --- 用户点击“确定” (保存并离开) ---
            function() {
                if (isSaving) {
                    showModal("保存中...");
                    return;
                }
                isSaving = true;

                saveArticle(0, false, function (success) {
                    isSaving = false;
                    if (success) {
                        // 保存成功，准备跳转
                        setTimeout(() => {
                            // 🔥 [修改] 设置令牌，允许离开
                            isNavigatingAway = true;
                            window.location.href = targetUrl;
                        }, 100);
                    } else {
                        // 保存失败，再次询问
                        showConfirm(
                            "草稿保存失败，是否仍然要离开？（未保存内容将丢失）",
                            function() {
                                // 强制离开
                                isNavigatingAway = true;
                                window.location.href = targetUrl;
                            },
                            null // 取消则留在此页
                        );
                    }
                });
            },

            // --- 用户点击“取消” (放弃修改，直接离开) ---
            function() {
                // 🔥 [修改] 设置令牌，允许离开
                isNavigatingAway = true;
                window.location.href = targetUrl;
            }
        );
    } else {
        // 无修改，直接跳转
        isNavigatingAway = true;
        window.location.href = targetUrl;
    }
}

// 自动保存
function autoSave() {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();

    if (
        titleVal &&
        (titleVal.trim() !== originalTitle.trim() ||
            contentVal.trim() !== originalContent.trim())
    ) {
        console.log("自动保存中...");
        saveArticle(0, true);
    }
}

// 保存/更新文章
function saveArticle(status, isSilent, successCallback) {
    const titleVal = $("#title").val();
    const contentVal = $("#content").val();
    const categoryIdVal = $("#selected-category-id").val();

    if (!titleVal) {
        if (!isSilent) {
            showModal("标题不能为空");
        }
        if (successCallback) successCallback(false);
        return;
    }

    if (status === 1 && (!categoryIdVal || categoryIdVal === "")) {
        if (!isSilent) {
            showModal("发布文章请务必选择一个分类！");
        }
        if (successCallback) successCallback(false);
        return;
    }

    const payload = {
        title: titleVal,
        content: contentVal,
        summary: contentVal.substring(0, 100),
        categoryId: categoryIdVal || 0,
        status: status
    };

    let action = "publishArticle";
    if (editArticleId) {
        action = "updateArticle";
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
                // 更新基准值
                originalTitle = titleVal;
                originalContent = contentVal;

                if (status === 0) {
                    if (!isSilent) {
                        showModal("✅ 草稿保存成功！");
                    }

                    if (resp.data && resp.data.newId) {
                        editArticleId = resp.data.newId;
                        const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + "?id=" + editArticleId;
                        window.history.replaceState({ path: newUrl }, "", newUrl);

                        $("#btn-publish")
                            .text("更新发布")
                            .off("click")
                            .click(() => saveArticle(1, false));
                    }
                } else {
                    // 发布成功跳转
                    showModal("🎉 文章发布成功！", function() {
                        // 🔥 [修改] 设置令牌，允许离开
                        isNavigatingAway = true;
                        window.location.href = "index.html";
                    });
                }

                if (successCallback) successCallback(true);
            } else {
                if (!isSilent) {
                    showModal("❌ 操作失败: " + resp.message);
                }
                if (resp.message && resp.message.includes("登录")) {
                    showModal(resp.message, function() {
                        isNavigatingAway = true; // 允许跳去登录页
                        window.location.href = "login.html";
                    });
                }
                if (successCallback) successCallback(false);
            }
        },
        error: function (xhr) {
            console.error(xhr);
            if (!isSilent) {
                showModal("网络错误，保存失败");
            }
            if (successCallback) successCallback(false);
        }
    });
}

// 加载文章（编辑模式）
function loadArticleForEdit(id) {
    $.ajax({
        url: ARTICLE_API_URL,
        type: "GET",
        data: { action: "getDetail", id: id },
        dataType: "json",
        success: function (resp) {
            if (resp.success && resp.data) {
                const article = resp.data;

                $("#title").val(article.title);
                $("#content").val(article.content);

                // 更新基准值
                originalTitle = article.title;
                originalContent = article.content;

                $("#selected-category-id").val(article.categoryId);

                // 分类 tag 回显逻辑
                let attempt = 0;
                const highlightInterval = setInterval(() => {
                    attempt++;
                    const $targetTag = $(`.category-tag[data-id='${article.categoryId}']`);
                    if ($targetTag.length > 0) {
                        $targetTag.trigger("click");
                        clearInterval(highlightInterval);
                    } else if (attempt > 10) {
                        clearInterval(highlightInterval);
                    }
                }, 100);
            } else {
                showModal("无法加载文章: " + resp.message, function() {
                    isNavigatingAway = true;
                    window.location.href = "center.html";
                });
            }
        },
        error: function() {
            showModal("加载文章失败，请重试", function() {
                isNavigatingAway = true;
                window.location.href = "center.html";
            });
        }
    });
}

// 加载分类
function loadCategories() {
    const $container = $("#category-tags-container");

    $.ajax({
        url: ARTICLE_API_URL,
        type: "GET",
        data: { action: "loadCategories" },
        dataType: "json",
        success: function (resp) {
            $container.empty();
            if (resp.success && resp.data && resp.data.length > 0) {
                resp.data.forEach((category) => {
                    const $tag = $(
                        `<div class="category-tag" data-id="${category.id}">${category.name}</div>`
                    );

                    $tag.click(function () {
                        $(this).siblings(".category-tag").removeClass("active");
                        $(this).addClass("active");
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