/**
 * 电表管理页面功能实现
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
            centerAddr: '',
            ammeterNo: ''
        },
        selectedAmmeters: new Set()
    };

    // 加载电表列表
    async function loadAmmeterList() {
        try {
            const response = await utils.ajax({
                url: '/api/admin/ammeter/list',
                method: 'GET',
                data: {
                    page: state.currentPage,
                    size: state.pageSize,
                    centerAddr: state.searchParams.centerAddr,
                    ammeterNo: state.searchParams.ammeterNo
                }
            });

            updateListUI(response.data);
            updatePagination(response.total);
        } catch (error) {
            utils.showMessage('加载电表列表失败', 'error');
        }
    }

    // 更新列表UI
    function updateListUI(data) {
        const tbody = document.getElementById('ammeterList');
        tbody.innerHTML = data.map(item => `
            <tr>
                <td class="td_left">
                    <input type="checkbox" class="ammeter-checkbox" value="${item.ammeterNo}"
                        ${state.selectedAmmeters.has(item.ammeterNo) ? 'checked' : ''}>
                </td>
                <td>${item.ammeterNo}</td>
                <td>${item.type}</td>
                <td>
                    <span class="status ${item.status === 'online' ? 'status-online' : 'status-offline'}">
                        ${item.status === 'online' ? '在线' : '离线'}
                    </span>
                </td>
                <td>${item.valveStatus}</td>
                <td>${item.readTime ? utils.formatDate(item.readTime) : '--'}</td>
                <td>
                    <button type="button" class="detail-btn" data-ammeter="${item.ammeterNo}">详情</button>
                </td>
            </tr>
        `).join('');

        // 更新全选框状态
        updateSelectAllCheckbox();
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

    // 更新选中状态
    function updateSelectionState() {
        const count = state.selectedAmmeters.size;
        document.getElementById('selectedCount').textContent = count;
        document.getElementById('batchReadBtn').disabled = count === 0;
        updateSelectAllCheckbox();
    }

    // 更新全选框状态
    function updateSelectAllCheckbox() {
        const checkboxes = document.querySelectorAll('.ammeter-checkbox');
        const selectAllCheckbox = document.getElementById('selectAll');
        if (checkboxes.length === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else {
            const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;
            selectAllCheckbox.checked = checkedCount === checkboxes.length;
            selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
        }
    }

    // 加载电表详情
    async function loadAmmeterDetail(ammeterNo) {
        try {
            const response = await utils.ajax({
                url: '/api/admin/ammeter/detail',
                data: { ammeterNo },
                method: 'GET'
            });
            updateDetailUI(response);
            showDetailModal();
        } catch (error) {
            utils.showMessage('加载电表详情失败', 'error');
        }
    }

    // 更新详情UI
    function updateDetailUI(data) {
        document.getElementById('detail-ammeterNo').textContent = data.ammeterNo || '--';
        document.getElementById('detail-cat').textContent = data.cat || '--';
        document.getElementById('detail-type').textContent = data.type || '--';
        document.getElementById('detail-psw').textContent = data.psw || '--';
        document.getElementById('detail-comPort').textContent = data.comPort || '--';
        document.getElementById('detail-baudRate').textContent = data.baudRate || '--';
        document.getElementById('detail-protocol').textContent = data.protocol || '--';
        document.getElementById('detail-status').textContent = data.status || '--';
        document.getElementById('detail-valveStatus').textContent = data.valveStatus || '--';
        document.getElementById('detail-readTime').textContent = data.readTime ? utils.formatDate(data.readTime) : '--';
        document.getElementById('detail-peek').textContent = data.peek || '--';
        document.getElementById('detail-sharp').textContent = data.sharp || '--';
        document.getElementById('detail-flat').textContent = data.flat || '--';
        document.getElementById('detail-low').textContent = data.low || '--';
    }

    // 显示详情弹窗
    function showDetailModal() {
        document.getElementById('detailModal').style.display = 'block';
    }

    // 批量采集
    async function batchRead() {
        if (state.selectedAmmeters.size === 0) {
            return;
        }

        try {
            await utils.ajax({
                url: '/api/admin/ammeter/read',
                method: 'POST',
                data: {
                    ammeterNos: Array.from(state.selectedAmmeters)
                }
            });
            utils.showMessage('批量采集请求已发送', 'success');
        } catch (error) {
            utils.showMessage('批量采集失败', 'error');
        }
    }

    // 初始化事件监听
    function initEventListeners() {
        // 搜索按钮点击
        document.getElementById('searchBtn').addEventListener('click', () => {
            state.searchParams.centerAddr = document.getElementById('centerAddr').value.trim();
            state.searchParams.ammeterNo = document.getElementById('ammeterNo').value.trim();
            state.currentPage = 1;
            loadAmmeterList();
        });

        // 回车搜索
        ['centerAddr', 'ammeterNo'].forEach(id => {
            document.getElementById(id).addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    document.getElementById('searchBtn').click();
                }
            });
        });

        // 分页控制
        document.getElementById('prevPage').addEventListener('click', () => {
            if (state.currentPage > 1) {
                state.currentPage--;
                loadAmmeterList();
            }
        });

        document.getElementById('nextPage').addEventListener('click', () => {
            const totalPages = Math.ceil(state.total / state.pageSize);
            if (state.currentPage < totalPages) {
                state.currentPage++;
                loadAmmeterList();
            }
        });

        // 全选/取消全选
        document.getElementById('selectAll').addEventListener('change', (e) => {
            const checkboxes = document.querySelectorAll('.ammeter-checkbox');
            checkboxes.forEach(cb => {
                cb.checked = e.target.checked;
                if (e.target.checked) {
                    state.selectedAmmeters.add(cb.value);
                } else {
                    state.selectedAmmeters.delete(cb.value);
                }
            });
            updateSelectionState();
        });

        // 单个选择
        document.getElementById('ammeterList').addEventListener('change', (e) => {
            if (e.target.classList.contains('ammeter-checkbox')) {
                if (e.target.checked) {
                    state.selectedAmmeters.add(e.target.value);
                } else {
                    state.selectedAmmeters.delete(e.target.value);
                }
                updateSelectionState();
            }
        });

        // 详情按钮点击
        document.getElementById('ammeterList').addEventListener('click', (e) => {
            const detailBtn = e.target.closest('.detail-btn');
            if (detailBtn) {
                const ammeterNo = detailBtn.dataset.ammeter;
                if (ammeterNo) {
                    loadAmmeterDetail(ammeterNo);
                }
            }
        });

        // 关闭详情弹窗
        document.querySelector('.close').addEventListener('click', () => {
            document.getElementById('detailModal').style.display = 'none';
        });

        // 点击弹窗外部关闭
        window.addEventListener('click', (e) => {
            const modal = document.getElementById('detailModal');
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        });

        // 批量采集
        document.getElementById('batchReadBtn').addEventListener('click', batchRead);
    }

    // 初始化页面
    function initPage() {
        initEventListeners();
        loadAmmeterList();
    }

    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', initPage);
})(); 