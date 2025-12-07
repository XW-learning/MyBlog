// src/main/webapp/static/js/password-utils.js

$(document).ready(function () {

    // 1. 初始化 SVG 图标
    const ICON_EYE = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        </svg>`;

    const ICON_EYE_OFF = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M1 1l22 22"></path>
            <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"></path>
        </svg>`;

    // 页面加载时，将所有占位符替换为 SVG
    $(".toggle-password").html(ICON_EYE_OFF);

    // 2. 绑定点击事件：切换密码可见性
    $(document).on("click", ".toggle-password", function () {
        const targetId = "#" + $(this).data("target");
        const $input = $(targetId);

        // 切换 Input 类型
        if ($input.attr("type") === "password") {
            $input.attr("type", "text");
            $(this).html(ICON_EYE); // 睁眼
            $(this).css("color", "#fc5531"); // 高亮图标
        } else {
            $input.attr("type", "password");
            $(this).html(ICON_EYE_OFF); // 闭眼
            $(this).css("color", ""); // 恢复默认色
        }
    });

    // 3. 密码强度检测 (仅对带有 data-check-strength="true" 的输入框生效)
    // 这样登录页面的密码框就不会出现强度条，只有注册页会显示
    $(document).on("input", "input[data-check-strength='true']", function () {
        const $input = $(this);
        const val = $input.val();

        // 查找或创建强度条容器
        let $strengthContainer = $input.closest(".form-group").find(".strength-container");
        if ($strengthContainer.length === 0) {
            const html = `
                <div class="strength-container">
                    <div class="strength-bar-bg">
                        <div class="strength-bar-fill"></div>
                    </div>
                    <div class="strength-text"></div>
                </div>
            `;
            $input.closest(".password-group").after(html);
            $strengthContainer = $input.closest(".form-group").find(".strength-container");
        }

        // 如果密码为空，隐藏条
        if (!val) {
            $strengthContainer.slideUp(200);
            return;
        } else {
            $strengthContainer.slideDown(200);
        }

        // 计算分数
        const score = calculateStrength(val);
        const $container = $strengthContainer;
        const $text = $strengthContainer.find(".strength-text");

        // 重置类名
        $container.removeClass("level-weak level-medium level-strong");

        // 根据分数应用样式
        if (score < 2) {
            $container.addClass("level-weak");
            $text.text("强度：弱 (建议包含字母、数字)");
        } else if (score < 4) {
            $container.addClass("level-medium");
            $text.text("强度：中 (建议包含大小写或符号)");
        } else {
            $container.addClass("level-strong");
            $text.text("强度：强 (完美的密码！)");
        }
    });

    // 简单算法：长度+字符类型
    function calculateStrength(password) {
        let score = 0;
        if (password.length >= 6) score++;
        if (password.length >= 10) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;
        return score; // Max 5
    }

});