/**
 * 首页功能实现
 */
(function() {
    // 检查登录状态
    if (!utils.checkLogin()) {
        return;
    }

    // 系统状态相关
    const systemStatus = {
        // 加载系统状态数据
        async loadStatus() {
            try {
                const response = await utils.ajax({
                    url: '/api/admin/system/info'
                });
                this.updateStatusUI(response);
            } catch (error) {
                utils.showMessage('加载系统状态失败', 'error');
            }
        },

        // 更新系统状态UI
        updateStatusUI(data) {
            document.getElementById('startTime').textContent = utils.formatDate(data.startTime);
            document.getElementById('runTime').textContent = this.formatRunTime(data.runTime);
            document.getElementById('memory').textContent = this.formatMemory(data.memory);
            document.getElementById('diskUsage').textContent = 
                `${this.formatSize(data.diskUsage)} / ${this.formatSize(data.diskTotal)}`;
            document.getElementById('lastBeatTime').textContent = 
                data.lastBeatTime ? utils.formatDate(data.lastBeatTime) : '--';
            document.getElementById('ip').textContent = data.ip || '--';
            document.getElementById('httpPort').textContent = data.httpPort || '--';
            document.getElementById('tcpPort').textContent = data.tcpPort || '--';
        },

        // 格式化运行时长
        formatRunTime(ms) {
            if (!ms) return '--';
            const days = Math.floor(ms / (24 * 60 * 60 * 1000));
            const hours = Math.floor((ms % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000));
            const minutes = Math.floor((ms % (60 * 60 * 1000)) / (60 * 1000));
            return `${days}天${hours}小时${minutes}分钟`;
        },

        // 格式化内存大小
        formatMemory(bytes) {
            return this.formatSize(bytes);
        },

        // 格式化文件大小
        formatSize(bytes) {
            if (!bytes) return '--';
            const units = ['B', 'KB', 'MB', 'GB', 'TB'];
            let size = bytes;
            let unitIndex = 0;
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024;
                unitIndex++;
            }
            return `${size.toFixed(2)}${units[unitIndex]}`;
        }
    };

    // 设备状态相关
    const deviceStatus = {
        // 加载设��状态数据
        async loadStatus() {
            try {
                const response = await utils.ajax({
                    url: '/api/admin/device/info'
                });
                this.updateStatusUI(response);
            } catch (error) {
                utils.showMessage('加载设备状态失败', 'error');
            }
        },

        // 更新设备状态UI
        updateStatusUI(data) {
            document.getElementById('centerOnline').textContent = data.center?.online || 0;
            document.getElementById('centerOffline').textContent = data.center?.offline || 0;
            document.getElementById('ammeterOnline').textContent = data.ammeter?.online || 0;
            document.getElementById('ammeterOffline').textContent = data.ammeter?.offline || 0;
            document.getElementById('lastReadTime').textContent = 
                data.lastReadTime ? utils.formatDate(data.lastReadTime) : '--';
            document.getElementById('readSuccess').textContent = data.ok || 0;
            document.getElementById('readFailed').textContent = data.failed || 0;
        }
    };

    // 系统配置相关
    const systemConfig = {
        // 加载配置数据
        async loadConfig() {
            try {
                const response = await utils.ajax({
                    url: '/api/admin/system/config'
                });
                this.updateConfigUI(response);
            } catch (error) {
                utils.showMessage('加载系统配置失败', 'error');
            }
        },

        // 更新配置UI
        updateConfigUI(data) {
            document.getElementById('remotAddr').value = data.remotAddr || '';
            document.getElementById('remotPort').value = data.remotPort || '';
            document.getElementById('remotSecret').value = data.remotSecret || '';
            document.getElementById('configIp').value = data.ip || '';
            document.getElementById('secret').value = data.secret || '';
        },

        // 保存配置
        async saveConfig(formData) {
            try {
                await utils.ajax({
                    url: '/api/admin/system/config/update',
                    method: 'POST',
                    data: formData
                });
                this.showMessage('配置保存成功', 'success');
            } catch (error) {
                this.showMessage('配置保存失败', 'error');
            }
        },

        // 拉取配置
        async pullConfig() {
            try {
                await utils.ajax({
                    url: '/api/admin/device/config/pull',
                    method: 'POST'
                });
                this.showMessage('配置拉取成功', 'success');
                // 重新加载设备状态
                deviceStatus.loadStatus();
            } catch (error) {
                this.showMessage('配置拉取失败', 'error');
            }
        },

        // 显示消息提示
        showMessage(message, type = 'info') {
            const messageBox = document.getElementById('messageBox');
            const messageContent = messageBox.querySelector('.message-content');
            
            messageBox.className = `message-box ${type}`;
            messageContent.textContent = message;
            messageBox.style.display = 'block';
            
            // 2秒后自动隐藏
            setTimeout(() => {
                messageBox.style.display = 'none';
            }, 2000);
        }
    };

    // 初始化页面
    function initPage() {
        // 加载初始数据
        systemStatus.loadStatus();
        deviceStatus.loadStatus();
        systemConfig.loadConfig();

        // 配置表单提交处理
        const configForm = document.getElementById('configForm');
        configForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = {
                remotAddr: document.getElementById('remotAddr').value,
                remotPort: document.getElementById('remotPort').value,
                remotSecret: document.getElementById('remotSecret').value,
                ip: document.getElementById('configIp').value,
                secret: document.getElementById('secret').value
            };
            await systemConfig.saveConfig(formData);
        });

        // 同步按钮点击处理
        document.getElementById('syncBtn').addEventListener('click', () => {
            systemConfig.pullConfig();
        });

        // 定时刷新状态
        setInterval(() => {
            systemStatus.loadStatus();
            deviceStatus.loadStatus();
        }, 5000); // 每5秒刷新一次
    }

    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', initPage);
})(); 