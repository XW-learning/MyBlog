// src/main/webapp/static/js/article_detail.js

const COMMENT_API_URL = "/mypen/comment";
const ARTICLE_API_URL = "/mypen/article";

$(document).ready(function () {
    // 1. 从 URL 获取文章 ID
    const params = new URLSearchParams(window.location.search);
    const articleId = params.get("id");

    if (articleId) {
        loadArticleDetail(articleId);
        loadComments(articleId);

        // 绑定顶部的主发表按钮
        $("#btn-submit-comment").click(function () {
            const content = $("#comment-content").val();
            submitComment(articleId, content, null, function () {
                $("#comment-content").val("");
            });
        });

        $("#btn-like").click(function () {
            handleLikeClick(articleId);
        });

        bindCommentEvents(articleId);

    } else {
        // ❌ 修复：删除 alert
        showModal("文章ID缺失", function () {
            window.location.href = "index.html";
        });
    }
});

// -------------------------------------------------------------
// 1. 文章详情与点赞模块
// -------------------------------------------------------------
function loadArticleDetail(articleId) {
    $.ajax({
        url: ARTICLE_API_URL, type: "GET", data: {action: "getDetail", id: articleId}, dataType: "json",
        success: function (resp) {
            if (resp.success && resp.data) {
                const article = resp.data;
                $("#article-title").text(article.title);
                $("#article-author").text("👤 作者ID: " + article.userId);
                $("#article-time").text("📅 " + new Date(article.createTime).toLocaleString());
                $("#article-views").text("👁️ 阅读: " + article.views);
                $("#article-content").html(article.content);
                $("#like-count").text(article.likes);
                document.title = article.title + " - 个人博客";
                checkMyLikeStatus(articleId);
            } else {
                // ❌ 修复：删除 alert
                showModal("加载失败: " + resp.message, function () {
                    window.location.href = "index.html";
                });
            }
        },
        error: function () {
            // ❌ 修复：删除 alert
            showModal("网络错误，无法加载文章。");
        }
    });
}

function checkMyLikeStatus(articleId) {
    if (!localStorage.getItem("user")) return;
    $.ajax({
        url: ARTICLE_API_URL,
        type: "GET",
        data: {action: "checkLikeStatus", id: articleId},
        dataType: "json",
        success: function (resp) {
            if (resp.success) updateLikeButtonStyle(resp.data);
        }
    });
}

function handleLikeClick(articleId) {
    if (!localStorage.getItem("user")) {
        // ❌ 修复：删除 alert，并使用回调跳转
        showModal("请先登录再点赞！", function () {
            window.location.href = "login.html";
        });
        return;
    }
    $.ajax({
        url: ARTICLE_API_URL,
        type: "POST",
        data: {action: "like", id: articleId},
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                $("#like-count").text(resp.data.newCount);
                updateLikeButtonStyle(resp.data.isLiked);
            } else {
                // ❌ 修复：删除 alert
                showModal(resp.message);
            }
        },
        error: function () {
            // ❌ 修复：删除 alert
            showModal("操作失败，请检查网络");
        }
    });
}

function updateLikeButtonStyle(isLiked) {
    const $btn = $("#btn-like");
    const $text = $("#like-text");
    if (isLiked) {
        $btn.addClass("active");
        if ($text.length) $text.text("已赞");
    } else {
        $btn.removeClass("active");
        if ($text.length) $text.text("点赞");
    }
}


// -------------------------------------------------------------
// 2. 评论模块
// -------------------------------------------------------------

function loadComments(articleId) {
    const $list = $("#comment-list");
    $list.html('<p class="loading-text">正在加载评论...</p>');

    $.ajax({
        url: COMMENT_API_URL,
        type: "GET",
        data: {action: "list", articleId: articleId},
        dataType: "json",
        success: function (resp) {
            $list.empty();
            if (resp.success && resp.data && resp.data.length > 0) {
                $.each(resp.data, function (index, rootComment) {
                    $list.append(buildRootCommentHtml(rootComment));
                });
            } else {
                $list.html('<p style="color:#999; text-align:center; padding: 20px;">暂无评论，快来抢沙发吧！</p>');
            }
        },
        error: function () {
            $list.html('<p style="color:red; text-align:center;">评论加载失败</p>');
        }
    });
}

function buildRootCommentHtml(comment) {
    // ... (保持不变)
    const mainBody = buildSingleCommentItem(comment, false);
    let childrenHtml = "";
    const allDescendants = flattenChildren(comment.children);

    if (allDescendants.length > 0) {
        let subItems = "";
        allDescendants.forEach(child => {
            subItems += buildSingleCommentItem(child, true);
        });
        childrenHtml = `<div class="sub-comments">${subItems}</div>`;
    }

    return `
        <div class="comment-item" data-comment-id="${comment.id}">
             ${getAvatarHtml(comment, false)}
             <div class="comment-body">
                 ${getCommentContentHtml(comment)}
                 ${childrenHtml} </div>
        </div>
    `;
}

function buildSingleCommentItem(comment, isSub) {
    // ... (保持不变)
    if (isSub) {
        return `
            <div class="comment-item" data-comment-id="${comment.id}">
                ${getAvatarHtml(comment, true)}
                <div class="comment-body">
                    ${getCommentContentHtml(comment)}
                </div>
            </div>
        `;
    } else {
        return "";
    }
}

function getAvatarHtml(comment, isSmall) {
    // ... (保持不变)
    const avatarClass = isSmall ? "comment-avatar small" : "comment-avatar";
    const src = comment.userAvatar;

    if (src) {
        return `<img src="${src}" class="${avatarClass}" onerror="this.src='../static/img/default-avatar.png'">`;
    } else {
        return `<div class="${avatarClass}">👤</div>`;
    }
}

function getCommentContentHtml(comment) {
    // ... (保持不变)
    let nickname = comment.userNickname || "匿名用户";
    let time = new Date(comment.createTime).toLocaleString();

    const currentUserJson = localStorage.getItem("user");
    const currentUser = currentUserJson ? JSON.parse(currentUserJson) : null;
    let deleteBtn = '';
    if (currentUser && (currentUser.id === comment.userId || currentUser.id === 1)) {
        deleteBtn = `<span class="comment-action-btn btn-delete-comment" data-id="${comment.id}">删除</span>`;
    }

    let replyTargetHtml = "";
    if (comment.parentNickname) {
        replyTargetHtml = `<span class="reply-target" style="color:#999; margin-right:5px;">回复 @${comment.parentNickname}:</span>`;
    }

    return `
        <div class="comment-user">${nickname}</div>
        <div class="comment-text">
            ${replyTargetHtml}
            ${comment.content}
        </div>
        <div class="comment-info">
            <span style="margin-right: 15px;">${time}</span>
            <span class="comment-actions-bar">
                <span class="comment-action-btn btn-reply" data-id="${comment.id}" data-nickname="${nickname}">回复</span>
                ${deleteBtn}
            </span>
        </div>
    `;
}

function flattenChildren(children) {
    // ... (保持不变)
    let result = [];
    if (!children || children.length === 0) return result;
    children.forEach(child => {
        result.push(child);
        if (child.children && child.children.length > 0) {
            result = result.concat(flattenChildren(child.children));
        }
    });
    return result;
}

function bindCommentEvents(articleId) {
    $("#comment-list").off('click');

    // 删除
    $("#comment-list").on('click', '.btn-delete-comment', function () {
        const commentId = $(this).data('id');
        // 🔥 这里保留 confirm 是合适的，它是系统级阻塞，防止误删
        if (confirm("确定要删除这条评论吗？")) {
            deleteComment(commentId);
        }
    });

    // 回复逻辑 (保持不变)
    $("#comment-list").on('click', '.btn-reply', function () {
        const $btn = $(this);
        const commentId = $btn.data("id");
        const nickname = $btn.data("nickname");
        const $commentBody = $btn.closest(".comment-body");

        if ($commentBody.find(".inline-reply-box").length > 0) {
            $commentBody.find("textarea").focus();
            return;
        }
        $(".inline-reply-box").remove();

        const replyBoxHtml = `
            <div class="inline-reply-box" style="margin-top: 10px; padding: 10px; background: #fafafa; border-radius: 4px;">
                <textarea class="comment-textarea small" 
                          style="width:100%; height:60px; padding:8px; border:1px solid #ddd; resize:none;" 
                          placeholder="回复 @${nickname}:"></textarea>
                <div class="comment-actions" style="margin-top: 5px; text-align: right;">
                    <button class="btn-cancel-reply" style="margin-right: 10px; background:none; border:none; color:#666; cursor:pointer;">取消</button>
                    <button class="btn-primary btn-submit-reply" 
                            style="background:#fc5531; color:#fff; border:none; padding:4px 12px; border-radius:4px; cursor:pointer;"
                            data-parent-id="${commentId}">发表</button>
                </div>
            </div>
        `;
        $commentBody.children(".comment-info").after(replyBoxHtml);
        $commentBody.find("textarea").focus();
    });

    $("#comment-list").on("click", ".btn-cancel-reply", function () {
        $(this).closest(".inline-reply-box").remove();
    });

    $("#comment-list").on("click", ".btn-submit-reply", function () {
        const parentId = $(this).data("parent-id");
        const $box = $(this).closest(".inline-reply-box");
        const content = $box.find("textarea").val();
        submitComment(articleId, content, parentId, function () {
            $box.remove();
        });
    });
}

function deleteComment(commentId) {
    const params = new URLSearchParams(window.location.search);
    const articleId = params.get("id");

    $.ajax({
        url: COMMENT_API_URL,
        type: "POST",
        data: {action: "delete", commentId: commentId},
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                // 删除成功无需弹窗，直接刷新体验更好
                loadComments(articleId);
            } else {
                // ❌ 修复：删除 alert，保留 showModal
                showModal("删除失败: " + resp.message);
            }
        },
        error: function () {
            // ❌ 修复：删除 alert
            showModal("删除失败，网络错误");
        }
    });
}

function submitComment(articleId, content, parentId, successCallback) {
    if (!content || content.trim() === "") {
        // ❌ 修复：删除 alert
        showModal("请输入评论内容");
        return;
    }
    if (!localStorage.getItem("user")) {
        // ❌ 修复：删除 alert，并使用回调跳转
        showModal("请先登录", function () {
            location.href = "login.html";
        });
        return;
    }

    $.ajax({
        url: COMMENT_API_URL, type: "POST",
        data: {action: "publish", articleId: articleId, content: content, parentId: parentId || ""},
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                // ❌ 修复：删除 alert
                showModal("评论成功！");
                if (successCallback) successCallback();
                loadComments(articleId);
            } else {
                if (resp.message.includes("登录")) {
                    // ❌ 修复：删除 alert，并使用回调跳转
                    showModal("发布失败: " + resp.message, function () {
                        window.location.href = "login.html";
                    });
                } else {
                    // ❌ 修复：删除 alert
                    showModal("发布失败: " + resp.message);
                }
            }
        },
        error: function () {
            // ❌ 修复：删除 alert
            showModal("网络错误，请稍后重试");
        }
    });
}