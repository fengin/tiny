/**
 * 登录管理器类
 * 处理用户登录相关的功能，包括表单提交、登录状态检查等
 */
class LoginManager {
    /**
     * 构造函数
     * 初始化登录表单引用
     */
    constructor() {
        this.form = document.getElementById('loginForm');
        this.init();
    }
    
    /**
     * 初始化方法
     * 设置表单提交事件监听，检查登录状态
     */
    init() {
        // 绑定表单提交事件
        this.form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.login();
        });
        
        // 检查是否已登录，如果已登录则直接跳转到设备页面
        if (localStorage.getItem('token')) {
            window.location.href = '/device.html';
        }
    }
    
    /**
     * 登录方法
     * 处理登录表单提交，发送登录请求
     */
    async login() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        try {
            const response = await fetch('/api/admin/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });
            
            if (response.ok) {
                // 登录成功，保存token和用户名
                const data = await response.json();
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', username);
                // 跳转到设备页面
                window.location.href = '/device.html';
            } else {
                this.showError('用户名或密码错误');
            }
        } catch (error) {
            console.error('Login failed:', error);
            this.showError('登录失败，请重试');
        }
    }
    
    /**
     * 显示错误信息
     * @param {string} message - 错误信息
     */
    showError(message) {
        alert(message);
    }
}

// 当DOM加载完成后初始化登录管理器
document.addEventListener('DOMContentLoaded', () => {
    new LoginManager();
}); 