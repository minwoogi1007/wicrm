/**
 * 교환/반품 목록 관리 JavaScript
 */
class ExchangeListManager {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 20;
        this.searchParams = {};
        this.selectedItems = new Set();
        
        this.init();
    }

    /**
     * 초기화
     */
    init() {
        this.bindEvents();
        this.loadDashboardStats();
        this.loadExchangeList();
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 검색 폼
        const searchForm = document.getElementById('searchForm');
        if (searchForm) {
            searchForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleSearch();
            });
        }

        // 리셋 버튼
        const resetBtn = document.getElementById('resetBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                this.handleReset();
            });
        }

        // 빠른 필터 버튼
        document.querySelectorAll('.quick-filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleQuickFilter(e.target.dataset.status);
            });
        });

        // 페이지 크기 변경
        const pageSizeSelect = document.getElementById('pageSize');
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                this.pageSize = parseInt(e.target.value);
                this.currentPage = 1;
                this.loadExchangeList();
            });
        }

        // 전체 선택 체크박스
        const selectAllCheckbox = document.getElementById('selectAll');
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', (e) => {
                this.handleSelectAll(e.target.checked);
            });
        }

        // 일괄 처리 버튼
        const batchUpdateBtn = document.getElementById('batchUpdateBtn');
        if (batchUpdateBtn) {
            batchUpdateBtn.addEventListener('click', () => {
                this.handleBatchUpdate();
            });
        }

        // 엑셀 다운로드 버튼
        const excelDownloadBtn = document.getElementById('excelDownloadBtn');
        if (excelDownloadBtn) {
            excelDownloadBtn.addEventListener('click', () => {
                this.handleExcelDownload();
            });
        }
    }

    /**
     * 대시보드 통계 로드
     */
    async loadDashboardStats() {
        try {
            const response = await fetch('/exchange/api/dashboard');
            const result = await response.json();
            
            if (result.success) {
                this.updateDashboardStats(result.data);
            } else {
                console.error('통계 데이터 로드 실패:', result.message);
            }
        } catch (error) {
            console.error('통계 데이터 로드 중 오류:', error);
        }
    }

    /**
     * 대시보드 통계 업데이트
     */
    updateDashboardStats(stats) {
        const totalElement = document.getElementById('totalCount');
        const pendingElement = document.getElementById('pendingCount');
        const processingElement = document.getElementById('processingCount');
        const completedElement = document.getElementById('completedCount');

        if (totalElement) totalElement.textContent = stats.totalCount || 0;
        if (pendingElement) pendingElement.textContent = stats.pendingCount || 0;
        if (processingElement) processingElement.textContent = stats.processingCount || 0;
        if (completedElement) completedElement.textContent = stats.completedCount || 0;
    }

    /**
     * 교환/반품 목록 로드
     */
    async loadExchangeList() {
        try {
            this.showLoading();

            const requestData = {
                ...this.searchParams,
                pageNum: this.currentPage,
                pageSize: this.pageSize
            };

            const headers = {
                'Content-Type': 'application/json',
            };
            
            // CSRF 토큰이 있으면 헤더에 추가
            if (window.csrfToken && window.csrfHeader) {
                headers[window.csrfHeader] = window.csrfToken;
            }

            const response = await fetch('/exchange/api/list', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(requestData)
            });

            const result = await response.json();
            
            if (result.success !== false) {
                this.renderExchangeList(result.list || []);
                this.renderPagination(result);
                this.updateResultInfo(result);
            } else {
                this.showError(result.message || '데이터 로드에 실패했습니다.');
            }
        } catch (error) {
            console.error('목록 로드 중 오류:', error);
            this.showError('서버 오류가 발생했습니다.');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * 교환/반품 목록 렌더링
     */
    renderExchangeList(items) {
        const tbody = document.getElementById('exchangeTableBody');
        if (!tbody) return;

        if (items.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="13" class="text-center py-4">
                        <div class="d-flex flex-column align-items-center">
                            <i class="ki-duotone ki-file-deleted fs-3x text-gray-400 mb-3">
                                <span class="path1"></span>
                                <span class="path2"></span>
                            </i>
                            <span class="text-gray-600">검색 결과가 없습니다.</span>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = items.map(item => `
            <tr>
                <td>
                    <div class="form-check form-check-sm">
                        <input class="form-check-input" type="checkbox" value="${item.returnId || ''}" 
                               onchange="exchangeManager.handleItemSelect(this)">
                    </div>
                </td>
                <td class="id-cell">${item.returnId || '-'}</td>
                <td>
                    <span class="type-badge ${this.getTypeBadgeClass(item.returnTypeLabel)}">${item.returnTypeLabel || '-'}</span>
                </td>
                <td>
                    <span class="order-number">${item.orderNumber || '-'}</span>
                </td>
                <td class="customer-info">
                    <div class="customer-name">${item.customerName || '-'}</div>
                    <div class="customer-phone">${item.customerPhone || '-'}</div>
                </td>
                <td>${item.siteName || '-'}</td>
                <td class="date-cell">${this.formatDate(item.csDate) || '-'}</td>
                <td>
                    <span class="status-badge ${this.getStatusBadgeClass(item.returnStatusLabel)}">${item.returnStatusLabel || '-'}</span>
                </td>
                <td class="amount-cell">${this.formatCurrency(item.returnAmount) || '-'}</td>
                <td class="amount-cell">${this.formatCurrency(item.shippingCost) || '-'}</td>
                <td style="max-width: 150px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${item.returnReason || '-'}">${item.returnReason || '-'}</td>
                <td>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                            <i class="fas fa-cog me-1"></i>작업
                        </button>
                        <ul class="dropdown-menu shadow">
                            <li><a class="dropdown-item" href="#" onclick="exchangeManager.editItem(${item.returnId})">
                                <i class="fas fa-edit me-2"></i>수정</a></li>
                            <li><a class="dropdown-item text-danger" href="#" onclick="exchangeManager.deleteItem(${item.returnId})">
                                <i class="fas fa-trash me-2"></i>삭제</a></li>
                        </ul>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    /**
     * 페이지네이션 렌더링
     */
    renderPagination(result) {
        const pagination = document.getElementById('pagination');
        if (!pagination) return;

        const { pageNum, totalPages, hasPrev, hasNext } = result;
        
        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let paginationHtml = `
            <ul class="pagination">
                <li class="page-item ${!hasPrev ? 'disabled' : ''}">
                    <a class="page-link" href="#" onclick="exchangeManager.goToPage(${pageNum - 1})">
                        <i class="previous"></i>
                    </a>
                </li>
        `;

        // 페이지 번호 생성
        const startPage = Math.max(1, pageNum - 2);
        const endPage = Math.min(totalPages, pageNum + 2);
        
        for (let i = startPage; i <= endPage; i++) {
            paginationHtml += `
                <li class="page-item ${i === pageNum ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="exchangeManager.goToPage(${i})">${i}</a>
                </li>
            `;
        }

        paginationHtml += `
                <li class="page-item ${!hasNext ? 'disabled' : ''}">
                    <a class="page-link" href="#" onclick="exchangeManager.goToPage(${pageNum + 1})">
                        <i class="next"></i>
                    </a>
                </li>
            </ul>
        `;

        pagination.innerHTML = paginationHtml;
    }

    /**
     * 결과 정보 업데이트
     */
    updateResultInfo(result) {
        const resultInfo = document.getElementById('resultInfo');
        if (resultInfo) {
            const { pageNum, pageSize, totalCount } = result;
            const startIndex = (pageNum - 1) * pageSize + 1;
            const endIndex = Math.min(pageNum * pageSize, totalCount);
            
            resultInfo.textContent = `총 ${totalCount}건 중 ${startIndex}-${endIndex}건 표시`;
        }
    }

    /**
     * 검색 처리
     */
    handleSearch() {
        const form = document.getElementById('searchForm');
        const formData = new FormData(form);
        
        this.searchParams = {};
        for (let [key, value] of formData.entries()) {
            if (value && value.trim()) {
                this.searchParams[key] = value.trim();
            }
        }
        
        this.currentPage = 1;
        this.loadExchangeList();
    }

    /**
     * 리셋 처리
     */
    handleReset() {
        document.getElementById('searchForm').reset();
        this.searchParams = {};
        this.currentPage = 1;
        this.loadExchangeList();
    }

    /**
     * 빠른 필터 처리
     */
    handleQuickFilter(status) {
        // 모든 빠른 필터 버튼에서 active 클래스 제거
        document.querySelectorAll('.quick-filter-btn').forEach(btn => {
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-light');
        });

        // 클릭된 버튼에 active 스타일 적용
        event.target.classList.remove('btn-light');
        event.target.classList.add('btn-primary');

        // 검색 조건 설정
        if (status === 'ALL') {
            delete this.searchParams.returnStatusCode;
        } else {
            this.searchParams.returnStatusCode = status;
        }

        this.currentPage = 1;
        this.loadExchangeList();
    }

    /**
     * 페이지 이동
     */
    goToPage(page) {
        this.currentPage = page;
        this.loadExchangeList();
    }

    /**
     * 아이템 선택 처리
     */
    handleItemSelect(checkbox) {
        const returnId = parseInt(checkbox.value);
        
        if (checkbox.checked) {
            this.selectedItems.add(returnId);
        } else {
            this.selectedItems.delete(returnId);
        }

        this.updateBatchControls();
    }

    /**
     * 전체 선택 처리
     */
    handleSelectAll(checked) {
        const checkboxes = document.querySelectorAll('#exchangeTableBody input[type="checkbox"]');
        
        checkboxes.forEach(checkbox => {
            checkbox.checked = checked;
            const returnId = parseInt(checkbox.value);
            
            if (checked) {
                this.selectedItems.add(returnId);
            } else {
                this.selectedItems.delete(returnId);
            }
        });

        this.updateBatchControls();
    }

    /**
     * 일괄 처리 컨트롤 업데이트
     */
    updateBatchControls() {
        const batchUpdateBtn = document.getElementById('batchUpdateBtn');
        const selectedCount = document.getElementById('selectedCount');
        
        if (selectedCount) {
            selectedCount.textContent = this.selectedItems.size;
        }
        
        if (batchUpdateBtn) {
            batchUpdateBtn.disabled = this.selectedItems.size === 0;
        }
    }

    /**
     * 상태 업데이트
     */
    async updateStatus(returnId, statusCode) {
        try {
            const response = await fetch('/exchange/api/updateStatus', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    returnId: returnId,
                    returnStatusCode: statusCode
                })
            });

            const result = await response.json();
            
            if (result.success) {
                this.showSuccess(result.message);
                this.loadExchangeList();
                this.loadDashboardStats();
            } else {
                this.showError(result.message);
            }
        } catch (error) {
            console.error('상태 업데이트 중 오류:', error);
            this.showError('서버 오류가 발생했습니다.');
        }
    }

    /**
     * 일괄 상태 업데이트
     */
    async handleBatchUpdate() {
        if (this.selectedItems.size === 0) {
            this.showError('선택된 항목이 없습니다.');
            return;
        }

        const statusCode = prompt('변경할 상태를 입력하세요 (PENDING, PROCESSING, COMPLETED):');
        if (!statusCode) return;

        try {
            const response = await fetch('/exchange/api/batchUpdateStatus', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    returnIds: Array.from(this.selectedItems),
                    returnStatusCode: statusCode.toUpperCase()
                })
            });

            const result = await response.json();
            
            if (result.success) {
                this.showSuccess(result.message);
                this.selectedItems.clear();
                this.loadExchangeList();
                this.loadDashboardStats();
                this.updateBatchControls();
            } else {
                this.showError(result.message);
            }
        } catch (error) {
            console.error('일괄 업데이트 중 오류:', error);
            this.showError('서버 오류가 발생했습니다.');
        }
    }

    /**
     * 상세보기
     */
    viewDetail(returnId) {
        window.open(`/exchange/view/${returnId}`, '_blank');
    }

    /**
     * 수정
     */
    editItem(returnId) {
        window.location.href = `/exchange/edit/${returnId}`;
    }

    /**
     * 엑셀 다운로드 처리
     */
    handleExcelDownload() {
        // 엑셀 다운로드 구현
        this.showInfo('엑셀 다운로드 기능은 준비 중입니다.');
    }

    /**
     * 유틸리티 메서드들
     */
    getStatusColor(status) {
        const colorMap = {
            'PENDING': 'warning',
            'PROCESSING': 'primary', 
            'SHIPPING': 'info',
            'COMPLETED': 'success',
            'CANCELED': 'danger'
        };
        return colorMap[status] || 'secondary';
    }

    getTypeColor(type) {
        const colorMap = {
            'EXCHANGE': 'primary',
            'RETURN': 'info',
            'FULL_RETURN': 'warning',
            'PARTIAL_RETURN': 'secondary'
        };
        return colorMap[type] || 'light';
    }

    /**
     * 타입 배지 클래스 반환
     */
    getTypeBadgeClass(typeLabel) {
        if (!typeLabel) return 'type-return';
        const label = typeLabel.toLowerCase();
        if (label.includes('교환')) return 'type-exchange';
        if (label.includes('전체')) return 'type-full-return';  
        if (label.includes('부분')) return 'type-partial-return';
        return 'type-return';
    }

    /**
     * 상태 배지 클래스 반환
     */
    getStatusBadgeClass(statusLabel) {
        if (!statusLabel) return 'status-pending';
        const label = statusLabel.toLowerCase();
        if (label.includes('대기')) return 'status-pending';
        if (label.includes('처리중') || label.includes('처리')) return 'status-processing';
        if (label.includes('배송') || label.includes('발송')) return 'status-shipping';
        if (label.includes('완료')) return 'status-completed';
        if (label.includes('취소') || label.includes('반려')) return 'status-canceled';
        return 'status-pending';
    }

    /**
     * 삭제 기능 추가
     */
    async deleteItem(returnId) {
        if (!confirm('정말로 이 항목을 삭제하시겠습니까?')) {
            return;
        }

        try {
            const headers = {
                'Content-Type': 'application/json',
            };
            
            if (window.csrfToken && window.csrfHeader) {
                headers[window.csrfHeader] = window.csrfToken;
            }

            const response = await fetch(`/exchange/api/delete/${returnId}`, {
                method: 'DELETE',
                headers: headers
            });

            const result = await response.json();
            
            if (result.success) {
                this.showSuccess('삭제가 완료되었습니다.');
                this.loadExchangeList();
            } else {
                this.showError(result.message || '삭제에 실패했습니다.');
            }
        } catch (error) {
            console.error('삭제 중 오류:', error);
            this.showError('서버 오류가 발생했습니다.');
        }
    }

    formatCurrency(amount) {
        if (!amount) return '0원';
        return new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: 'KRW'
        }).format(amount);
    }

    formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleDateString('ko-KR');
    }

    /**
     * UI 상태 관리 메서드들
     */
    showLoading() {
        const loadingElement = document.getElementById('loadingSpinner');
        if (loadingElement) {
            loadingElement.style.display = 'block';
        }
    }

    hideLoading() {
        const loadingElement = document.getElementById('loadingSpinner');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
    }

    showSuccess(message) {
        this.showToast(message, 'success');
    }

    showError(message) {
        this.showToast(message, 'error');
    }

    showInfo(message) {
        this.showToast(message, 'info');
    }

    showToast(message, type = 'info') {
        // Toast 알림 표시 (기본 alert로 임시 구현)
        alert(message);
    }
}

// 전역 변수로 ExchangeListManager 인스턴스 생성
let exchangeManager;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    exchangeManager = new ExchangeListManager();
}); 