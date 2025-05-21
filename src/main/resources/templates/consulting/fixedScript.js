/**
 * 상담 문의 상세 페이지 복사 기능
 * - 각 항목별 복사
 * - 전체 정보 복사
 */

// 텍스트 복사 함수
function copyText(element) {
    var textToCopy = element.getAttribute('data-text');
    if (!textToCopy) {
        console.error('복사할 텍스트가 없습니다:', element);
        return;
    }
    
    console.log('복사할 텍스트:', textToCopy);
    
    // 클립보드 API 사용 (모던 브라우저용)
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(textToCopy)
            .then(() => {
                // 성공 메시지 토스트
                showToast("복사되었습니다", "success");
            })
            .catch(err => {
                console.error('클립보드 API 복사 실패:', err);
                // 폴백: 기존 방식으로 시도
                legacyCopyText(textToCopy);
            });
    } else {
        // 폴백: 기존 방식으로 시도
        legacyCopyText(textToCopy);
    }
}

// 레거시 복사 방식
function legacyCopyText(text) {
    // textarea 생성 방식으로 텍스트 복사
    var textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed'; // 화면 외부에 위치시키기
    textarea.style.left = '-999999px';
    textarea.style.top = '-999999px';
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    
    try {
        var successful = document.execCommand('copy');
        if (successful) {
            // 성공 메시지 토스트
            showToast("복사되었습니다", "success");
        } else {
            throw new Error('복사 명령 실패');
        }
    } catch (err) {
        console.error('복사 실패:', err);
        // 실패 메시지
        showToast("복사 실패", "error");
    }
    
    document.body.removeChild(textarea);
}

// 토스트 메시지 표시 함수
function showToast(message, icon) {
    Swal.fire({
        text: message,
        icon: icon,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
        timerProgressBar: true
    });
}

// 전체 정보 복사 함수
function copyAllInfo() {
    try {
        console.log("전체 정보 복사 시작");
        
        // 문의 정보 가져오기
        var inquiryId = document.querySelector('.d-flex.align-items-center:nth-child(1) .fw-bold').textContent.trim();
        var customerName = document.querySelector('.d-flex.align-items-center:nth-child(2) .fw-bold').textContent.trim();
        var phoneNumber = document.querySelector('.d-flex.align-items-center:nth-child(3) .fw-bold').textContent.trim();
        var orderNumber = document.querySelector('.d-flex.align-items-center:nth-child(4) .fw-bold').textContent.trim();
        var inquiryType = document.querySelector('.d-flex.align-items-center:nth-child(5) .badge').textContent.trim();
        var assignedTo = document.querySelector('.d-flex.align-items-center:nth-child(6) .fw-bold').textContent.trim();
        var createdDate = document.querySelector('.d-flex.align-items-center:nth-child(7) .fw-bold').textContent.trim();
        
        console.log("기본 정보 수집 완료");
        
        var inquiryContent = document.querySelector('.inquiry-content').textContent.trim();
        var processContent = document.querySelectorAll('.inquiry-content')[1].textContent.trim();
        
        console.log("문의/처리 내용 수집 완료");
        
        // 모든 코멘트 가져오기
        var comments = [];
        document.querySelectorAll('.comment-item').forEach(function(commentEl) {
            var author = commentEl.querySelector('.comment-author span').textContent.trim();
            var date = commentEl.querySelector('.comment-date').textContent.trim();
            var isInternal = commentEl.querySelector('.badge-warning') !== null ? '[내부용]' : '';
            var content = commentEl.querySelector('.comment-content').textContent.trim();
            
            comments.push(`${isInternal} ${author} (${date}):\n${content}`);
        });
        
        console.log("코멘트 수집 완료:", comments.length);
        
        // 전체 정보 포맷팅
        var allInfo = `[문의정보]
문의번호: ${inquiryId}
고객명: ${customerName}
연락처: ${phoneNumber}
주문번호: ${orderNumber}
문의유형: ${inquiryType}
담당자: ${assignedTo}
등록일: ${createdDate}

[문의내용]
${inquiryContent}

[처리내용]
${processContent}`;
        
        // 코멘트가 있는 경우 추가
        if (comments.length > 0) {
            allInfo += '\n\n[코멘트]\n' + comments.join('\n\n');
        }
        
        console.log("전체 정보 포맷팅 완료");
        
        // 클립보드 API 사용
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(allInfo)
                .then(() => {
                    // 성공 메시지
                    showToast("전체 정보가 복사되었습니다", "success");
                    console.log("클립보드 API로 복사 성공");
                })
                .catch(err => {
                    console.error('클립보드 API 복사 실패:', err);
                    // 폴백: 레거시 방식으로 시도
                    legacyCopyFullText(allInfo);
                });
        } else {
            // 폴백: 레거시 방식으로 시도
            legacyCopyFullText(allInfo);
        }
    } catch (err) {
        console.error("전체 정보 복사 중 오류 발생:", err);
        showToast("복사 중 오류가 발생했습니다", "error");
    }
}

// 전체 정보 레거시 복사 함수
function legacyCopyFullText(text) {
    var textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.left = '-999999px';
    textarea.style.top = '-999999px';
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    
    try {
        var successful = document.execCommand('copy');
        if (successful) {
            // 성공 메시지
            showToast("전체 정보가 복사되었습니다", "success");
            console.log("레거시 방식으로 복사 성공");
        } else {
            throw new Error('복사 명령 실패');
        }
    } catch (err) {
        console.error('복사 실패:', err);
        showToast("복사 실패", "error");
    }
    
    document.body.removeChild(textarea);
}

// 이미지 로더 숨기기
function hideLoader(imgElement) {
    if (!imgElement) return;
    
    var fileId = imgElement.getAttribute('data-file-id');
    if (fileId) {
        var loadingEl = document.getElementById('loading-' + fileId);
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

// 첨부 이미지 보기
function showAttachmentImage(attachmentId) {
    var img = document.getElementById('attachment-img-' + attachmentId);
    if (!img) return;
    
    var filePath = img.getAttribute('data-file-path');
    var fileName = img.getAttribute('data-file-name');
    var fileId = img.getAttribute('data-file-id');
    
    // 미리보기 이미지 설정
    var previewImageSrc = document.getElementById('previewImageSrc');
    previewImageSrc.src = img.src;
    
    // 이미지 제목 설정
    document.getElementById('previewImageTitle').textContent = fileName || '첨부이미지';
    
    // 원본 이미지 링크 설정
    var originalImageBtn = document.getElementById('originalImageBtn');
    var downloadBtn = document.getElementById('previewImageDownload');
    
    if (filePath) {
        originalImageBtn.href = '/api/image-proxy?path=' + encodeURIComponent(filePath);
        downloadBtn.href = '/api/download?path=' + encodeURIComponent(filePath);
        downloadBtn.download = fileName || 'attachment-' + fileId;
        downloadBtn.style.display = 'inline-block';
        originalImageBtn.style.display = 'inline-block';
    } else {
        downloadBtn.style.display = 'none';
        originalImageBtn.style.display = 'none';
    }
    
    // 모달 열기
    var imageModal = new bootstrap.Modal(document.getElementById('imagePreviewModal'));
    imageModal.show();
}

// 첨부파일 디버깅 정보 토글 함수
function showAttachmentDebug() {
    var debugInfo = document.getElementById('attachmentDebugInfo');
    if (debugInfo) {
        if (debugInfo.style.display === 'none') {
            debugInfo.style.display = 'block';
            console.log('첨부파일 디버깅 정보 표시');
        } else {
            debugInfo.style.display = 'none';
            console.log('첨부파일 디버깅 정보 숨김');
        }
    }
}

// 페이지 로드 시 실행할 초기화 함수
function initDetailPage() {
    console.log("상담 문의 상세 페이지 초기화 실행");
    
    // 자동으로 텍스트 영역 높이 조정
    document.querySelectorAll('textarea').forEach(function(textarea) {
        adjustTextareaHeight(textarea);
        textarea.addEventListener('input', function() {
            adjustTextareaHeight(this);
        });
    });
    
    // Select2 초기화
    if (typeof $.fn.select2 !== 'undefined') {
        $('#statusSelect').select2({
            minimumResultsForSearch: -1 // 검색 상자 비활성화
        });
    }
    
    // 툴팁 초기화
    if (typeof $.fn.tooltip !== 'undefined') {
        $('[data-bs-toggle="tooltip"]').tooltip();
    }
    
    console.log("상담 문의 상세 페이지 초기화 완료");
}

// 텍스트 영역 높이 자동 조정
function adjustTextareaHeight(textarea) {
    console.log("텍스트 영역 높이 조정:", textarea.id);
    textarea.style.height = 'auto';
    textarea.style.height = (textarea.scrollHeight) + 'px';
}

// 페이지 로드 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', initDetailPage);
