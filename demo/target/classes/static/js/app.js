/**
 * 应用主JS文件
 */
(function() {
    // 检查登录状态
    if (!utils.checkLogin()) {
        return;
    }

    // 初始化用户信息
    function initUserInfo() {
        const username = localStorage.getItem('username');
        if (username) {
            document.querySelector('.username').textContent = username;
        }
    }

    // 处理菜单点击
    function handleMenuClick() {
        const menu = document.querySelector('.sidebar-menu ul');
        menu.addEventListener('click', (e) => {
            const link = e.target.closest('a');
            if (!link) return;
            
            e.preventDefault();
            const menuItem = link.closest('.menu-item');
            if (menuItem) {
                // 移除其他菜单项的active类
                document.querySelectorAll('.menu-item').forEach(item => {
                    item.classList.remove('active');
                });
                // 添加当前菜单项的active类
                menuItem.classList.add('active');
                
                // 加载对应的页面内容
                const url = link.dataset.url;
                if (url) {
                    const content = document.querySelector('.app-content');
                    content.innerHTML = '<iframe src="' + url + '" style="width:100%;height:100%;border:none;"></iframe>';
                }
            }
        });
    }

    // 处理退出登录
    function handleLogout() {
        const logoutBtn = document.querySelector('.logout');
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            location.href = '/index.html';
        });
    }

    // ��始化页面
    function initPage() {
        initUserInfo();
        handleMenuClick();
        handleLogout();

        // 默认加载首页内容
        const content = document.querySelector('.app-content');
        content.innerHTML = '<iframe src="home.html" style="width:100%;height:100%;border:none;"></iframe>';
    }

    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', initPage);
})(); 