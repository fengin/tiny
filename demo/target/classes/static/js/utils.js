/**
 * 通用工具函数库
 */
const utils = {
    /**
     * AJAX请求封装
     * @param {Object} options 配置项
     * @returns {Promise}
     */
    ajax: function(options) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            
            // 处理GET请求的参数
            let url = options.url;
            if (options.method === 'GET' && options.data) {
                const params = new URLSearchParams(options.data);
                url = `${url}?${params.toString()}`;
            }
            
            // 设置请求方法和URL
            xhr.open(options.method || 'GET', url);
            
            // 设置请求头
            xhr.setRequestHeader('Content-Type', 'application/json');
            
            // 处理认证信息
            const token = localStorage.getItem('token');
            if (token) {
                xhr.setRequestHeader('Authorization', token);
            }
            
            // 处理响应
            xhr.onload = function() {
                if (xhr.status >= 200 && xhr.status < 300) {
                    try {
                        const response = JSON.parse(xhr.responseText);
                        if (response.success) {
                            resolve(response.data);
                        } else {
                            reject(new Error(response.msg || '请求失败'));
                        }
                    } catch (e) {
                        reject(new Error('解析响应数据失败'));
                    }
                } else {
                    reject(new Error(xhr.statusText));
                }
            };
            
            // 处理错误
            xhr.onerror = function() {
                reject(new Error('网络请求失败'));
            };
            
            // 发送请求
            if (options.data && options.method !== 'GET') {
                xhr.send(JSON.stringify(options.data));
            } else {
                xhr.send();
            }
        });
    },

    /**
     * 显示提示消息
     * @param {string} message 消息内容
     * @param {string} type 消息类型：success/error/warning/info
     */
    showMessage: function(message, type = 'info') {
        const msgDiv = document.createElement('div');
        msgDiv.className = `message message-${type}`;
        msgDiv.textContent = message;
        document.body.appendChild(msgDiv);
        
        setTimeout(() => {
            msgDiv.remove();
        }, 3000);
    },

    /**
     * 检查登录状态
     * @returns {boolean}
     */
    checkLogin: function() {
        const token = localStorage.getItem('token');
        if (!token) {
            location.href = '/index.html';
            return false;
        }
        return true;
    },

    /**
     * 格式化日期
     * @param {Date|string|number} date 日期对象或时间戳
     * @param {string} format 格式化模板
     * @returns {string}
     */
    formatDate: function(date, format = 'YYYY-MM-DD HH:mm:ss') {
        date = new Date(date);
        const map = {
            YYYY: date.getFullYear(),
            MM: String(date.getMonth() + 1).padStart(2, '0'),
            DD: String(date.getDate()).padStart(2, '0'),
            HH: String(date.getHours()).padStart(2, '0'),
            mm: String(date.getMinutes()).padStart(2, '0'),
            ss: String(date.getSeconds()).padStart(2, '0')
        };
        
        return format.replace(/YYYY|MM|DD|HH|mm|ss/g, matched => map[matched]);
    }
}; 