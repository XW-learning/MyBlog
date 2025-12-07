// 全局回调变量
let modalConfirmCallback = null;
let modalCancelCallback = null;

/**
 * 普通提示框 (只有确定按钮)
 */
function showModal(message, callback) {
    document.getElementById('modalMessage').textContent = message;

    // 隐藏取消按钮
    document.getElementById('modalCancel').style.display = 'none';

    modalConfirmCallback = callback;
    modalCancelCallback = null; // 清空取消回调

    document.getElementById('customModal').style.display = 'block';
}

/**
 * 🔥 新增：确认对话框 (有确定和取消按钮)
 * @param message 提示内容
 * @param onConfirm 点击确定后的回调
 * @param onCancel 点击取消后的回调
 */
function showConfirm(message, onConfirm, onCancel) {
    document.getElementById('modalMessage').textContent = message;

    // 显示取消按钮
    document.getElementById('modalCancel').style.display = 'inline-block';

    modalConfirmCallback = onConfirm;
    modalCancelCallback = onCancel;

    document.getElementById('customModal').style.display = 'block';
}

// 隐藏模态框
function hideModal() {
    document.getElementById('customModal').style.display = 'none';
}

// --- 绑定事件 ---

// 点击“确定”
document.getElementById('modalConfirm').addEventListener('click', function() {
    hideModal();
    if (modalConfirmCallback) {
        modalConfirmCallback();
        modalConfirmCallback = null; // 防止重复触发
    }
});

// 点击“取消”
document.getElementById('modalCancel').addEventListener('click', function() {
    hideModal();
    if (modalCancelCallback) {
        modalCancelCallback();
        modalCancelCallback = null;
    }
});

// 点击外部关闭 (视为取消)
window.onclick = function(event) {
    const modal = document.getElementById('customModal');
    if (event.target === modal) {
        hideModal();
        // 如果有取消回调，点击外部也触发取消逻辑（可选）
        // if (modalCancelCallback) modalCancelCallback();
    }
};