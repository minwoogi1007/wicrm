// 전체 정보 복사 함수
function copyAllInfo() {
    // 문의 정보 가져오기
    var inquiryId = document.querySelector('.d-flex.align-items-center:nth-child(1) .fw-bold').textContent.trim();
    var customerName = document.querySelector('.d-flex.align-items-center:nth-child(2) .fw-bold').textContent.trim();
    var phoneNumber = document.querySelector('.d-flex.align-items-center:nth-child(3) .fw-bold').textContent.trim();
    var orderNumber = document.querySelector('.d-flex.align-items-center:nth-child(4) .fw-bold').textContent.trim();
    var inquiryType = document.querySelector('.d-flex.align-items-center:nth-child(5) .badge').textContent.trim();
    var assignedTo = document.querySelector('.d-flex.align-items-center:nth-child(6) .fw-bold').textContent.trim();
    var createdDate = document.querySelector('.d-flex.align-items-center:nth-child(7) .fw-bold').textContent.trim();
    var inquiryContent = document.querySelector('.inquiry-content').textContent.trim();
    var processContent = document.querySelectorAll('.inquiry-content')[1].textContent.trim();
    
    // 모든 코멘트 가져오기
    var comments = [];
    document.querySelectorAll('.comment-item').forEach(function(commentEl) {
        var author = commentEl.querySelector('.comment-author span').textContent.trim();
        var date = commentEl.querySelector('.comment-date').textContent.trim();
        var isInternal = commentEl.querySelector('.badge-warning') !== null ? '[내부용]' : '';
        var content = commentEl.querySelector('.comment-content').textContent.trim();
        
        comments.push(`${isInternal} ${author} (${date}):\n${content}`);
    });
    
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
    
    // 클립보드 API 사용
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(allInfo)
            .then(() => {
                // 성공 메시지
                showToast("전체 정보가 복사되었습니다", "success");
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
        } else {
            throw new Error('복사 명령 실패');
        }
    } catch (err) {
        console.error('복사 실패:', err);
        showToast("복사 실패", "error");
    }
    
    document.body.removeChild(textarea);
}
