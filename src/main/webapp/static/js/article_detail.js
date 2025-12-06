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
        alert("文章ID缺失");
        window.location.href = "index.html";
    }
});

// -------------------------------------------------------------
// 1. 文章详情与点赞模块 (保持不变)
// -------------------------------------------------------------
// ... (此处代码与你提供的一致，为节省篇幅省略，请保留原有的 loadArticleDetail 等函数) ...
function loadArticleDetail(articleId) { /* ...原代码... */
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
                alert("加载失败: " + resp.message);
                window.location.href = "index.html";
            }
        },
        error: function () {
            alert("网络错误，无法加载文章。");
        }
    });
}

function checkMyLikeStatus(articleId) { /* ...原代码... */
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

function handleLikeClick(articleId) { /* ...原代码... */
    if (!localStorage.getItem("user")) {
        alert("请先登录再点赞！");
        window.location.href = "login.html";
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
                alert(resp.message);
            }
        },
        error: function () {
            alert("操作失败，请检查网络");
        }
    });
}

function updateLikeButtonStyle(isLiked) { /* ...原代码... */
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
// 2. 评论模块 (核心逻辑修改：实现扁平化展示)
// -------------------------------------------------------------

/**
 * 加载并渲染评论列表
 */
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
                // resp.data 应该是只有一级评论的数组(树状结构)
                $.each(resp.data, function (index, rootComment) {
                    // 这里我们专门调用构建“根评论”的方法
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

/**
 * 构建【一级评论】HTML (包含子评论容器)
 */
function buildRootCommentHtml(comment) {
    // 1. 生成一级评论主体
    const mainBody = buildSingleCommentItem(comment, false);

    // 2. 处理子评论：将所有后代扁平化，放在同一个容器里
    let childrenHtml = "";

    // 获取所有后代（拍平）
    const allDescendants = flattenChildren(comment.children);

    if (allDescendants.length > 0) {
        let subItems = "";
        allDescendants.forEach(child => {
            // 对子评论调用生成单项的方法，标记为 isSub=true
            subItems += buildSingleCommentItem(child, true);
        });

        // 包装在 .sub-comments 容器中
        childrenHtml = `<div class="sub-comments">${subItems}</div>`;
    }

    // 3. 组合：一级评论内容 + (内嵌的)子评论区域
    // 注意：我们将 childrenHtml 放在 comment-body 内部的最下方

    // 为了插入到准确位置，这里我们需要稍微手动拼接一下，或者复用 buildSingleCommentItem
    // 为了简单清晰，我重写一下 Root 的拼接逻辑：

    return `
        <div class="comment-item" data-comment-id="${comment.id}">
             ${getAvatarHtml(comment, false)}
             <div class="comment-body">
                 ${getCommentContentHtml(comment)}
                 ${childrenHtml} </div>
        </div>
    `;
}

/**
 * 辅助：生成单条评论的 HTML 内容 (不包含包裹子评论的逻辑)
 * @param {Object} comment 评论数据
 * @param {Boolean} isSub 是否是子评论 (用于样式微调)
 */
function buildSingleCommentItem(comment, isSub) {
    // 如果是子评论，它是被包裹在 .sub-comments 里的 .comment-item
    // 如果是根评论，它已经在 buildRootCommentHtml 里被包裹了

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
        // 仅返回内容部分，供 buildRootCommentHtml 使用
        // 其实这一步在 buildRootCommentHtml 已经手动拼接了，这里主要服务于递归逻辑的解耦
        return "";
    }
}

/**
 * 辅助：获取头像 HTML
 */
function getAvatarHtml(comment, isSmall) {
    const avatarClass = isSmall ? "comment-avatar small" : "comment-avatar"; // CSS可以配合调整大小
    const src = comment.userAvatar;

    if (src) {
        return `<img src="${src}" class="${avatarClass}">`;
    } else {
        return `<div class="${avatarClass}">👤</div>`;
    }
}

/**
 * 辅助：获取评论主体内容 HTML (昵称、文本、操作栏)
 */
function getCommentContentHtml(comment) {
    let nickname = comment.userNickname || "匿名用户";
    let time = new Date(comment.createTime).toLocaleString();

    // 权限检查
    const currentUserJson = localStorage.getItem("user");
    const currentUser = currentUserJson ? JSON.parse(currentUserJson) : null;
    let deleteBtn = '';
    if (currentUser && (currentUser.id === comment.userId || currentUser.id === 1)) {
        deleteBtn = `<span class="comment-action-btn btn-delete-comment" data-id="${comment.id}">删除</span>`;
    }

    // 回复对象提示 (关键：扁平化后，依靠这个显示是在回复谁)
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

/**
 * 核心算法：将树状的 children 递归拍平成一维数组
 */
function flattenChildren(children) {
    let result = [];
    if (!children || children.length === 0) return result;

    children.forEach(child => {
        // 1. 加入当前子节点
        result.push(child);

        // 2. 如果当前子节点还有子节点，递归获取并合并
        if (child.children && child.children.length > 0) {
            result = result.concat(flattenChildren(child.children));
        }
    });

    return result;
}


// -------------------------------------------------------------
// 3. 评论交互事件绑定 (保持不变，逻辑兼容)
// -------------------------------------------------------------

function bindCommentEvents(articleId) {
    $("#comment-list").off('click');

    // 删除
    $("#comment-list").on('click', '.btn-delete-comment', function () {
        const commentId = $(this).data('id');
        if (confirm("确定要删除这条评论吗？")) {
            deleteComment(commentId);
        }
    });

    // 回复
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
        // 插入位置：插在 .comment-info 后面
        $commentBody.children(".comment-info").after(replyBoxHtml);
        $commentBody.find("textarea").focus();
    });

    // 取消
    $("#comment-list").on("click", ".btn-cancel-reply", function () {
        $(this).closest(".inline-reply-box").remove();
    });

    // 提交回复
    $("#comment-list").on("click", ".btn-submit-reply", function () {
        const parentId = $(this).data("parent-id");
        const $box = $(this).closest(".inline-reply-box");
        const content = $box.find("textarea").val();
        submitComment(articleId, content, parentId, function () {
            $box.remove();
        });
    });
}

// -------------------------------------------------------------
// 4. 数据提交逻辑 (保持不变)
// -------------------------------------------------------------
function deleteComment(commentId) {
    // 1. 先获取当前文章ID，用于刷新列表
    const params = new URLSearchParams(window.location.search);
    const articleId = params.get("id");

    $.ajax({
        url: COMMENT_API_URL,
        type: "POST",
        data: {action: "delete", commentId: commentId},
        dataType: "json", // 确保解析 JSON
        success: function (resp) {
            if (resp.success) {
                // 方案 A (旧)：只删除了被点击的那一行，会导致子评论变成“幽灵”
                // $(`.comment-item[data-comment-id="${commentId}"]`).remove();

                // 方案 B (新)：删除成功后，重新加载列表
                // 这样能自动把已经被级联删除的子评论也一同清掉，保证数据同步
                loadComments(articleId);

                // 可选：给个轻提示
                // alert("删除成功");
            } else {
                alert(resp.message);
            }
        },
        error: function () {
            alert("删除失败，网络错误");
        }
    });
}

function submitComment(articleId, content, parentId, successCallback) {
    if (!content || content.trim() === "") {
        alert("请输入评论内容");
        return;
    }
    if (!localStorage.getItem("user")) {
        alert("请先登录");
        location.href = "login.html";
        return;
    }

    $.ajax({
        url: COMMENT_API_URL, type: "POST",
        data: {action: "publish", articleId: articleId, content: content, parentId: parentId || ""},
        dataType: "json",
        success: function (resp) {
            if (resp.success) {
                alert("评论成功！");
                if (successCallback) successCallback();
                loadComments(articleId); // 重新加载以刷新列表
            } else {
                alert("发布失败: " + resp.message);
                if (resp.message.includes("登录")) window.location.href = "login.html";
            }
        },
        error: function () {
            alert("网络错误，请稍后重试");
        }
    });
}