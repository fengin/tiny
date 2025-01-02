/**
 * 集中器管理页面功能实现
 */
(function() {
    // 检查登录状态
    if (!utils.checkLogin()) {
        return;
    }

    // 页面状态管理
    const state = {
        currentPage: 1,
        pageSize: 10,
        total: 0,
        searchParams: {
            name: '',
            addr: ''
        }
    };

    // 加载集中器列表
    async function loadCenterList() {
        try {
            const response = await utils.ajax({
                url: '/api/admin/center/list',
                method: 'GET',
                data: {
                    page: state.currentPage,
                    size: state.pageSize,
                    name: state.searchParams.name,
                    addr: state.searchParams.addr
                }
            });

            updateListUI(response.data);
            updatePagination(response.total);
        } catch (error) {
            utils.showMessage('加载集中器列表失败', 'error');
        }
    }

    // 更新列表UI
    function updateListUI(data) {
        const tbody = document.getElementById('centerList');
        tbody.innerHTML = data.map(item => `
            <tr>
                <td class="td_left">${item.name}</td>
                <td>
                    <span class="status ${item.status === 'online' ? 'status-online' : 'status-offline'}">
                        ${item.status === 'online' ? '在线' : '离线'}
                    </span>
                </td>
                <td>${item.type}</td>
                <td>${item.addr}</td>
                <td>${item.ammeterNum}</td>
                <td>${item.ammeter?.online || 0}/${item.ammeter?.offline || 0}</td>
            </tr>
        `).join('');
    }

    // 更新分页信息
    function updatePagination(total) {
        state.total = total;
        const totalPages = Math.ceil(total / state.pageSize);
        
        document.getElementById('pageInfo').textContent = 
            `第 ${state.currentPage} 页 / 共 ${totalPages} 页`;
        
        document.getElementById('prevPage').disabled = state.currentPage <= 1;
        document.getElementById('nextPage').disabled = state.currentPage >= totalPages;
    }

    // 初始化事件监听
    function initEventListeners() {
        // 搜索按钮点击
        document.getElementById('searchBtn').addEventListener('click', () => {
            state.searchParams.name = document.getElementById('centerName').value.trim();
            state.searchParams.addr = document.getElementById('centerAddr').value.trim();
            state.currentPage = 1;
            loadCenterList();
        });

        // 上一页
        document.getElementById('prevPage').addEventListener('click', () => {
            if (state.currentPage > 1) {
                state.currentPage--;
                loadCenterList();
            }
        });

        // 下一页
        document.getElementById('nextPage').addEventListener('click', () => {
            const totalPages = Math.ceil(state.total / state.pageSize);
            if (state.currentPage < totalPages) {
                state.currentPage++;
                loadCenterList();
            }
        });

        // 回车搜索
        const searchInputs = ['centerName', 'centerAddr'];
        searchInputs.forEach(id => {
            document.getElementById(id).addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    document.getElementById('searchBtn').click();
                }
            });
        });
    }

    // 初始化页面
    function initPage() {
        initEventListeners();
        loadCenterList();
    }

    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', initPage);
})(); 